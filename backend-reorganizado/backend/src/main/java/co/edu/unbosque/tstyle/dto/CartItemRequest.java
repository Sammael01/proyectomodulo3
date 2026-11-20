package co.edu.unbosque.tstyle.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Datos para agregar un producto al carrito.
 *
 * @param productId identificador del producto
 * @param quantity unidades a agregar (1 a 99)
 *
 * @author Equipo T Style
 */
public record CartItemRequest(

        @NotNull(message = "El producto es obligatorio")
        Long productId,

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad mínima es 1")
        @Max(value = 99, message = "La cantidad máxima es 99")
        Integer quantity) {
}
