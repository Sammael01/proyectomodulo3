/**
 * Registro central de endpoints del backend unificado.
 *
 * Reglas:
 *  - Una ruta en `null` significa PENDIENTE DE CONFIRMACIÓN: el servicio correspondiente
 *    sigue funcionando con datos locales y no envía ninguna petición HTTP.
 *  - Al confirmar una ruta con el integrante responsable, se escribe aquí (relativa a apiUrl)
 *    y el servicio pasa automáticamente a consumir el backend. No escribir rutas supuestas.
 *  - `{id}` se reemplaza con `endpoint(ruta, { id })`.
 */
export const API_ENDPOINTS = {
  /** Módulo A — usuarios, seguridad y JWT. Rutas ya usadas por el frontend; confirmar con A. */
  auth: {
    login: '/auth/login',
    register: '/users/register',
    verifyEmail: '/users/verify-email',
    resendVerification: '/users/resend-verification',
    forgotPassword: '/auth/forgot-password',
    resetPassword: '/auth/reset-password',
    me: '/auth/me',
  },
  /** Módulo B — catálogo de productos. PENDIENTE: definir con B. */
  catalog: {
    list: null as string | null,
    byId: null as string | null, // ej. de formato: '/<recurso>/{id}'
    search: null as string | null,
  },
  /** Módulo B — administración de productos (rol ADMIN). PENDIENTE: definir con B. */
  catalogAdmin: {
    create: null as string | null,
    update: null as string | null,
    remove: null as string | null,
  },
  /** Módulo C — carrito del usuario autenticado. PENDIENTE: definir con C. */
  cart: {
    get: null as string | null,
    addItem: null as string | null,
    updateItem: null as string | null,
    removeItem: null as string | null,
    clear: null as string | null,
  },
  /** Módulo C — pedidos y checkout. PENDIENTE: definir con C. */
  orders: {
    create: null as string | null,
    mine: null as string | null,
  },
  /** Módulo C — administración de pedidos (rol ADMIN). PENDIENTE: definir con C. */
  ordersAdmin: {
    all: null as string | null,
    updateStatus: null as string | null,
  },
  /** Módulo B — películas/trailers (TMDB vía backend). PENDIENTE: definir con B. */
  videos: {
    list: null as string | null,
  },
};

/** Sustituye los marcadores `{param}` de una ruta. */
export function endpoint(path: string, params: Record<string, string | number> = {}): string {
  return path.replace(/\{(\w+)\}/g, (_, key: string) => {
    if (!(key in params)) throw new Error(`Falta el parámetro "${key}" para el endpoint ${path}`);
    return encodeURIComponent(String(params[key]));
  });
}

/** Error usado cuando se invoca una operación remota cuyo endpoint aún no está confirmado. */
export class PendingEndpointError extends Error {
  constructor(name: string) {
    super(`El endpoint "${name}" aún no está definido por el backend.`);
    this.name = 'PendingEndpointError';
  }
}
