import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-notfoundcomponent',
  imports: [RouterLink],
  template: `
    <section class="ts-auth text-center">
      <div class="animate__animated animate__fadeIn">
        <div class="ts-404 font-display animate__animated animate__tada animate__delay-1s">404</div>
        <h1 class="ts-heading mb-2">Página no encontrada</h1>
        <p class="text-body-secondary mb-4">La página que buscas no existe o fue movida.</p>
        <div class="d-flex gap-3 justify-content-center flex-wrap">
          <a routerLink="/" class="btn ts-btn">Ir al inicio</a>
          <a routerLink="/coleccion" class="btn ts-btn-outline">Ver colección</a>
        </div>
      </div>
    </section>
  `,
  styles: `
    .ts-404 {
      font-size: clamp(6rem, 18vw, 11rem);
      line-height: 1;
      color: var(--ts-yellow);
      text-shadow: 8px 8px 0 var(--ts-orange);
    }
  `,
})
export class Notfoundcomponent {}
