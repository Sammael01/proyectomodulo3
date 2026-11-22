import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { Observable, finalize } from 'rxjs';
import { DatePipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { apiMessage } from '../../core/api-error';
import { AuthService } from '../../core/auth.service';
import { CatalogService, ProductDraft } from '../../core/catalog.service';
import { CurrencyService } from '../../core/currency.service';
import { Order, Product } from '../../core/models';
import { MoneyPipe } from '../../core/money.pipe';
import { OrderService } from '../../core/order.service';
import { ToastService } from '../../core/toast.service';

@Component({
  selector: 'app-admincomponent',
  imports: [ReactiveFormsModule, RouterLink, DatePipe, MoneyPipe],
  templateUrl: './admincomponent.html',
  styleUrl: './admincomponent.css',
})
/**
 * Panel de administración. Ocultar botones o proteger la ruta no es seguridad:
 * cada operación debe ser autorizada por el backend con el rol ADMIN.
 */
export class Admincomponent implements OnInit {
  protected readonly auth = inject(AuthService);
  protected readonly catalog = inject(CatalogService);
  protected readonly orders = inject(OrderService);
  private readonly toast = inject(ToastService);
  private readonly currency = inject(CurrencyService);
  private readonly fb = inject(FormBuilder).nonNullable;

  protected readonly tab = signal<'products' | 'orders'>('products');
  protected readonly editing = signal<string | null>(null);
  protected readonly formOpen = signal(false);
  protected readonly filter = signal('');
  protected readonly saving = signal(false);
  protected readonly actionError = signal<string | null>(null);
  protected readonly statuses: Order['status'][] = ['Confirmado', 'En preparación', 'Enviado', 'Entregado'];

  protected readonly form = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(3)]],
    category: ['', Validators.required],
    price: [0, [Validators.required, Validators.min(1000)]],
    oldPrice: [0, Validators.min(0)],
    color: ['', Validators.required],
    swatch: ['#e8f20c', Validators.required],
    sizes: ['S, M, L', Validators.required],
    stock: [10, [Validators.required, Validators.min(0)]],
    isNew: [true],
    description: ['', [Validators.required, Validators.minLength(10)]],
  });

  protected readonly products = computed(() => {
    const q = this.filter().trim().toLowerCase();
    return this.catalog.products().filter((p) => !q || `${p.name} ${p.category}`.toLowerCase().includes(q));
  });
  protected readonly stats = computed(() => {
    const all = this.orders.all();
    return {
      products: this.catalog.products().length,
      lowStock: this.catalog.products().filter((p) => p.stock <= 10).length,
      orders: all.length,
      revenue: all.reduce((s, o) => s + o.total, 0),
    };
  });

  protected readonly statCards = computed(() => {
    const s = this.stats();
    return [
      { label: 'Productos', value: String(s.products), tone: '' },
      { label: 'Stock bajo', value: String(s.lowStock), tone: 'text-o' },
      { label: 'Pedidos', value: String(s.orders), tone: '' },
      { label: 'Ventas', value: this.currency.format(s.revenue), tone: 'text-y' },
    ];
  });

  ngOnInit(): void {
    // En modo local no hacen nada; en modo API consultan datos reales.
    this.catalog.load();
    this.orders.loadAll();
  }

  protected newProduct(): void {
    this.editing.set(null);
    this.form.reset();
    this.formOpen.set(true);
  }

  protected edit(p: Product): void {
    this.editing.set(p.id);
    this.form.setValue({
      name: p.name,
      category: p.category,
      price: p.price,
      oldPrice: p.oldPrice ?? 0,
      color: p.color,
      swatch: p.swatch,
      sizes: p.sizes.join(', '),
      stock: p.stock,
      isNew: p.isNew,
      description: p.description,
    });
    this.formOpen.set(true);
  }

  protected cancel(): void {
    this.formOpen.set(false);
    this.editing.set(null);
  }

  protected save(): void {
    if (this.form.invalid || this.saving()) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const draft: ProductDraft = {
      name: v.name.trim(),
      category: v.category.trim(),
      price: Math.round(v.price),
      oldPrice: v.oldPrice > v.price ? Math.round(v.oldPrice) : undefined,
      color: v.color.trim(),
      swatch: v.swatch,
      sizes: v.sizes.split(',').map((s) => s.trim()).filter(Boolean),
      stock: Math.max(0, Math.round(v.stock)),
      isNew: v.isNew,
      description: v.description.trim(),
    };
    const id = this.editing();
    this.run(id ? this.catalog.update({ ...draft, id }) : this.catalog.create(draft), () => {
      this.toast.show(id ? 'Producto actualizado' : 'Producto creado');
      this.cancel();
    });
  }

  protected remove(p: Product): void {
    if (this.saving() || !confirm(`¿Eliminar "${p.name}" del catálogo?`)) return;
    this.run(this.catalog.delete(p.id), () => this.toast.show('Producto eliminado'));
  }

  protected resetCatalog(): void {
    if (this.catalog.source !== 'local') return;
    if (!confirm('¿Restaurar el catálogo original? Se perderán los cambios.')) return;
    this.catalog.reset();
    this.toast.show('Catálogo restaurado');
  }

  protected setStatus(id: string, status: string): void {
    if (this.saving()) return;
    this.run(this.orders.updateStatus(id, status as Order['status']), () => this.toast.show(`Pedido ${id}: ${status}`));
  }

  /** Ejecuta una operación de escritura con estado de carga, bloqueo de duplicados y error real del backend. */
  private run<T>(operation: Observable<T>, done: () => void): void {
    this.saving.set(true);
    this.actionError.set(null);
    operation.pipe(finalize(() => this.saving.set(false))).subscribe({
      next: () => done(),
      error: (err) => this.actionError.set(apiMessage(err, 'No fue posible completar la operación.')),
    });
  }

  protected invalid(name: keyof typeof this.form.controls): boolean {
    const c = this.form.controls[name];
    return c.invalid && c.touched;
  }
}
