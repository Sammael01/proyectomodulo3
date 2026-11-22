import { Component, computed, inject, input, linkedSignal, signal } from '@angular/core';
import { Router } from '@angular/router';
import { CatalogService } from '../../core/catalog.service';
import { Productcardcomponent } from '../productcardcomponent/productcardcomponent';

type SortKey = 'relevance' | 'price-asc' | 'price-desc' | 'name';

@Component({
  selector: 'app-catalogcomponent',
  imports: [Productcardcomponent],
  templateUrl: './catalogcomponent.html',
  styleUrl: './catalogcomponent.css',
})
export class Catalogcomponent {
  protected readonly catalog = inject(CatalogService);
  private readonly router = inject(Router);

  readonly categoria = input<string>();
  readonly nuevos = input<string>();
  readonly q = input<string>();

  protected readonly category = linkedSignal(() => this.categoria() ?? 'Todos');
  protected readonly onlyNew = linkedSignal(() => this.nuevos() === '1');
  protected readonly query = linkedSignal(() => this.q() ?? '');
  protected readonly sort = signal<SortKey>('relevance');
  protected readonly onlySale = signal(false);

  protected readonly categories = computed(() => ['Todos', ...this.catalog.categories()]);
  protected readonly sortOptions: { key: SortKey; label: string }[] = [
    { key: 'relevance', label: 'Relevancia' },
    { key: 'price-asc', label: 'Menor precio' },
    { key: 'price-desc', label: 'Mayor precio' },
    { key: 'name', label: 'Nombre A-Z' },
  ];

  protected readonly results = computed(() => {
    const q = this.query().trim().toLowerCase();
    const list = this.catalog.products().filter(
      (p) =>
        (this.category() === 'Todos' || p.category === this.category()) &&
        (!this.onlyNew() || p.isNew) &&
        (!this.onlySale() || !!p.oldPrice) &&
        (!q || `${p.name} ${p.category} ${p.color}`.toLowerCase().includes(q))
    );
    switch (this.sort()) {
      case 'price-asc':
        return [...list].sort((a, b) => a.price - b.price);
      case 'price-desc':
        return [...list].sort((a, b) => b.price - a.price);
      case 'name':
        return [...list].sort((a, b) => a.name.localeCompare(b.name, 'es'));
      default:
        return list;
    }
  });

  protected setCategory(name: string): void {
    this.category.set(name);
    this.syncUrl();
  }

  protected setQuery(value: string): void {
    this.query.set(value);
    this.syncUrl();
  }

  protected toggleNew(value: boolean): void {
    this.onlyNew.set(value);
    this.syncUrl();
  }

  protected setSort(value: string): void {
    this.sort.set(value as SortKey);
  }

  protected clearFilters(): void {
    this.category.set('Todos');
    this.query.set('');
    this.onlyNew.set(false);
    this.onlySale.set(false);
    this.sort.set('relevance');
    this.syncUrl();
  }

  private syncUrl(): void {
    this.router.navigate([], {
      queryParams: {
        categoria: this.category() === 'Todos' ? null : this.category(),
        nuevos: this.onlyNew() ? 1 : null,
        q: this.query().trim() || null,
      },
      replaceUrl: true,
    });
  }
}
