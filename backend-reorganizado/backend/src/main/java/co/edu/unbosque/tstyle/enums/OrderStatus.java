package co.edu.unbosque.tstyle.enums;

/**
 * Estados de un pedido. El checkout es simulado: el pedido nace como PAID.
 *
 * @author Equipo T Style
 */
public enum OrderStatus {

    /** Pago simulado aprobado. */
    PAID,

    /** Pedido despachado. */
    SHIPPED,

    /** Pedido entregado. */
    DELIVERED,

    /** Pedido cancelado (devuelve el stock). */
    CANCELLED
}
