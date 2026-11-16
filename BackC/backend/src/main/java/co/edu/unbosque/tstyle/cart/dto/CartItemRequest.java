package co.edu.unbosque.tstyle.cart.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Datos para agregar un producto al carrito.
 *
 * @param productId identificador del producto
 * @param quantity unidades a agregar
 *
 * @author Equipo T Style
 */
public record CartItemRequest(

        @NotNull(message = "El producto es obligatorio")
        Long productId,

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad mínima es 1")
        @Max(value = CartLimits.MAX_QUANTITY_PER_ITEM,
                message = "La cantidad máxima por producto es 10")
        Integer quantity) {
}
