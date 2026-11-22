import { HttpErrorResponse } from '@angular/common/http';
import { PendingEndpointError } from './api-endpoints';

const STATUS_MESSAGES: Record<number, string> = {
  400: 'La solicitud contiene datos inválidos. Revisa la información e intenta de nuevo.',
  401: 'No autorizado. Verifica tus credenciales o inicia sesión nuevamente.',
  403: 'No tienes permisos para realizar esta acción.',
  404: 'El recurso solicitado no existe.',
  409: 'La operación entra en conflicto con el estado actual (por ejemplo, inventario insuficiente).',
};

/**
 * Convierte un error HTTP en un mensaje para el usuario. Prioriza el mensaje real del backend
 * (`message`, o la lista `errors` si existe) y solo usa textos genéricos cuando no viene ninguno.
 */
export function apiMessage(error: unknown, fallback = 'Ocurrió un error inesperado. Intenta de nuevo.'): string {
  if (error instanceof PendingEndpointError) return 'Esta funcionalidad aún no está conectada al servidor.';
  if (!(error instanceof HttpErrorResponse)) return fallback;
  if (error.status === 0) return 'No fue posible conectar con el servidor. Verifica que el backend esté en ejecución.';
  const body = error.error;
  const message: string | undefined = typeof body === 'string' && body.trim() && !body.trimStart().startsWith('<')
    ? body
    : body?.message;
  if (message) return message.replace(/^[A-Za-z]+:\s*/, '');
  if (Array.isArray(body?.errors) && body.errors.length) {
    return body.errors.map((e: unknown) => (typeof e === 'string' ? e : (e as { message?: string })?.message ?? '')).filter(Boolean).join(' ') || fallback;
  }
  if (error.status >= 500) return 'El servidor no pudo procesar la solicitud.';
  return STATUS_MESSAGES[error.status] ?? fallback;
}
