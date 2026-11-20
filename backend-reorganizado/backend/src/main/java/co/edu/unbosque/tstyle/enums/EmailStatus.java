package co.edu.unbosque.tstyle.enums;

/**
 * Estado del envío del comprobante por correo. Es independiente del estado
 * del pedido: un fallo de correo nunca afecta ni duplica el pedido.
 *
 * @author Equipo T Style
 */
public enum EmailStatus {

    /** Aún no enviado (o el correo del comprador no está verificado). */
    PENDING,

    /** Comprobante enviado. */
    SENT,

    /** El envío falló; se puede reintentar sin crear otro pedido. */
    FAILED
}
