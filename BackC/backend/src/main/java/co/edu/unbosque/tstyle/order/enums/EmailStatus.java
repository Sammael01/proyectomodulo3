package co.edu.unbosque.tstyle.order.enums;

/**
 * Estado del envío del comprobante por correo.
 *
 * <p>Es independiente del estado del pedido: un fallo de correo
 * nunca afecta ni duplica el pedido.</p>
 *
 * @author Equipo T Style
 */
public enum EmailStatus {

    /** Aún no se ha enviado (o el correo del comprador no está verificado). */
    PENDING,

    /** El comprobante fue enviado correctamente. */
    SENT,

    /** El envío falló; se puede reintentar sin crear otro pedido. */
    FAILED
}
