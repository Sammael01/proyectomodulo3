import { PaymentMethod } from './models';

/**
 * Configuración de métodos de pago. PENDIENTE: definir con el equipo y con C qué métodos
 * existirán en la entrega.
 *
 *  - `gatewayAvailable`: true solo si hay una pasarela de pago REAL integrada (tarjeta/PSE).
 *    Mientras sea false, tarjeta y PSE se muestran explícitamente como simulación (modo local)
 *    o deshabilitados (modo API).
 *  - `backendMethods`: métodos que el backend de C acepta al crear un pedido. En modo API,
 *    contraentrega solo se habilita si aparece aquí.
 *
 * Los datos de tarjeta nunca se guardan, ni se registran en consola, ni se envían al backend propio.
 */
export const CHECKOUT_CONFIG = {
  gatewayAvailable: false,
  backendMethods: [] as PaymentMethod[],
};
