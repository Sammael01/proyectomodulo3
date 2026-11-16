package co.edu.unbosque.tstyle.order.dto;

import java.math.BigDecimal;

import co.edu.unbosque.tstyle.order.model.OrderItem;

/**
 * Línea de un pedido con el precio aplicado al comprar.
 *
 * @param productId identificador del producto
 * @param productName nombre del producto al comprar
 * @param unitPrice precio unitario aplicado
 * @param quantity unidades compradas
 * @param subtotal precio unitario por cantidad
 *
 * @author Equipo T Style
 */
public record OrderItemResponse(
        Long productId,
        String productName,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal subtotal) {

    /**
     * Convierte la entidad en su respuesta.
     *
     * @param item línea del pedido
     * @return respuesta para el cliente
     */
    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(
                item.getProductId(),
                item.getProductName(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getSubtotal());
    }
}
