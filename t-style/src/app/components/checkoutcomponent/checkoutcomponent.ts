import { Component, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { CartService } from '../../core/cart.service';
import { MoneyPipe } from '../../core/money.pipe';
import { Order, PaymentMethod } from '../../core/models';
import { OrderService } from '../../core/order.service';

function cardNumber(control: AbstractControl): ValidationErrors | null {
  const digits = String(control.value ?? '').replace(/\s/g, '');
  if (!/^\d{13,19}$/.test(digits)) return { card: true };
  let sum = 0;
  for (let i = 0; i < digits.length; i++) {
    let d = Number(digits[digits.length - 1 - i]);
    if (i % 2 === 1) {
      d *= 2;
      if (d > 9) d -= 9;
    }
    sum += d;
  }
  return sum % 10 === 0 ? null : { card: true };
}

function cardExpiry(control: AbstractControl): ValidationErrors | null {
  const match = /^(0[1-9]|1[0-2])\/(\d{2})$/.exec(String(control.value ?? ''));
  if (!match) return { expiry: true };
  const end = new Date(2000 + Number(match[2]), Number(match[1]), 1);
  return end > new Date() ? null : { expiry: true };
}

@Component({
  selector: 'app-checkoutcomponent',
  imports: [ReactiveFormsModule, RouterLink, MoneyPipe],
  templateUrl: './checkoutcomponent.html',
  styleUrl: './checkoutcomponent.css',
})
export class Checkoutcomponent {
  protected readonly cart = inject(CartService);
  private readonly auth = inject(AuthService);
  private readonly orders = inject(OrderService);
  private readonly fb = inject(FormBuilder).nonNullable;

  protected readonly departments = [
    'Amazonas', 'Antioquia', 'Arauca', 'Atlántico', 'Bogotá D.C.', 'Bolívar', 'Boyacá', 'Caldas', 'Caquetá', 'Casanare',
    'Cauca', 'Cesar', 'Chocó', 'Córdoba', 'Cundinamarca', 'Guainía', 'Guaviare', 'Huila', 'La Guajira', 'Magdalena', 'Meta',
    'Nariño', 'Norte de Santander', 'Putumayo', 'Quindío', 'Risaralda', 'San Andrés', 'Santander', 'Sucre', 'Tolima',
    'Valle del Cauca', 'Vaupés', 'Vichada',
  ];
  protected readonly payments: { key: PaymentMethod; label: string; icon: string; hint: string }[] = [
    { key: 'card', label: 'Tarjeta', icon: 'bi-credit-card', hint: 'Crédito o débito' },
    { key: 'pse', label: 'PSE', icon: 'bi-bank', hint: 'Débito desde tu banco' },
    { key: 'cod', label: 'Contraentrega', icon: 'bi-cash-coin', hint: 'Pagas al recibir' },
  ];

  protected readonly form = this.fb.group({
    fullName: [this.auth.user()?.name ?? '', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
    phone: ['', [Validators.required, Validators.pattern(/^3\d{9}$/)]],
    address: ['', [Validators.required, Validators.minLength(6)]],
    city: ['', Validators.required],
    department: ['', Validators.required],
    notes: [''],
    payment: ['card' as PaymentMethod],
    cardName: [''],
    cardNumber: [''],
    cardExpiry: [''],
    cardCvv: [''],
    bank: [''],
  });

  protected readonly submitted = signal(false);
  protected readonly processing = signal(false);
  protected readonly order = signal<Order | null>(null);

  constructor() {
    this.updatePaymentValidators('card');
    this.form.controls.payment.valueChanges.subscribe((method) => this.updatePaymentValidators(method));
  }

  protected invalid(name: keyof typeof this.form.controls): boolean {
    const control = this.form.controls[name];
    return control.invalid && (control.touched || this.submitted());
  }

  protected formatCard(value: string): void {
    const digits = value.replace(/\D/g, '').slice(0, 19);
    this.form.controls.cardNumber.setValue(digits.replace(/(.{4})/g, '$1 ').trim());
  }

  protected formatExpiry(value: string): void {
    const digits = value.replace(/\D/g, '').slice(0, 4);
    this.form.controls.cardExpiry.setValue(digits.length > 2 ? `${digits.slice(0, 2)}/${digits.slice(2)}` : digits);
  }

  protected submit(): void {
    this.submitted.set(true);
    if (this.form.invalid || this.processing()) {
      this.form.markAllAsTouched();
      return;
    }
    this.processing.set(true);
    const v = this.form.getRawValue();
    setTimeout(() => {
      const placed = this.orders.place(
        { fullName: v.fullName.trim(), phone: v.phone, address: v.address.trim(), city: v.city.trim(), department: v.department, notes: v.notes.trim() },
        v.payment
      );
      this.order.set(placed);
      this.processing.set(false);
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }, 1200);
  }

  private updatePaymentValidators(method: PaymentMethod): void {
    const c = this.form.controls;
    const card = method === 'card';
    c.cardName.setValidators(card ? [Validators.required, Validators.minLength(3)] : null);
    c.cardNumber.setValidators(card ? [Validators.required, cardNumber] : null);
    c.cardExpiry.setValidators(card ? [Validators.required, cardExpiry] : null);
    c.cardCvv.setValidators(card ? [Validators.required, Validators.pattern(/^\d{3,4}$/)] : null);
    c.bank.setValidators(method === 'pse' ? Validators.required : null);
    [c.cardName, c.cardNumber, c.cardExpiry, c.cardCvv, c.bank].forEach((ctrl) => ctrl.updateValueAndValidity({ emitEvent: false }));
  }
}
