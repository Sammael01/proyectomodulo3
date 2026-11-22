import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { validToken } from '../../testing/jwt';
import { API_BASE_URL } from './api-config';
import { authInterceptor } from './auth.interceptor';
import { AuthService } from './auth.service';

function setup(apiBase: string, token: string | null = validToken()) {
  localStorage.clear();
  if (token) {
    localStorage.setItem('tstyle_session', JSON.stringify({ token, userId: 1, name: 'Ana Ruiz', email: 'ana@t-style.co', role: 'CLIENT' }));
  }
  TestBed.resetTestingModule();
  TestBed.configureTestingModule({
    providers: [
      provideRouter([]),
      provideHttpClient(withInterceptors([authInterceptor])),
      provideHttpClientTesting(),
      { provide: API_BASE_URL, useValue: apiBase },
    ],
  });
  return {
    http: TestBed.inject(HttpClient),
    ctrl: TestBed.inject(HttpTestingController),
    auth: TestBed.inject(AuthService),
    router: TestBed.inject(Router),
  };
}

describe('authInterceptor', () => {
  afterEach(() => localStorage.clear());

  it('envía el JWT con URL base relativa (/api)', () => {
    const { http, ctrl } = setup('/api');
    http.get('/api/auth/me').subscribe();
    const req = ctrl.expectOne('/api/auth/me');
    expect(req.request.headers.get('Authorization')).toMatch(/^Bearer /);
    req.flush({});
    ctrl.verify();
  });

  it('envía el JWT con URL base absoluta (Tomcat)', () => {
    const { http, ctrl } = setup('https://backend.example.test/tstyle/api');
    http.get('https://backend.example.test/tstyle/api/auth/me').subscribe();
    const req = ctrl.expectOne('https://backend.example.test/tstyle/api/auth/me');
    expect(req.request.headers.get('Authorization')).toMatch(/^Bearer /);
    req.flush({});
    ctrl.verify();
  });

  it('envía el JWT a http://localhost:8080/api (desarrollo)', () => {
    const { http, ctrl } = setup('http://localhost:8080/api');
    http.get('http://localhost:8080/api/auth/me').subscribe();
    expect(ctrl.expectOne('http://localhost:8080/api/auth/me').request.headers.has('Authorization')).toBe(true);
    ctrl.verify();
  });

  it('no envía el JWT a YouTube ni a otros servicios externos', () => {
    const { http, ctrl } = setup('https://backend.example.test/api');
    http.get('https://www.googleapis.com/youtube/v3/search').subscribe();
    http.get('https://backend.example.test.evil.com/api/x').subscribe();
    http.get('/api/relativa-en-netlify').subscribe();
    for (const url of ['https://www.googleapis.com/youtube/v3/search', 'https://backend.example.test.evil.com/api/x', '/api/relativa-en-netlify']) {
      expect(ctrl.expectOne(url).request.headers.has('Authorization')).toBe(false);
    }
    ctrl.verify();
  });

  it('ante 401 de una petición autenticada cierra sesión y redirige al login', () => {
    const { http, ctrl, auth, router } = setup('/api');
    const navigate = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    http.get('/api/auth/me').subscribe({ error: () => {} });
    ctrl.expectOne('/api/auth/me').flush({ message: 'Token expirado' }, { status: 401, statusText: 'Unauthorized' });
    expect(auth.isLoggedIn()).toBe(false);
    expect(localStorage.getItem('tstyle_session')).toBeNull();
    expect(navigate).toHaveBeenCalledWith(['/login'], expect.objectContaining({ queryParams: expect.objectContaining({ expired: 1 }) }));
  });

  it('un 401 sin sesión (credenciales inválidas en login) no redirige', () => {
    const { http, ctrl, router } = setup('/api', null);
    const navigate = vi.spyOn(router, 'navigate');
    http.post('/api/auth/login', {}).subscribe({ error: () => {} });
    const req = ctrl.expectOne('/api/auth/login');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({ message: 'Credenciales inválidas' }, { status: 401, statusText: 'Unauthorized' });
    expect(navigate).not.toHaveBeenCalled();
  });
});
