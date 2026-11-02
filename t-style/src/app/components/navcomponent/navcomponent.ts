import { Component, signal } from '@angular/core';

@Component({
  selector: 'app-navcomponent',
  templateUrl: './navcomponent.html',
  styleUrl: './navcomponent.css',
})
export class Navcomponent {
  protected readonly brand = signal('T-Style');
  protected readonly announcement = signal('Envío gratis en compras superiores a $150.000 · Nueva colección disponible');
  protected readonly links = signal(['Inicio', 'Colección', 'Novedades', 'Contacto']);
  protected readonly socials = signal([
    { icon: 'bi-instagram', label: 'Instagram' },
    { icon: 'bi-tiktok', label: 'TikTok' },
    { icon: 'bi-bag', label: 'Carrito' },
  ]);
}
