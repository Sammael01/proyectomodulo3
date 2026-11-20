import { Component, HostListener, inject, signal } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { filter } from 'rxjs';
import { AuthService } from '../../core/auth.service';
import { CartService } from '../../core/cart.service';
import { CurrencyCode, CurrencyService } from '../../core/currency.service';

@Component({
  selector: 'app-navcomponent',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './navcomponent.html',
  styleUrl: './navcomponent.css',
})
export class Navcomponent {
  protected readonly auth = inject(AuthService);
  protected readonly cart = inject(CartService);
  protected readonly currency = inject(CurrencyService);

  protected readonly brand = signal('T-Style');
  protected readonly announcement = signal('Envío gratis en compras superiores a $150.000 · Nueva colección disponible');
  protected readonly links = signal([
    { label: 'Inicio', path: '/' },
    { label: 'Colección', path: '/coleccion' },
    { label: 'T-Style TV', path: '/videos' },
    { label: 'Contacto', path: '/contacto' },
  ]);
  protected readonly socials = signal([
    { icon: 'bi-instagram', label: 'Instagram', url: 'https://www.instagram.com' },
    { icon: 'bi-tiktok', label: 'TikTok', url: 'https://www.tiktok.com' },
  ]);
  protected readonly menuOpen = signal(false);
  protected readonly userOpen = signal(false);

  constructor() {
    inject(Router)
      .events.pipe(filter((e) => e instanceof NavigationEnd))
      .subscribe(() => {
        this.menuOpen.set(false);
        this.userOpen.set(false);
      });
  }

  @HostListener('document:click', ['$event'])
  protected closeOnOutside(event: MouseEvent): void {
    if (!(event.target as HTMLElement).closest('.ts-user')) this.userOpen.set(false);
  }

  protected setCurrency(code: string): void {
    this.currency.set(code as CurrencyCode);
  }

  protected logout(): void {
    this.userOpen.set(false);
    this.auth.logout();
  }
}
