package co.edu.unbosque.tstyle.order.enums;

/**
 * Estados de un pedido de T Style.
 *
 * <p>El checkout es simulado: el pedido nace directamente como PAID.</p>
 *
 * @author Equipo T Style
 */
public enum OrderStatus {

    /** Pago simulado aprobado. */
    PAID,

    /** Pedido despachado. */
    SHIPPED,

    /** Pedido entregado al cliente. */
    DELIVERED,

    /** Pedido cancelado (devuelve el stock). */
    CANCELLED
}
