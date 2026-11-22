import { Component, computed, effect, inject, input, linkedSignal, signal, untracked } from '@angular/core';
import { apiMessage } from '../../core/api-error';
import { Router, RouterLink } from '@angular/router';
import { CartService } from '../../core/cart.service';
import { CatalogService } from '../../core/catalog.service';
import { MoneyPipe } from '../../core/money.pipe';
import { ToastService } from '../../core/toast.service';
import { Productcardcomponent } from '../productcardcomponent/productcardcomponent';

@Component({
  selector: 'app-productcomponent',
  imports: [RouterLink, MoneyPipe, Productcardcomponent],
  templateUrl: './productcomponent.html',
  styleUrl: './productcomponent.css',
})
export class Productcomponent {
  protected readonly catalog = inject(CatalogService);
  private readonly cart = inject(CartService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);

  readonly id = input.required<string>();

  protected readonly product = computed(() => this.catalog.byId(this.id()));
  protected readonly related = computed(() => {
    const p = this.product();
    return p ? this.catalog.related(p) : [];
  });
  protected readonly size = linkedSignal(() => {
    const p = this.product();
    return p && p.sizes.length === 1 ? p.sizes[0] : '';
  });
  protected readonly qty = linkedSignal({ source: this.id, computation: () => 1 });
  protected readonly sizeError = signal(0);
  protected readonly loadError = signal<string | null>(null);

  constructor() {
    // Modo API: si se entra directamente a /producto/:id, se consulta el producto al backend.
    effect(() => {
      const id = this.id();
      untracked(() => {
        this.loadError.set(null);
        this.catalog.fetchById(id).subscribe({ error: (err) => this.loadError.set(apiMessage(err, 'No fue posible cargar el producto.')) });
      });
    });
  }
  protected readonly savings = computed(() => {
    const p = this.product();
    return p?.oldPrice ? Math.round((1 - p.price / p.oldPrice) * 100) : 0;
  });
  protected readonly maxQty = computed(() => Math.min(this.cart.maxQty, this.product()?.stock ?? 0));

  protected pickSize(size: string): void {
    this.size.set(size);
    this.sizeError.set(0);
  }

  protected changeQty(delta: number): void {
    this.qty.update((q) => Math.max(1, Math.min(this.maxQty(), q + delta)));
  }

  protected add(buyNow = false): void {
    const p = this.product();
    if (!p) return;
    if (!this.size()) {
      this.sizeError.update((n) => n + 1);
      return;
    }
    this.cart.add(p, this.size(), this.qty());
    if (buyNow) {
      this.router.navigateByUrl('/carrito');
      return;
    }
    this.toast.show(`${p.name} (talla ${this.size()}) agregado al carrito`);
  }
}
