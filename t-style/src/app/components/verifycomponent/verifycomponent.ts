import { Component, DestroyRef, OnInit, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { apiMessage } from '../../core/api-error';
import { AuthService } from '../../core/auth.service';
import { ToastService } from '../../core/toast.service';
import { EMAIL_PATTERN } from '../../core/validators';

@Component({
  selector: 'app-verifycomponent',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './verifycomponent.html',
})
export class Verifycomponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);
  private readonly fb = inject(FormBuilder).nonNullable;

  readonly email = input<string>();
  readonly returnUrl = input<string>();

  protected readonly form = this.fb.group({
    email: ['', [Validators.required, Validators.pattern(EMAIL_PATTERN)]],
    code: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]],
  });
  protected readonly loading = signal(false);
  protected readonly resending = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly info = signal<string | null>(null);
  protected readonly cooldown = signal(0);
  private timer?: ReturnType<typeof setInterval>;

  constructor() {
    inject(DestroyRef).onDestroy(() => clearInterval(this.timer));
  }

  ngOnInit(): void {
    if (this.email()) {
      this.form.controls.email.setValue(this.email()!);
      this.startCooldown();
    }
  }

  protected invalid(name: 'email' | 'code'): boolean {
    const c = this.form.controls[name];
    return c.invalid && c.touched;
  }

  protected onCode(value: string): void {
    this.form.controls.code.setValue(value.replace(/\D/g, '').slice(0, 6));
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.error.set(null);
    const { email, code } = this.form.getRawValue();
    const normalized = email.trim().toLowerCase();
    this.auth.verifyEmail({ email: normalized, code }).subscribe({
      next: () => {
        this.loading.set(false);
        this.toast.show('Correo verificado correctamente');
        this.router.navigate(['/login'], { queryParams: { email: normalized, verified: 1, returnUrl: this.returnUrl() || null } });
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(apiMessage(err));
      },
    });
  }

  protected resend(): void {
    const control = this.form.controls.email;
    if (control.invalid) {
      control.markAsTouched();
      return;
    }
    this.resending.set(true);
    this.error.set(null);
    this.info.set(null);
    this.auth.resendVerification(control.value.trim().toLowerCase()).subscribe({
      next: (res) => {
        this.resending.set(false);
        this.info.set(res.message || 'Te enviamos un nuevo código.');
        this.startCooldown();
      },
      error: (err) => {
        this.resending.set(false);
        this.error.set(apiMessage(err));
      },
    });
  }

  private startCooldown(): void {
    clearInterval(this.timer);
    this.cooldown.set(60);
    this.timer = setInterval(() => {
      this.cooldown.update((s) => s - 1);
      if (this.cooldown() <= 0) clearInterval(this.timer);
    }, 1000);
  }
}
