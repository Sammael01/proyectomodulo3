import { Component, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CartService } from '../../core/cart.service';
import { Product } from '../../core/models';
import { MoneyPipe } from '../../core/money.pipe';
import { ToastService } from '../../core/toast.service';

@Component({
  selector: 'app-productcardcomponent',
  imports: [RouterLink, MoneyPipe],
  templateUrl: './productcardcomponent.html',
  styleUrl: './productcardcomponent.css',
})
export class Productcardcomponent {
  private readonly cart = inject(CartService);
  private readonly toast = inject(ToastService);

  readonly product = input.required<Product>();
  protected readonly pulse = signal(false);

  protected quickAdd(): void {
    const p = this.product();
    if (p.stock <= 0) return;
    this.cart.add(p);
    this.pulse.set(true);
    this.toast.show(`${p.name} (talla ${p.sizes[0]}) agregado al carrito`);
  }
}
