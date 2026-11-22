package co.edu.unbosque.tstyle.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import co.edu.unbosque.tstyle.enums.WishlistStatus;
import co.edu.unbosque.tstyle.model.WishlistItem;

public record WishlistItemResponse(
        Long productId,
        String name,
        BigDecimal price,
        boolean available,
        WishlistStatus status,
        LocalDateTime addedAt,
        LocalDateTime updatedAt) {
    public static WishlistItemResponse from(WishlistItem item) {
        return new WishlistItemResponse(
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getPrice(),
                item.getProduct().isActive() && item.getProduct().getStock() > 0,
                item.getStatus(),
                item.getCreatedAt(),
                item.getUpdatedAt());
    }
}
