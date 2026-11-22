import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../core/auth.service';
import { EMAIL_PATTERN } from '../../core/validators';

@Component({
  selector: 'app-contactcomponent',
  imports: [ReactiveFormsModule],
  templateUrl: './contactcomponent.html',
  styleUrl: './contactcomponent.css',
})
export class Contactcomponent {
  private readonly auth = inject(AuthService);
  private readonly fb = inject(FormBuilder).nonNullable;

  protected readonly channels = [
    { icon: 'bi-whatsapp', title: 'WhatsApp', text: '+57 300 000 0000', hint: 'Lunes a sábado, 8:00 a. m. – 8:00 p. m.' },
    { icon: 'bi-envelope', title: 'Correo', text: 'soporte@tstyle.co', hint: 'Respondemos en menos de 24 horas' },
    { icon: 'bi-geo-alt', title: 'Tienda', text: 'Bogotá, Colombia', hint: 'Visítanos con cita previa' },
  ];
  protected readonly faqs = [
    { q: '¿Cuánto tarda el envío?', a: 'Entre 2 y 5 días hábiles en ciudades principales y hasta 8 días en otros municipios.' },
    { q: '¿Cómo hago un cambio?', a: 'Tienes 30 días desde la entrega. La prenda debe estar sin uso y con etiquetas.' },
    { q: '¿Qué medios de pago aceptan?', a: 'Tarjeta crédito o débito, PSE y pago contraentrega.' },
  ];
  protected readonly topics = ['Estado de mi pedido', 'Cambios y devoluciones', 'Tallas y productos', 'Pagos', 'Otro'];

  protected readonly form = this.fb.group({
    name: [this.auth.user()?.name ?? '', [Validators.required, Validators.minLength(2)]],
    email: [this.auth.user()?.email ?? '', [Validators.required, Validators.pattern(EMAIL_PATTERN)]],
    topic: ['', Validators.required],
    message: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(1000)]],
  });
  protected readonly sending = signal(false);
  protected readonly sent = signal(false);
  protected readonly openFaq = signal<number | null>(0);

  protected invalid(name: 'name' | 'email' | 'topic' | 'message'): boolean {
    const c = this.form.controls[name];
    return c.invalid && c.touched;
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.sending.set(true);
    setTimeout(() => {
      this.sending.set(false);
      this.sent.set(true);
    }, 900);
  }

  protected again(): void {
    this.sent.set(false);
    this.form.controls.topic.reset();
    this.form.controls.message.reset();
  }
}
