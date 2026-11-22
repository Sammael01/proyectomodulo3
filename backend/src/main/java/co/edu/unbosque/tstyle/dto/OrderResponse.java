package co.edu.unbosque.tstyle.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import co.edu.unbosque.tstyle.enums.EmailStatus;
import co.edu.unbosque.tstyle.enums.OrderStatus;
import co.edu.unbosque.tstyle.model.PurchaseOrder;

public record OrderResponse(
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
    public static OrderResponse from(PurchaseOrder order, boolean includeEmailError) {
        return new OrderResponse(
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
