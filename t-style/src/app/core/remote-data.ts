/** Origen de datos de una funcionalidad: `local` (demo/localStorage) o `api` (backend). */
export type DataSource = 'local' | 'api';

/** Resultado paginado normalizado para la interfaz. */
export interface PageResult<T> {
  items: T[];
  page: number;
  size: number;
  totalItems: number;
  totalPages: number;
}

/**
 * Normaliza una respuesta de listado. Acepta un arreglo plano o un objeto paginado con la
 * forma de `org.springframework.data.domain.Page` (content/number/size/totalElements/totalPages).
 * PENDIENTE: confirmar con B/C cuál de las dos formas devolverá cada endpoint.
 */
export function toPageResult<T>(body: unknown, mapItem: (raw: unknown) => T): PageResult<T> {
  if (Array.isArray(body)) {
    const items = body.map(mapItem);
    return { items, page: 0, size: items.length, totalItems: items.length, totalPages: 1 };
  }
  const page = (body ?? {}) as { content?: unknown[]; number?: number; size?: number; totalElements?: number; totalPages?: number };
  const items = Array.isArray(page.content) ? page.content.map(mapItem) : [];
  return {
    items,
    page: page.number ?? 0,
    size: page.size ?? items.length,
    totalItems: page.totalElements ?? items.length,
    totalPages: page.totalPages ?? 1,
  };
}
