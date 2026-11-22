import { isBackendUrl, joinApiUrl } from './api-config';

describe('isBackendUrl', () => {
  const origin = 'https://t-style.netlify.app/';

  it('detecta URL base relativa (/api, proxy local)', () => {
    expect(isBackendUrl('/api/auth/login', '/api', origin)).toBe(true);
    expect(isBackendUrl('/api', '/api', origin)).toBe(true);
    expect(isBackendUrl('https://t-style.netlify.app/api/products', '/api', origin)).toBe(true);
  });

  it('detecta URL base absoluta (localhost:8080 y Tomcat)', () => {
    expect(isBackendUrl('http://localhost:8080/api/auth/me', 'http://localhost:8080/api', origin)).toBe(true);
    expect(isBackendUrl('https://backend.example.test/tstyle/api/auth/login', 'https://backend.example.test/tstyle/api/', origin)).toBe(true);
  });

  it('rechaza servicios externos y dominios o rutas parecidas', () => {
    const api = 'https://backend.example.test/api';
    expect(isBackendUrl('https://www.googleapis.com/youtube/v3/search', api, origin)).toBe(false);
    expect(isBackendUrl('https://backend.example.test.evil.com/api/x', api, origin)).toBe(false);
    expect(isBackendUrl('https://backend.example.test/api-otro/x', api, origin)).toBe(false);
    expect(isBackendUrl('http://backend.example.test/api/x', api, origin)).toBe(false);
    expect(isBackendUrl('/api/auth/login', api, origin)).toBe(false);
    expect(isBackendUrl('/apiary', '/api', origin)).toBe(false);
  });
});

describe('joinApiUrl', () => {
  it('une base y ruta sin duplicar barras', () => {
    expect(joinApiUrl('/api', '/auth/login')).toBe('/api/auth/login');
    expect(joinApiUrl('https://h.test/api/', 'auth/me')).toBe('https://h.test/api/auth/me');
  });
});
