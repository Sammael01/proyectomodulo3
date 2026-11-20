import { Component, OnInit, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { apiMessage } from '../../core/api-error';
import { AuthService } from '../../core/auth.service';
import { ToastService } from '../../core/toast.service';
import { EMAIL_PATTERN } from '../../core/validators';

@Component({
  selector: 'app-logincomponent',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './logincomponent.html',
})
export class Logincomponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);
  private readonly fb = inject(FormBuilder).nonNullable;

  readonly returnUrl = input<string>();
  readonly email = input<string>();
  readonly verified = input<string>();
  readonly reset = input<string>();
  readonly expired = input<string>();

  protected readonly form = this.fb.group({
    email: ['', [Validators.required, Validators.pattern(EMAIL_PATTERN), Validators.maxLength(150)]],
    password: ['', [Validators.required, Validators.maxLength(72)]],
  });
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly needsVerification = signal(false);
  protected readonly showPassword = signal(false);

  ngOnInit(): void {
    if (this.email()) this.form.controls.email.setValue(this.email()!);
  }

  protected invalid(name: 'email' | 'password'): boolean {
    const c = this.form.controls[name];
    return c.invalid && c.touched;
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.error.set(null);
    this.needsVerification.set(false);
    const { email, password } = this.form.getRawValue();
    this.auth.login({ email: email.trim().toLowerCase(), password }).subscribe({
      next: (res) => {
        this.loading.set(false);
        this.toast.show(`Bienvenido, ${res.name.split(' ')[0]}`);
        const target = this.returnUrl() || (res.role === 'ADMIN' ? '/admin' : '/');
        this.router.navigateByUrl(target);
      },
      error: (err) => {
        this.loading.set(false);
        const message = apiMessage(err);
        this.error.set(message);
        this.needsVerification.set(/verificar/i.test(message));
      },
    });
  }
}
