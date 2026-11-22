import { Component, signal } from '@angular/core';

@Component({
  selector: 'app-foodercomponent',
  templateUrl: './foodercomponent.html',
  styleUrl: './foodercomponent.css',
})
export class Foodercomponent {
  protected readonly features = signal([
    { icon: 'bi-truck', text: 'Envíos a todo el país', accent: false },
    { icon: 'bi-arrow-repeat', text: 'Cambios en 30 días', accent: true },
    { icon: 'bi-shield-check', text: 'Pago 100% seguro', accent: false },
    { icon: 'bi-headset', text: 'Soporte 24/7', accent: true },
  ]);
  protected readonly year = new Date().getFullYear();
}
