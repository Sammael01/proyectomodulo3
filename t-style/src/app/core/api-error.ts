import { HttpErrorResponse } from '@angular/common/http';

export function apiMessage(error: unknown, fallback = 'Ocurrió un error inesperado. Intenta de nuevo.'): string {
  if (!(error instanceof HttpErrorResponse)) return fallback;
  if (error.status === 0) return 'No fue posible conectar con el servidor. Verifica que el backend esté en ejecución.';
  const message: string | undefined = error.error?.message;
  if (!message) return error.status >= 500 ? 'El servidor no pudo procesar la solicitud.' : fallback;
  return message.replace(/^[A-Za-z]+:\s*/, '');
}
