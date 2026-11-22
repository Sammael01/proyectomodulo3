package co.edu.unbosque.tstyle.dto;

import java.math.BigDecimal;

public record CartLineResponse(
        Long productId,
        String name,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal subtotal,
        int stock,
        boolean available) {
}
