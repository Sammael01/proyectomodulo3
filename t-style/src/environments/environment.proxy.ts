/**
 * Ambiente de DESARROLLO LOCAL con proxy de Angular (npm run start:proxy).
 * Las peticiones a /api las reenvía el dev-server a http://localhost:8080 (proxy.conf.json),
 * por lo que no se necesita CORS. Solo para desarrollo: el proxy no existe en Netlify.
 */
export const environment = {
  production: false,
  apiUrl: '/api',
  youtubeApiKey: '',
  youtubeQuery: 'streetwear lookbook outfit',
};
