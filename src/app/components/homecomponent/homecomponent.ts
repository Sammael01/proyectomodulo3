import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CatalogService } from '../../core/catalog.service';
import { Productcardcomponent } from '../productcardcomponent/productcardcomponent';

@Component({
  selector: 'app-homecomponent',
  imports: [RouterLink, Productcardcomponent],
  templateUrl: './homecomponent.html',
  styleUrl: './homecomponent.css',
})
export class Homecomponent {
  private readonly catalog = inject(CatalogService);

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

  protected readonly featured = computed(() => this.catalog.products().filter((p) => p.isNew).slice(0, 4));
  protected readonly categories = computed(() =>
    this.catalog.categories().map((name) => ({
      name,
      count: this.catalog.products().filter((p) => p.category === name).length,
    }))
  );
}
