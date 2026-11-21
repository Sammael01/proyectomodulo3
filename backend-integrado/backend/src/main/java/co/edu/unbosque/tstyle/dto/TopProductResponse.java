package co.edu.unbosque.tstyle.dto;

import java.math.BigDecimal;

public record TopProductResponse(
        Long productId,
        String name,
        long unitsSold,
        BigDecimal revenue) {
}
