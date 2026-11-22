import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { apiMessage } from '../../core/api-error';
import { AuthService } from '../../core/auth.service';
import { EMAIL_PATTERN, PASSWORD_PATTERN, PASSWORD_RULES, matchFields } from '../../core/validators';

@Component({
  selector: 'app-recovercomponent',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './recovercomponent.html',
})
export class Recovercomponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder).nonNullable;

  readonly email = input<string>();

  protected readonly step = signal<1 | 2>(1);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly info = signal<string | null>(null);
  protected readonly showPassword = signal(false);

  protected readonly emailForm = this.fb.group({
    email: ['', [Validators.required, Validators.pattern(EMAIL_PATTERN), Validators.maxLength(150)]],
  });
  protected readonly resetForm = this.fb.group(
    {
      code: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]],
      newPassword: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72), Validators.pattern(PASSWORD_PATTERN)]],
      confirm: ['', Validators.required],
    },
    { validators: matchFields('newPassword', 'confirm') }
  );
  private readonly password = toSignal(this.resetForm.controls.newPassword.valueChanges, { initialValue: '' });
  protected readonly rules = computed(() => PASSWORD_RULES.map((r) => ({ label: r.label, ok: r.test(this.password()) })));

  ngOnInit(): void {
    if (this.email()) this.emailForm.controls.email.setValue(this.email()!);
  }

  protected invalidEmail(): boolean {
    const c = this.emailForm.controls.email;
    return c.invalid && c.touched;
  }

  protected invalid(name: 'code' | 'newPassword' | 'confirm'): boolean {
    const c = this.resetForm.controls[name];
    return c.invalid && c.touched;
  }

  protected mismatch(): boolean {
    return this.resetForm.hasError('mismatch') && this.resetForm.controls.confirm.touched;
  }

  protected onCode(value: string): void {
    this.resetForm.controls.code.setValue(value.replace(/\D/g, '').slice(0, 6));
  }

  protected requestCode(): void {
    if (this.emailForm.invalid) {
      this.emailForm.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.error.set(null);
    this.auth.forgotPassword(this.normalizedEmail()).subscribe({
      next: (res) => {
        this.loading.set(false);
        this.info.set(res.message || 'Si el correo está registrado, recibirás un código de recuperación.');
        this.step.set(2);
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(apiMessage(err));
      },
    });
  }

  protected resetPassword(): void {
    if (this.resetForm.invalid) {
      this.resetForm.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.error.set(null);
    const { code, newPassword } = this.resetForm.getRawValue();
    const email = this.normalizedEmail();
    this.auth.resetPassword({ email, code, newPassword }).subscribe({
      next: () => {
        this.loading.set(false);
        this.router.navigate(['/login'], { queryParams: { email, reset: 1 } });
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(apiMessage(err));
      },
    });
  }

  protected back(): void {
    this.step.set(1);
    this.error.set(null);
    this.info.set(null);
    this.resetForm.reset();
  }

  private normalizedEmail(): string {
    return this.emailForm.controls.email.value.trim().toLowerCase();
  }
}
