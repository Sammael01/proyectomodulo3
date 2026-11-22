import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree, provideRouter } from '@angular/router';
import { Observable, firstValueFrom, isObservable } from 'rxjs';
import { expiredToken, validToken } from '../../testing/jwt';
import { API_BASE_URL } from './api-config';
import { AuthService, tokenExpired } from './auth.service';
import { adminGuard } from './guards';

function configure(session?: unknown) {
  localStorage.clear();
  if (session !== undefined) localStorage.setItem('tstyle_session', JSON.stringify(session));
  TestBed.resetTestingModule();
  TestBed.configureTestingModule({
    providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting(), { provide: API_BASE_URL, useValue: '/api' }],
  });
}

const session = (token: string, role = 'CLIENT') => ({ token, userId: 7, name: 'Ana Ruiz', email: 'ana@t-style.co', role });

describe('tokenExpired', () => {
  it('distingue tokens vigentes, expirados e ilegibles', () => {
    expect(tokenExpired(validToken())).toBe(false);
    expect(tokenExpired(expiredToken())).toBe(true);
    expect(tokenExpired('no-es-un-jwt')).toBe(true);
  });
});

describe('AuthService', () => {
  afterEach(() => localStorage.clear());

  it('recupera la sesión al recargar si el token sigue vigente', () => {
    configure(session(validToken()));
    const auth = TestBed.inject(AuthService);
    expect(auth.isLoggedIn()).toBe(true);
    expect(auth.firstName()).toBe('Ana');
  });

  it('descarta sesiones expiradas o con estructura inválida', () => {
    configure(session(expiredToken()));
    expect(TestBed.inject(AuthService).isLoggedIn()).toBe(false);
    configure({ token: validToken() });
    expect(TestBed.inject(AuthService).isLoggedIn()).toBe(false);
    expect(localStorage.getItem('tstyle_session')).toBeNull();
  });

  it('login guarda la respuesta del backend (token, userId, name, email, role)', () => {
    configure();
    const auth = TestBed.inject(AuthService);
    const ctrl = TestBed.inject(HttpTestingController);
    auth.login({ email: 'ana@t-style.co', password: 'x' }).subscribe();
    const req = ctrl.expectOne('/api/auth/login');
    expect(req.request.method).toBe('POST');
    req.flush(session(validToken()));
    expect(auth.user()?.userId).toBe(7);
    expect(JSON.parse(localStorage.getItem('tstyle_session')!).email).toBe('ana@t-style.co');
  });

  it('logout limpia la sesión', () => {
    configure(session(validToken()));
    const auth = TestBed.inject(AuthService);
    auth.logout(false);
    expect(auth.isLoggedIn()).toBe(false);
    expect(localStorage.getItem('tstyle_session')).toBeNull();
  });
});

describe('adminGuard', () => {
  afterEach(() => localStorage.clear());

  const run = () =>
    TestBed.runInInjectionContext(() => adminGuard({} as ActivatedRouteSnapshot, { url: '/admin' } as RouterStateSnapshot));

  it('no confía en un rol ADMIN manipulado en localStorage: lo confirma con /auth/me', async () => {
    configure(session(validToken(), 'ADMIN'));
    const result = run();
    expect(isObservable(result)).toBe(true);
    const pending = firstValueFrom(result as Observable<boolean | UrlTree>);
    TestBed.inject(HttpTestingController).expectOne('/api/auth/me').flush({ userId: 7, name: 'Ana Ruiz', email: 'ana@t-style.co', role: 'CLIENT' });
    const value = await pending;
    expect(value instanceof UrlTree && value.toString()).toBe('/');
  });

  it('permite el acceso cuando el backend confirma el rol ADMIN', async () => {
    configure(session(validToken(), 'ADMIN'));
    const pending = firstValueFrom(run() as Observable<boolean | UrlTree>);
    TestBed.inject(HttpTestingController).expectOne('/api/auth/me').flush({ userId: 1, name: 'Admin', email: 'a@t-style.co', role: 'ADMIN' });
    expect(await pending).toBe(true);
  });

  it('redirige al login sin sesión', () => {
    configure();
    const value = run();
    expect(value instanceof UrlTree && value.toString()).toContain('/login');
  });
});
