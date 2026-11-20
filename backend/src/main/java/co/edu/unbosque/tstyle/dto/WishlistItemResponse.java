package co.edu.unbosque.tstyle.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import co.edu.unbosque.tstyle.enums.WishlistStatus;
import co.edu.unbosque.tstyle.model.WishlistItem;

/**
 * Producto de la wishlist tal como lo recibe el cliente.
 *
 * @param productId identificador del producto
 * @param name nombre del producto
 * @param price precio vigente
 * @param available true si el producto está activo y hay stock
 * @param status estado asignado por el usuario
 * @param addedAt fecha en que se guardó
 * @param updatedAt fecha de la última modificación
 *
 * @author Equipo T Style
 */
public record WishlistItemResponse(
        Long productId,
        String name,
        BigDecimal price,
        boolean available,
        WishlistStatus status,
        LocalDateTime addedAt,
        LocalDateTime updatedAt) {

    /**
     * Convierte la entidad en su respuesta.
     *
     * @param item elemento de la wishlist
     * @return respuesta para el cliente
     */
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
