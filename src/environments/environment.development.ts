/**
 * Ambiente de DESARROLLO LOCAL (ng serve / npm start).
 * Peticiones directas al backend Spring en localhost:8080.
 * Requiere que el backend permita CORS para http://localhost:4200.
 * Si no lo permite, usar `npm run start:proxy` (environment.proxy.ts + proxy.conf.json).
 */
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api',
  youtubeApiKey: '',
  youtubeQuery: 'streetwear lookbook outfit',
};
