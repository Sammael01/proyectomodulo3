import { HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { AuthService } from './auth.service';
import { CartService } from './cart.service';

/**
 * Los guards solo mejoran la navegación: la autorización definitiva la hace Spring Security.
 * Cualquier dato del navegador (localStorage, rol) puede manipularse.
 */
export const authGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  return auth.token()
    ? true
    : inject(Router).createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
};

export const guestGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.token() ? inject(Router).createUrlTree(['/perfil']) : true;
};

/**
 * No confía en el rol almacenado en el navegador: confirma el rol con el backend (`/auth/me`)
 * antes de abrir el panel. Si el backend no responde o el rol no es ADMIN, se niega el acceso.
 */
export const adminGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const toLogin = router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
  if (!auth.token()) return toLogin;
  return auth.me().pipe(
    map((profile) => (profile.role === 'ADMIN' ? true : router.createUrlTree(['/']))),
    catchError((error: unknown) =>
      of(error instanceof HttpErrorResponse && error.status === 401 ? toLogin : router.createUrlTree(['/']))
    )
  );
};

export const cartNotEmptyGuard: CanActivateFn = () => {
  return inject(CartService).items().length > 0 ? true : inject(Router).createUrlTree(['/carrito']);
};
