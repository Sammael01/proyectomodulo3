import { Component, computed, inject, input, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { apiMessage } from '../../core/api-error';
import { AuthService } from '../../core/auth.service';
import { ToastService } from '../../core/toast.service';
import { EMAIL_PATTERN, PASSWORD_PATTERN, PASSWORD_RULES, matchFields } from '../../core/validators';

@Component({
  selector: 'app-registercomponent',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './registercomponent.html',
})
export class Registercomponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);
  private readonly fb = inject(FormBuilder).nonNullable;

  readonly returnUrl = input<string>();

  protected readonly form = this.fb.group(
    {
      name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      email: ['', [Validators.required, Validators.pattern(EMAIL_PATTERN), Validators.maxLength(150)]],
      password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72), Validators.pattern(PASSWORD_PATTERN)]],
      confirm: ['', Validators.required],
      terms: [false, Validators.requiredTrue],
    },
    { validators: matchFields('password', 'confirm') }
  );
  private readonly password = toSignal(this.form.controls.password.valueChanges, { initialValue: '' });
  protected readonly rules = computed(() => PASSWORD_RULES.map((r) => ({ label: r.label, ok: r.test(this.password()) })));
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly showPassword = signal(false);

  protected invalid(name: 'name' | 'email' | 'password' | 'confirm' | 'terms'): boolean {
    const c = this.form.controls[name];
    return c.invalid && c.touched;
  }

  protected mismatch(): boolean {
    return this.form.hasError('mismatch') && this.form.controls.confirm.touched;
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.error.set(null);
    const { name, email, password } = this.form.getRawValue();
    const normalizedEmail = email.trim().toLowerCase();
    this.auth.register({ name: name.trim(), email: normalizedEmail, password }).subscribe({
      next: (res) => {
        this.loading.set(false);
        this.toast.show(res.message || 'Cuenta creada. Revisa tu correo.');
        this.router.navigate(['/verificar'], { queryParams: { email: normalizedEmail, returnUrl: this.returnUrl() || null } });
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(apiMessage(err));
      },
    });
  }
}
