package co.edu.unbosque.tstyle.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import co.edu.unbosque.tstyle.order.enums.EmailStatus;
import co.edu.unbosque.tstyle.order.enums.OrderStatus;
import co.edu.unbosque.tstyle.order.model.PurchaseOrder;

/**
 * Detalle completo de un pedido. También es la fuente de datos del
 * comprobante PDF.
 *
 * @param id identificador del pedido
 * @param orderNumber número de pedido legible (TS-2026-000123)
 * @param createdAt fecha y hora del pedido
 * @param status estado del pedido
 * @param userId identificador del comprador
 * @param buyerName nombre del comprador
 * @param buyerEmail correo del comprador
 * @param items líneas con el precio aplicado
 * @param total valor total
 * @param emailStatus estado del envío del comprobante
 * @param emailSentAt fecha de envío del comprobante
 * @param emailError motivo del último fallo (solo se llena para administradores)
 *
 * @author Equipo T Style
 */
public record OrderDetailResponse(
        Long id,
        String orderNumber,
        LocalDateTime createdAt,
        OrderStatus status,
        Long userId,
        String buyerName,
        String buyerEmail,
        List<OrderItemResponse> items,
        BigDecimal total,
        EmailStatus emailStatus,
        LocalDateTime emailSentAt,
        String emailError) {

    /**
     * Convierte la entidad en su respuesta. Debe llamarse con el comprador
     * y las líneas ya cargados (dentro de la transacción).
     *
     * @param order pedido
     * @param includeEmailError true para incluir el motivo del fallo de correo
     * @return respuesta para el cliente
     */
    public static OrderDetailResponse from(PurchaseOrder order, boolean includeEmailError) {
        return new OrderDetailResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getCreatedAt(),
                order.getStatus(),
                order.getUser().getId(),
                order.getUser().getName(),
                order.getUser().getEmail(),
                order.getItems().stream().map(OrderItemResponse::from).toList(),
                order.getTotal(),
                order.getEmailStatus(),
                order.getEmailSentAt(),
                includeEmailError ? order.getEmailError() : null);
    }
}
