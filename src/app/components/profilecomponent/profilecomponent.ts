import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { apiMessage } from '../../core/api-error';
import { AuthService } from '../../core/auth.service';
import { MoneyPipe } from '../../core/money.pipe';
import { PaymentMethod, UserProfile } from '../../core/models';
import { OrderService } from '../../core/order.service';

@Component({
  selector: 'app-profilecomponent',
  imports: [RouterLink, DatePipe, MoneyPipe],
  templateUrl: './profilecomponent.html',
  styleUrl: './profilecomponent.css',
})
export class Profilecomponent implements OnInit {
  protected readonly auth = inject(AuthService);
  protected readonly orders = inject(OrderService);

  protected readonly profile = signal<UserProfile | null>(null);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly expanded = signal<string | null>(null);

  protected readonly view = computed(() => this.profile() ?? this.auth.user());
  protected readonly initials = computed(() =>
    (this.view()?.name ?? '')
      .split(' ')
      .filter(Boolean)
      .slice(0, 2)
      .map((w) => w[0].toUpperCase())
      .join('')
  );
  protected readonly totalSpent = computed(() => this.orders.mine().reduce((s, o) => s + o.total, 0));
  protected readonly paymentLabels: Record<PaymentMethod, string> = { card: 'Tarjeta', pse: 'PSE', cod: 'Contraentrega' };

  ngOnInit(): void {
    this.load();
    this.orders.loadMine(); // en modo local no hace nada
  }

  protected load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.auth.me().subscribe({
      next: (profile) => {
        this.profile.set(profile);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(apiMessage(err, 'No fue posible cargar tu perfil.'));
        this.loading.set(false);
      },
    });
  }

  protected toggle(id: string): void {
    this.expanded.update((current) => (current === id ? null : id));
  }
}
