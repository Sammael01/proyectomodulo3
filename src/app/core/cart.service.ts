import { HttpClient } from '@angular/common/http';
import { Injectable, computed, effect, inject, signal } from '@angular/core';
import { Observable, finalize } from 'rxjs';
import { API_BASE_URL, joinApiUrl } from './api-config';
import { API_ENDPOINTS, endpoint } from './api-endpoints';
import { apiMessage } from './api-error';
import { AuthService } from './auth.service';
import { CatalogService } from './catalog.service';
import { CartItem, CartLine, Product } from './models';
import { DataSource } from './remote-data';
import { readStorage, writeStorage } from './storage';

const CART_KEY = 'tstyle_cart';
const COUPON_KEY = 'tstyle_coupon';
/** Valores de referencia para la interfaz. Precios, envío, cupones y totales definitivos los calcula el backend. */
const FREE_SHIPPING_FROM = 150000;
const SHIPPING_COST = 12000;
const MAX_QTY = 10;
/** Cupones de demostración (modo local). En modo API los valida el backend. */
const COUPONS: Record<string, number> = { TSTYLE10: 0.1, OTONO26: 0.15 };

/**
 * ADAPTADOR PENDIENTE: convierte la respuesta del carrito del módulo C en líneas locales.
 * Acepta un arreglo de líneas o un objeto con `items`; ajustar al DTO definitivo de C.
 */
export function toCartLines(body: unknown): CartLine[] {
  const raw = Array.isArray(body) ? body : (body as { items?: unknown[] } | null)?.items;
  if (!Array.isArray(raw)) return [];
  return raw.map((item) => {
    const i = item as { productId: string | number; size: string; qty?: number; quantity?: number };
    return { productId: String(i.productId), size: i.size, qty: Number(i.qty ?? i.quantity ?? 1) };
  });
}

/**
 * Carrito. La copia local (localStorage) mejora la experiencia, pero NO es fuente oficial de
 * inventario ni de precios: en modo API cada cambio se envía al backend y se reemplaza el estado
 * local por la respuesta del servidor; si el backend lo rechaza (p. ej. inventario), se revierte.
 */
@Injectable({ providedIn: 'root' })
export class CartService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(API_BASE_URL);
  private readonly auth = inject(AuthService);
  private readonly catalog = inject(CatalogService);
  private readonly routes = API_ENDPOINTS.cart;
  private readonly lines = signal<CartLine[]>(readStorage(CART_KEY, []));
  private readonly _coupon = signal<string | null>(readStorage(COUPON_KEY, null));
  private readonly _syncing = signal(false);
  private readonly _error = signal<string | null>(null);

  /** `api` cuando C confirme el endpoint de consulta del carrito. */
  readonly source: DataSource = this.routes.get ? 'api' : 'local';
  readonly coupon = this._coupon.asReadonly();
  readonly syncing = this._syncing.asReadonly();
  readonly error = this._error.asReadonly();
  readonly freeShippingFrom = FREE_SHIPPING_FROM;
  readonly maxQty = MAX_QTY;

  readonly items = computed<CartItem[]>(() =>
    this.lines()
      .map((line) => ({ ...line, product: this.catalog.byId(line.productId) as Product }))
      .filter((item) => !!item.product)
  );
  readonly count = computed(() => this.items().reduce((n, i) => n + i.qty, 0));
  /** Estimado para la interfaz; el valor definitivo lo calcula el backend al crear el pedido. */
  readonly subtotal = computed(() => this.items().reduce((s, i) => s + i.qty * i.product.price, 0));
  readonly discount = computed(() => {
    const code = this._coupon();
    return code && code in COUPONS ? Math.round(this.subtotal() * COUPONS[code]) : 0;
  });
  readonly shipping = computed(() =>
    this.items().length === 0 || this.subtotal() >= FREE_SHIPPING_FROM ? 0 : SHIPPING_COST
  );
  readonly total = computed(() => this.subtotal() - this.discount() + this.shipping());
  readonly shippingProgress = computed(() => Math.min(100, (this.subtotal() / FREE_SHIPPING_FROM) * 100));
  readonly missingForFreeShipping = computed(() => Math.max(0, FREE_SHIPPING_FROM - this.subtotal()));

  constructor() {
    effect(() => writeStorage(CART_KEY, this.lines()));
    effect(() => writeStorage(COUPON_KEY, this._coupon()));
    if (this.source === 'api') {
      effect(() => {
        if (this.auth.isLoggedIn()) this.refresh();
      });
    }
  }

  /** Solo lectura de las líneas (productId real, talla y cantidad), p. ej. para crear el pedido. */
  snapshot(): CartLine[] {
    return this.lines().map((l) => ({ ...l }));
  }

  /** Modo API: reemplaza la copia local por el carrito del servidor. */
  refresh(): void {
    if (this.source === 'local' || !this.auth.isLoggedIn()) return;
    this.sync(this.http.get<unknown>(this.url(this.routes.get!)), this.lines());
  }

  add(product: Product, size = product.sizes[0], qty = 1): void {
    const previous = this.lines();
    this.lines.update((lines) => {
      const found = lines.find((l) => l.productId === product.id && l.size === size);
      return found
        ? lines.map((l) => (l === found ? { ...l, qty: Math.min(MAX_QTY, l.qty + qty) } : l))
        : [...lines, { productId: product.id, size, qty: Math.min(MAX_QTY, qty) }];
    });
    this.remote(this.routes.addItem, (url) => this.http.post<unknown>(url, { productId: product.id, size, quantity: qty }), previous);
  }

  setQty(item: CartItem, qty: number): void {
    if (qty < 1) return this.remove(item);
    const previous = this.lines();
    const next = Math.min(qty, MAX_QTY);
    this.lines.update((lines) => lines.map((l) => (this.same(l, item) ? { ...l, qty: next } : l)));
    this.remote(this.routes.updateItem, (url) => this.http.put<unknown>(url, { productId: item.productId, size: item.size, quantity: next }), previous);
  }

  setSize(item: CartItem, size: string): void {
    const previous = this.lines();
    this.lines.update((lines) => {
      const target = lines.find((l) => l.productId === item.productId && l.size === size);
      if (!target) return lines.map((l) => (this.same(l, item) ? { ...l, size } : l));
      return lines
        .filter((l) => !this.same(l, item))
        .map((l) => (l === target ? { ...l, qty: Math.min(MAX_QTY, l.qty + item.qty) } : l));
    });
    this.remote(
      this.routes.updateItem,
      (url) => this.http.put<unknown>(url, { productId: item.productId, size: item.size, newSize: size, quantity: item.qty }),
      previous
    );
  }

  remove(item: CartItem): void {
    const previous = this.lines();
    this.lines.update((lines) => lines.filter((l) => !this.same(l, item)));
    this.remote(
      this.routes.removeItem,
      (url) => this.http.delete<unknown>(url, { body: { productId: item.productId, size: item.size } }),
      previous
    );
  }

  /** Vacía el carrito. `localOnly` se usa tras crear un pedido, cuando el backend ya lo vació. */
  clear(localOnly = false): void {
    const previous = this.lines();
    this.lines.set([]);
    this._coupon.set(null);
    if (!localOnly) this.remote(this.routes.clear, (url) => this.http.delete<unknown>(url), previous);
  }

  applyCoupon(code: string): boolean {
    const normalized = code.trim().toUpperCase();
    if (!(normalized in COUPONS)) return false;
    this._coupon.set(normalized);
    return true;
  }

  removeCoupon(): void {
    this._coupon.set(null);
  }

  clearError(): void {
    this._error.set(null);
  }

  /**
   * Envía la operación al backend cuando el endpoint esté confirmado. Mientras sea null, el cambio
   * queda solo en la copia local (modo actual). Los cuerpos enviados son PROPUESTA pendiente de C.
   */
  private remote(route: string | null, call: (url: string) => Observable<unknown>, previous: CartLine[]): void {
    if (this.source === 'local' || !route || !this.auth.isLoggedIn()) return;
    this.sync(call(this.url(endpoint(route))), previous);
  }

  private sync(request: Observable<unknown>, previous: CartLine[]): void {
    this._syncing.set(true);
    this._error.set(null);
    request.pipe(finalize(() => this._syncing.set(false))).subscribe({
      next: (body) => {
        if (body !== null && body !== undefined) this.lines.set(toCartLines(body));
      },
      error: (err) => {
        this.lines.set(previous);
        this._error.set(apiMessage(err, 'No fue posible actualizar el carrito.'));
      },
    });
  }

  private url(path: string): string {
    return joinApiUrl(this.api, path);
  }

  private same(a: CartLine, b: CartLine): boolean {
    return a.productId === b.productId && a.size === b.size;
  }
}
