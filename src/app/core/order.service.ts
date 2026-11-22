import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, finalize, map, of, tap, throwError } from 'rxjs';
import { API_BASE_URL, joinApiUrl } from './api-config';
import { API_ENDPOINTS, PendingEndpointError, endpoint } from './api-endpoints';
import { apiMessage } from './api-error';
import { AuthService } from './auth.service';
import { CartService } from './cart.service';
import { Order, PaymentMethod, ShippingInfo } from './models';
import { DataSource, toPageResult } from './remote-data';
import { readStorage, writeStorage } from './storage';

const ORDERS_KEY = 'tstyle_orders';

/**
 * PROPUESTA de cuerpo para crear un pedido (pendiente de confirmar con C).
 * Solo identifica productos, tallas y cantidades: NO incluye precios, descuentos ni totales,
 * que el backend debe calcular. Nunca incluye datos de tarjeta.
 */
export interface CreateOrderRequest {
  items: { productId: string; size: string; quantity: number }[];
  shippingInfo: ShippingInfo;
  paymentMethod: PaymentMethod;
  couponCode?: string;
}

/** ADAPTADOR PENDIENTE: convierte el DTO de pedido del módulo C al modelo `Order`. */
export function toOrder(raw: unknown): Order {
  const dto = raw as Order & { id: string | number };
  return { ...dto, id: String(dto.id), items: Array.isArray(dto.items) ? dto.items : [] };
}

@Injectable({ providedIn: 'root' })
export class OrderService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(API_BASE_URL);
  private readonly auth = inject(AuthService);
  private readonly cart = inject(CartService);
  private readonly routes = API_ENDPOINTS.orders;
  private readonly adminRoutes = API_ENDPOINTS.ordersAdmin;

  /** `api` cuando C confirme el endpoint de creación de pedidos. */
  readonly source: DataSource = this.routes.create ? 'api' : 'local';

  private readonly _all = signal<Order[]>(this.source === 'local' ? readStorage(ORDERS_KEY, []) : []);
  private readonly _mine = signal<Order[]>([]);
  private readonly _loading = signal(false);
  private readonly _error = signal<string | null>(null);

  readonly loading = this._loading.asReadonly();
  readonly error = this._error.asReadonly();
  /** Todos los pedidos (panel admin). */
  readonly all = this._all.asReadonly();
  /** Pedidos del usuario autenticado. */
  readonly mine = computed(() => {
    if (this.source === 'api') return this._mine();
    const email = this.auth.user()?.email;
    return email ? this._all().filter((o) => o.userEmail === email) : [];
  });

  /** Crea el pedido. En modo API el backend asigna id, valida inventario y calcula los totales. */
  submit(shippingInfo: ShippingInfo, payment: PaymentMethod): Observable<Order> {
    if (this.source === 'local') return of(this.placeLocal(shippingInfo, payment));
    const body: CreateOrderRequest = {
      items: this.cart.snapshot().map((l) => ({ productId: l.productId, size: l.size, quantity: l.qty })),
      shippingInfo,
      paymentMethod: payment,
      ...(this.cart.coupon() ? { couponCode: this.cart.coupon()! } : {}),
    };
    return this.http.post<unknown>(this.url(this.routes.create!), body).pipe(
      map(toOrder),
      tap((order) => {
        this._mine.update((list) => [order, ...list]);
        this.cart.clear(true);
      })
    );
  }

  /** Carga los pedidos del usuario autenticado desde el backend. En modo local no hace nada. */
  loadMine(): void {
    if (this.source === 'local') return;
    if (!this.routes.mine) return this._error.set(apiMessage(new PendingEndpointError('orders.mine')));
    this.fetch(this.routes.mine, (orders) => this._mine.set(orders));
  }

  /** Panel admin: carga todos los pedidos (el backend debe exigir rol ADMIN). */
  loadAll(): void {
    if (this.source === 'local') return;
    if (!this.adminRoutes.all) return this._error.set(apiMessage(new PendingEndpointError('ordersAdmin.all')));
    this.fetch(this.adminRoutes.all, (orders) => this._all.set(orders));
  }

  /** Panel admin: actualiza el estado de un pedido. */
  updateStatus(id: string, status: Order['status']): Observable<Order> {
    const apply = (order: Order) => this._all.update((list) => list.map((o) => (o.id === order.id ? order : o)));
    if (this.source === 'local') {
      const current = this._all().find((o) => o.id === id);
      if (!current) return throwError(() => new Error('Pedido no encontrado'));
      const updated = { ...current, status };
      apply(updated);
      this.persist();
      return of(updated);
    }
    if (!this.adminRoutes.updateStatus) return throwError(() => new PendingEndpointError('ordersAdmin.updateStatus'));
    // PENDIENTE (C): formato del estado (enum del backend) y del cuerpo.
    return this.http.patch<unknown>(this.url(endpoint(this.adminRoutes.updateStatus, { id })), { status }).pipe(map(toOrder), tap(apply));
  }

  /** Modo DEMOSTRACIÓN: el pedido se guarda solo en este navegador. */
  private placeLocal(shippingInfo: ShippingInfo, payment: PaymentMethod): Order {
    const order: Order = {
      id: 'TS-' + Date.now().toString(36).toUpperCase(),
      date: new Date().toISOString(),
      userEmail: this.auth.user()?.email ?? '',
      items: this.cart.items().map((i) => ({
        productId: i.productId,
        name: i.product.name,
        size: i.size,
        qty: i.qty,
        price: i.product.price,
        swatch: i.product.swatch,
      })),
      subtotal: this.cart.subtotal(),
      discount: this.cart.discount(),
      shipping: this.cart.shipping(),
      total: this.cart.total(),
      payment,
      shippingInfo,
      status: 'Confirmado',
    };
    this._all.update((list) => [order, ...list]);
    this.persist();
    this.cart.clear();
    return order;
  }

  private fetch(route: string, done: (orders: Order[]) => void): void {
    if (this._loading()) return;
    this._loading.set(true);
    this._error.set(null);
    this.http
      .get<unknown>(this.url(route))
      .pipe(finalize(() => this._loading.set(false)))
      .subscribe({
        next: (body) => done(toPageResult(body, toOrder).items),
        error: (err) => this._error.set(apiMessage(err, 'No fue posible cargar los pedidos.')),
      });
  }

  private persist(): void {
    if (this.source === 'local') writeStorage(ORDERS_KEY, this._all());
  }

  private url(path: string): string {
    return joinApiUrl(this.api, path);
  }
}
