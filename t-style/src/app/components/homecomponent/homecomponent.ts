import { Component, signal } from '@angular/core';

@Component({
  selector: 'app-homecomponent',
  templateUrl: './homecomponent.html',
  styleUrl: './homecomponent.css',
})
export class Homecomponent {
  protected readonly eyebrow = signal('Colección Otoño 2026');
  protected readonly titleLines = signal([
    { text: 'Viste', accent: false },
    { text: 'Sin', accent: true },
    { text: 'Límites', accent: false },
  ]);
  protected readonly description = signal(
    'Prendas urbanas diseñadas para quienes se mueven distinto. Cortes audaces, materiales resistentes y una actitud que no pasa desapercibida.'
  );
  protected readonly cta = signal('Explorar colección');
  protected readonly tag = signal('Nuevo');
}
