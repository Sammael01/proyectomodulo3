import { InjectionToken } from '@angular/core';
import { environment } from '../../environments/environment';

/** URL base del backend (relativa `/api` o absoluta `https://host/api`). */
export const API_BASE_URL = new InjectionToken<string>('API_BASE_URL', {
  providedIn: 'root',
  factory: () => environment.apiUrl,
});

function trimSlashes(value: string): string {
  return value.replace(/\/+$/, '');
}

/** Une la URL base con una ruta de endpoint sin duplicar ni perder barras. */
export function joinApiUrl(base: string, path: string): string {
  return `${trimSlashes(base)}/${path.replace(/^\/+/, '')}`;
}

function currentOrigin(): string {
  if (typeof document !== 'undefined' && document.baseURI) return document.baseURI;
  if (typeof location !== 'undefined') return location.href;
  return 'http://localhost/';
}

/**
 * Indica si `url` apunta al backend configurado en `apiBase`.
 * Resuelve ambas URLs contra el origen actual, de modo que funciona con bases relativas
 * (`/api`, proxy local) y absolutas (`http://localhost:8080/api`, Tomcat en producción).
 * Compara origen exacto y prefijo de ruta con límite de segmento, por lo que
 * `https://api.host.com/api` NO coincide con `https://api.host.com.evil.com/api`
 * ni con `https://api.host.com/api-otro`.
 */
export function isBackendUrl(url: string, apiBase: string, origin: string = currentOrigin()): boolean {
  let target: URL;
  let base: URL;
  try {
    target = new URL(url, origin);
    base = new URL(apiBase, origin);
  } catch {
    return false;
  }
  if (target.origin !== base.origin) return false;
  const basePath = trimSlashes(base.pathname);
  return basePath === '' || target.pathname === basePath || target.pathname.startsWith(basePath + '/');
}
