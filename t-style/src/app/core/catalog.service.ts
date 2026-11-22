import { Injectable, computed, signal } from '@angular/core';
import { Product } from './models';
import { readStorage, writeStorage } from './storage';

const CATALOG_KEY = 'tstyle_catalog';

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

@Injectable({ providedIn: 'root' })
export class CatalogService {
  private readonly _products = signal<Product[]>(readStorage(CATALOG_KEY, DEFAULT_PRODUCTS));

  readonly products = this._products.asReadonly();
  readonly categories = computed(() => [...new Set(this._products().map((p) => p.category))]);

  byId(id: string): Product | undefined {
    return this._products().find((p) => p.id === id);
  }

  related(product: Product, limit = 4): Product[] {
    const others = this._products().filter((p) => p.id !== product.id);
    const same = others.filter((p) => p.category === product.category);
    return [...same, ...others.filter((p) => p.category !== product.category)].slice(0, limit);
  }

  save(product: Product): void {
    this._products.update((list) =>
      list.some((p) => p.id === product.id) ? list.map((p) => (p.id === product.id ? product : p)) : [...list, product]
    );
    this.persist();
  }

  remove(id: string): void {
    this._products.update((list) => list.filter((p) => p.id !== id));
    this.persist();
  }

  reset(): void {
    this._products.set(DEFAULT_PRODUCTS);
    writeStorage(CATALOG_KEY, null);
  }

  newId(): string {
    return 'p' + Date.now().toString(36);
  }

  private persist(): void {
    writeStorage(CATALOG_KEY, this._products());
  }
}
