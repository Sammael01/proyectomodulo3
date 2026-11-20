import { Injectable, computed, inject, signal } from '@angular/core';
import { AuthService } from './auth.service';
import { CartService } from './cart.service';
import { Order, PaymentMethod, ShippingInfo } from './models';
import { readStorage, writeStorage } from './storage';

const ORDERS_KEY = 'tstyle_orders';

@Injectable({ providedIn: 'root' })
export class OrderService {
  private readonly auth = inject(AuthService);
  private readonly cart = inject(CartService);
  private readonly _orders = signal<Order[]>(readStorage(ORDERS_KEY, []));

  readonly all = this._orders.asReadonly();
  readonly mine = computed(() => {
    const email = this.auth.user()?.email;
    return email ? this._orders().filter((o) => o.userEmail === email) : [];
  });

  place(shippingInfo: ShippingInfo, payment: PaymentMethod): Order {
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
    this._orders.update((list) => [order, ...list]);
    this.persist();
    this.cart.clear();
    return order;
  }

  setStatus(id: string, status: Order['status']): void {
    this._orders.update((list) => list.map((o) => (o.id === id ? { ...o, status } : o)));
    this.persist();
  }

  private persist(): void {
    writeStorage(ORDERS_KEY, this._orders());
  }
}
