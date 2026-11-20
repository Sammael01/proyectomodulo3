import { Injectable, computed, effect, inject, signal } from '@angular/core';
import { CatalogService } from './catalog.service';
import { CartItem, CartLine, Product } from './models';
import { readStorage, writeStorage } from './storage';

const CART_KEY = 'tstyle_cart';
const COUPON_KEY = 'tstyle_coupon';
const FREE_SHIPPING_FROM = 150000;
const SHIPPING_COST = 12000;
const MAX_QTY = 10;
const COUPONS: Record<string, number> = { TSTYLE10: 0.1, OTONO26: 0.15 };

@Injectable({ providedIn: 'root' })
export class CartService {
  private readonly catalog = inject(CatalogService);
  private readonly lines = signal<CartLine[]>(readStorage(CART_KEY, []));
  private readonly _coupon = signal<string | null>(readStorage(COUPON_KEY, null));

  readonly coupon = this._coupon.asReadonly();
  readonly freeShippingFrom = FREE_SHIPPING_FROM;
  readonly maxQty = MAX_QTY;

  readonly items = computed<CartItem[]>(() =>
    this.lines()
      .map((line) => ({ ...line, product: this.catalog.byId(line.productId) as Product }))
      .filter((item) => !!item.product)
  );
  readonly count = computed(() => this.items().reduce((n, i) => n + i.qty, 0));
  readonly subtotal = computed(() => this.items().reduce((s, i) => s + i.qty * i.product.price, 0));
  readonly discount = computed(() => {
    const code = this._coupon();
    return code ? Math.round(this.subtotal() * COUPONS[code]) : 0;
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
  }

  add(product: Product, size = product.sizes[0], qty = 1): void {
    this.lines.update((lines) => {
      const found = lines.find((l) => l.productId === product.id && l.size === size);
      return found
        ? lines.map((l) => (l === found ? { ...l, qty: Math.min(MAX_QTY, l.qty + qty) } : l))
        : [...lines, { productId: product.id, size, qty: Math.min(MAX_QTY, qty) }];
    });
  }

  setQty(item: CartItem, qty: number): void {
    if (qty < 1) return this.remove(item);
    this.lines.update((lines) =>
      lines.map((l) => (this.same(l, item) ? { ...l, qty: Math.min(qty, MAX_QTY) } : l))
    );
  }

  setSize(item: CartItem, size: string): void {
    this.lines.update((lines) => {
      const target = lines.find((l) => l.productId === item.productId && l.size === size);
      if (!target) return lines.map((l) => (this.same(l, item) ? { ...l, size } : l));
      return lines
        .filter((l) => !this.same(l, item))
        .map((l) => (l === target ? { ...l, qty: Math.min(MAX_QTY, l.qty + item.qty) } : l));
    });
  }

  remove(item: CartItem): void {
    this.lines.update((lines) => lines.filter((l) => !this.same(l, item)));
  }

  clear(): void {
    this.lines.set([]);
    this._coupon.set(null);
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

  private same(a: CartLine, b: CartLine): boolean {
    return a.productId === b.productId && a.size === b.size;
  }
}
