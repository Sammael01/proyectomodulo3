/**
 * Ambiente de PRODUCCIÓN (ng build / Netlify).
 *
 * Angular sustituye este archivo por environment.development.ts (o environment.proxy.ts)
 * mediante `fileReplacements` en angular.json; los servicios siempre importan este archivo.
 *
 * ⚠️ PENDIENTE (Integrante A): URL pública definitiva del backend desplegado en Apache Tomcat.
 * El dominio `.invalid` está reservado (RFC 2606) y nunca resuelve: cualquier petición fallará
 * de forma visible hasta que se reemplace por la URL real, p. ej. https://<host>/<contexto>/api
 */
const PENDING_BACKEND_API_URL = 'https://backend-pendiente.invalid/api';

export const environment = {
  production: true,
  apiUrl: PENDING_BACKEND_API_URL,
  /**
   * Clave pública de YouTube Data API (opcional). Solo debe usarse una clave de navegador
   * restringida por dominio (HTTP referrer de Netlify) y por API (YouTube Data API v3).
   * Nunca colocar aquí tokens privados (TMDB, JWT secret, etc.).
   */
  youtubeApiKey: '',
  youtubeQuery: 'streetwear lookbook outfit',
};
