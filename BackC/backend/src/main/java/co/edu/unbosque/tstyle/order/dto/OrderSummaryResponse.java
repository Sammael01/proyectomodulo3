package co.edu.unbosque.tstyle.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import co.edu.unbosque.tstyle.order.enums.EmailStatus;
import co.edu.unbosque.tstyle.order.enums.OrderStatus;
import co.edu.unbosque.tstyle.order.model.PurchaseOrder;

/**
 * Resumen de un pedido para listados.
 *
 * @param id identificador del pedido
 * @param orderNumber número de pedido legible
 * @param createdAt fecha y hora del pedido
 * @param status estado del pedido
 * @param total valor total
 * @param emailStatus estado del envío del comprobante
 * @param userId identificador del comprador
 * @param buyerName nombre del comprador
 * @param buyerEmail correo del comprador
 *
 * @author Equipo T Style
 */
public record OrderSummaryResponse(
        Long id,
        String orderNumber,
        LocalDateTime createdAt,
        OrderStatus status,
        BigDecimal total,
        EmailStatus emailStatus,
        Long userId,
        String buyerName,
        String buyerEmail) {

    /**
     * Convierte la entidad en su resumen. Requiere el comprador cargado.
     *
     * @param order pedido
     * @return resumen para el cliente
     */
    public static OrderSummaryResponse from(PurchaseOrder order) {
        return new OrderSummaryResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getCreatedAt(),
                order.getStatus(),
                order.getTotal(),
                order.getEmailStatus(),
                order.getUser().getId(),
                order.getUser().getName(),
                order.getUser().getEmail());
    }
}
