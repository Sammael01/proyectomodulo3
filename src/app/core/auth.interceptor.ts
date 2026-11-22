import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { API_BASE_URL, isBackendUrl } from './api-config';
import { AuthService } from './auth.service';

/**
 * Agrega `Authorization: Bearer <token>` únicamente a peticiones dirigidas al backend
 * (URL base relativa o absoluta). Servicios externos (YouTube, CDNs, etc.) nunca reciben el JWT.
 * Ante un 401 de una petición autenticada, cierra la sesión y redirige al login.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const isApi = isBackendUrl(req.url, inject(API_BASE_URL));
  const token = isApi ? auth.token() : null;
  const request = token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;

  return next(request).pipe(
    catchError((error: unknown) => {
      if (isApi && token && error instanceof HttpErrorResponse && error.status === 401) {
        auth.logout(false);
        const returnUrl = router.url.startsWith('/login') ? undefined : router.url;
        router.navigate(['/login'], { queryParams: { returnUrl, expired: 1 } });
      }
      return throwError(() => error);
    })
  );
};
