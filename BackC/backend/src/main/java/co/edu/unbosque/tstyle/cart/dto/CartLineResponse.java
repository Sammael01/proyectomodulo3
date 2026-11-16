package co.edu.unbosque.tstyle.cart.dto;

import java.math.BigDecimal;

/**
 * Línea del resumen del carrito.
 *
 * @param productId identificador del producto
 * @param name nombre del producto
 * @param unitPrice precio unitario vigente
 * @param quantity unidades en el carrito
 * @param subtotal precio unitario por cantidad
 * @param stock unidades disponibles actualmente
 * @param available false si el producto se desactivó o ya no alcanza el stock
 *
 * @author Equipo T Style
 */
public record CartLineResponse(
        Long productId,
        String name,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal subtotal,
        int stock,
        boolean available) {
}
