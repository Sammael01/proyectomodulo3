import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, finalize, map, of, tap, throwError } from 'rxjs';
import { API_BASE_URL, joinApiUrl } from './api-config';
import { API_ENDPOINTS, PendingEndpointError, endpoint } from './api-endpoints';
import { apiMessage } from './api-error';
import { Product } from './models';
import { DataSource, PageResult, toPageResult } from './remote-data';
import { readStorage, writeStorage } from './storage';

const CATALOG_KEY = 'tstyle_catalog';

/** Productos de DEMOSTRACIÓN. Solo se usan mientras `API_ENDPOINTS.catalog.list` sea null. */
const DEFAULT_PRODUCTS: Product[] = [
  { id: 'p1', name: 'Hoodie Oversize Block', category: 'Buzos', price: 159900, color: 'Negro', swatch: '#1d1f1e', sizes: ['S', 'M', 'L', 'XL'], isNew: true, stock: 24,
    description: 'Buzo de algodón perchado de 380 g con capota doble, hombro caído y bolsillo canguro. Corte oversize para un fit relajado.' },
  { id: 'p2', name: 'Cargo Utility Pant', category: 'Pantalones', price: 189900, oldPrice: 219900, color: 'Oliva', swatch: '#5d6812', sizes: ['28', '30', '32', '34', '36'], isNew: false, stock: 18,
    description: 'Pantalón cargo en dril ripstop con seis bolsillos, pretina ajustable y bota con cordón para ajustar el largo.' },
  { id: 'p3', name: 'Tee Boxy Signal', category: 'Camisetas', price: 79900, color: 'Naranja', swatch: '#ff5b22', sizes: ['S', 'M', 'L', 'XL'], isNew: true, stock: 40,
    description: 'Camiseta boxy de algodón peinado de 240 g con cuello acanalado y estampado frontal en tinta al agua.' },
  { id: 'p4', name: 'Chaqueta Shell Neon', category: 'Chaquetas', price: 249900, color: 'Amarillo', swatch: '#e8f20c', sizes: ['S', 'M', 'L', 'XL'], isNew: true, stock: 10,
    description: 'Chaqueta cortavientos repelente al agua con capota oculta en el cuello, cremalleras reflectivas y forro de malla.' },
  { id: 'p5', name: 'Gorra Six Panel', category: 'Accesorios', price: 59900, color: 'Arena', swatch: '#dedcd5', sizes: ['Única'], isNew: false, stock: 35,
    description: 'Gorra de seis paneles en gabardina lavada con logo bordado y correa metálica ajustable.' },
  { id: 'p6', name: 'Jogger Tech Fleece', category: 'Pantalones', price: 169900, color: 'Grafito', swatch: '#3a3c3b', sizes: ['S', 'M', 'L', 'XL'], isNew: false, stock: 22,
    description: 'Jogger en tejido técnico de doble cara, bolsillos con cremallera y puños elásticos en la bota.' },
  { id: 'p7', name: 'Camiseta Long Sleeve Grid', category: 'Camisetas', price: 99900, oldPrice: 119900, color: 'Blanco hueso', swatch: '#efede6', sizes: ['S', 'M', 'L'], isNew: false, stock: 28,
    description: 'Camiseta manga larga con estampado de retícula en las mangas y ajuste regular.' },
  { id: 'p8', name: 'Chaqueta Denim Raw', category: 'Chaquetas', price: 229900, color: 'Índigo', swatch: '#26324f', sizes: ['M', 'L', 'XL'], isNew: true, stock: 12,
    description: 'Chaqueta de jean crudo de 14 oz con botones metálicos, costuras contrastantes y corte cuadrado.' },
  { id: 'p9', name: 'Buzo Crewneck Static', category: 'Buzos', price: 139900, color: 'Gris jaspe', swatch: '#8a8b86', sizes: ['S', 'M', 'L', 'XL'], isNew: false, stock: 30,
    description: 'Buzo cuello redondo en felpa francesa con estampado en relieve y acabado lavado.' },
  { id: 'p10', name: 'Riñonera Crossbody', category: 'Accesorios', price: 89900, color: 'Negro', swatch: '#121313', sizes: ['Única'], isNew: true, stock: 16,
    description: 'Riñonera en nylon balístico con correa ajustable, bolsillo frontal y compartimento interno acolchado.' },
  { id: 'p11', name: 'Short Nylon Track', category: 'Pantalones', price: 99900, color: 'Naranja quemado', swatch: '#c2451a', sizes: ['S', 'M', 'L'], isNew: false, stock: 20,
    description: 'Pantaloneta en nylon liviano con forro interno de malla, pretina elástica y bolsillos laterales.' },
  { id: 'p12', name: 'Medias Pack x3', category: 'Accesorios', price: 39900, color: 'Mixto', swatch: '#b8c40a', sizes: ['Única'], isNew: false, stock: 50,
    description: 'Paquete de tres pares de medias de caña media en algodón con logo tejido.' },
];

/** Datos editables de un producto (sin identificador: en modo API lo asigna el backend). */
export type ProductDraft = Omit<Product, 'id'>;

export interface CatalogQuery {
  q?: string;
  category?: string;
  page?: number;
  size?: number;
}

/**
 * ADAPTADOR PENDIENTE: convierte el DTO de producto del módulo B al modelo `Product`.
 * Mientras no exista el DTO definitivo se asume la misma forma que `Product`;
 * el identificador se normaliza a string (las rutas Angular usan string).
 */
export function toProduct(raw: unknown): Product {
  const dto = raw as Product & { id: string | number };
  return { ...dto, id: String(dto.id), sizes: Array.isArray(dto.sizes) ? dto.sizes : [] };
}

@Injectable({ providedIn: 'root' })
export class CatalogService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(API_BASE_URL);
  private readonly routes = API_ENDPOINTS.catalog;
  private readonly adminRoutes = API_ENDPOINTS.catalogAdmin;

  /** `api` en cuanto B confirme el endpoint de listado; mientras tanto, catálogo de demostración. */
  readonly source: DataSource = this.routes.list ? 'api' : 'local';

  private readonly _products = signal<Product[]>(this.source === 'local' ? readStorage(CATALOG_KEY, DEFAULT_PRODUCTS) : []);
  private readonly _page = signal<PageResult<Product> | null>(null);
  private readonly _loading = signal(false);
  private readonly _error = signal<string | null>(null);

  readonly products = this._products.asReadonly();
  readonly page = this._page.asReadonly();
  readonly loading = this._loading.asReadonly();
  readonly error = this._error.asReadonly();
  readonly categories = computed(() => [...new Set(this._products().map((p) => p.category))]);

  constructor() {
    if (this.source === 'api') this.load();
  }

  byId(id: string): Product | undefined {
    return this._products().find((p) => p.id === id);
  }

  related(product: Product, limit = 4): Product[] {
    const others = this._products().filter((p) => p.id !== product.id);
    const same = others.filter((p) => p.category === product.category);
    return [...same, ...others.filter((p) => p.category !== product.category)].slice(0, limit);
  }

  /** Carga (o recarga) el catálogo desde el backend. En modo local no hace nada. */
  load(query: CatalogQuery = {}): void {
    if (this.source === 'local' || this._loading()) return;
    this._loading.set(true);
    this._error.set(null);
    this.http
      .get<unknown>(this.url(this.routes.list!), { params: this.params(query) })
      .pipe(finalize(() => this._loading.set(false)))
      .subscribe({
        next: (body) => {
          const page = toPageResult(body, toProduct);
          this._page.set(page);
          this._products.set(page.items);
        },
        error: (err) => this._error.set(apiMessage(err, 'No fue posible cargar el catálogo.')),
      });
  }

  /** Consulta un producto por id. En modo API lo pide al backend si no está en memoria. */
  fetchById(id: string): Observable<Product | undefined> {
    const cached = this.byId(id);
    if (this.source === 'local' || cached) return of(cached);
    if (!this.routes.byId) return throwError(() => new PendingEndpointError('catalog.byId'));
    return this.http.get<unknown>(this.url(endpoint(this.routes.byId, { id }))).pipe(
      map(toProduct),
      tap((product) => this.upsert(product))
    );
  }

  /** Búsqueda de productos. En modo local filtra los productos de demostración. */
  search(query: CatalogQuery): Observable<PageResult<Product>> {
    if (this.source === 'local') {
      const q = query.q?.trim().toLowerCase() ?? '';
      const items = this._products().filter(
        (p) => (!q || `${p.name} ${p.category} ${p.color}`.toLowerCase().includes(q)) && (!query.category || p.category === query.category)
      );
      return of({ items, page: 0, size: items.length, totalItems: items.length, totalPages: 1 });
    }
    const route = this.routes.search ?? null;
    if (!route) return throwError(() => new PendingEndpointError('catalog.search'));
    return this.http.get<unknown>(this.url(route), { params: this.params(query) }).pipe(map((body) => toPageResult(body, toProduct)));
  }

  // ---- Administración (en modo API el backend valida el rol ADMIN) ----

  create(draft: ProductDraft): Observable<Product> {
    if (this.source === 'local') {
      const product = { ...draft, id: this.newId() };
      this.save(product);
      return of(product);
    }
    if (!this.adminRoutes.create) return throwError(() => new PendingEndpointError('catalogAdmin.create'));
    return this.http.post<unknown>(this.url(this.adminRoutes.create), draft).pipe(map(toProduct), tap((p) => this.upsert(p)));
  }

  update(product: Product): Observable<Product> {
    if (this.source === 'local') {
      this.save(product);
      return of(product);
    }
    if (!this.adminRoutes.update) return throwError(() => new PendingEndpointError('catalogAdmin.update'));
    const { id, ...draft } = product;
    return this.http
      .put<unknown>(this.url(endpoint(this.adminRoutes.update, { id })), draft)
      .pipe(map(toProduct), tap((p) => this.upsert(p)));
  }

  delete(id: string): Observable<void> {
    if (this.source === 'local') {
      this.remove(id);
      return of(undefined);
    }
    if (!this.adminRoutes.remove) return throwError(() => new PendingEndpointError('catalogAdmin.remove'));
    return this.http
      .delete<void>(this.url(endpoint(this.adminRoutes.remove, { id })))
      .pipe(tap(() => this._products.update((list) => list.filter((p) => p.id !== id))));
  }

  /** Solo modo local: restaura los productos de demostración. */
  reset(): void {
    if (this.source !== 'local') return;
    this._products.set(DEFAULT_PRODUCTS);
    writeStorage(CATALOG_KEY, null);
  }

  private save(product: Product): void {
    this.upsert(product);
    this.persist();
  }

  private remove(id: string): void {
    this._products.update((list) => list.filter((p) => p.id !== id));
    this.persist();
  }

  private upsert(product: Product): void {
    this._products.update((list) =>
      list.some((p) => p.id === product.id) ? list.map((p) => (p.id === product.id ? product : p)) : [...list, product]
    );
  }

  /** Identificador local SOLO para el modo demostración; en modo API lo asigna el backend. */
  private newId(): string {
    return 'p' + Date.now().toString(36);
  }

  private persist(): void {
    if (this.source === 'local') writeStorage(CATALOG_KEY, this._products());
  }

  private url(path: string): string {
    return joinApiUrl(this.api, path);
  }

  /** PENDIENTE: confirmar con B los nombres de los parámetros de búsqueda y paginación. */
  private params(query: CatalogQuery): HttpParams {
    let params = new HttpParams();
    if (query.q) params = params.set('q', query.q);
    if (query.category) params = params.set('category', query.category);
    if (query.page !== undefined) params = params.set('page', query.page);
    if (query.size !== undefined) params = params.set('size', query.size);
    return params;
  }
}
