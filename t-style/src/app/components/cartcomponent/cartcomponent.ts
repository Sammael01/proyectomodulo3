import { Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { CartService } from '../../core/cart.service';
import { CatalogService } from '../../core/catalog.service';
import { CartItem } from '../../core/models';
import { MoneyPipe } from '../../core/money.pipe';
import { ToastService } from '../../core/toast.service';

@Component({
  selector: 'app-cartcomponent',
  imports: [RouterLink, MoneyPipe],
  templateUrl: './cartcomponent.html',
  styleUrl: './cartcomponent.css',
})
export class Cartcomponent {
  protected readonly cart = inject(CartService);
  protected readonly auth = inject(AuthService);
  private readonly catalog = inject(CatalogService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);

  protected readonly couponInput = signal('');
  protected readonly couponError = signal<string | null>(null);
  protected readonly removing = signal<Set<string>>(new Set());

  protected readonly suggestions = computed(() => {
    const ids = new Set(this.cart.items().map((i) => i.productId));
    return this.catalog.products().filter((p) => !ids.has(p.id) && p.stock > 0).slice(0, 3);
  });

  protected applyCoupon(): void {
    if (this.cart.applyCoupon(this.couponInput())) {
      this.couponInput.set('');
      this.couponError.set(null);
      this.toast.show('Cupón aplicado correctamente');
    } else {
      this.couponError.set('El cupón no es válido.');
    }
  }

  protected key(item: CartItem): string {
    return item.productId + '|' + item.size;
  }

  protected startRemove(item: CartItem): void {
    this.removing.update((s) => new Set(s).add(this.key(item)));
  }

  protected finishRemove(item: CartItem, event: AnimationEvent): void {
    if (!event.animationName.includes('fadeOut')) return;
    this.removing.update((s) => {
      const n = new Set(s);
      n.delete(this.key(item));
      return n;
    });
    this.cart.remove(item);
  }

  protected addSuggestion(id: string): void {
    const p = this.catalog.byId(id);
    if (!p) return;
    this.cart.add(p);
    this.toast.show(`${p.name} agregado al carrito`);
  }

  protected checkout(): void {
    this.router.navigateByUrl('/checkout');
  }
}
