package co.edu.unbosque.tstyle.cart.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Cantidad de unidades de un producto ya identificado por la URL.
 *
 * @param quantity unidades
 *
 * @author Equipo T Style
 */
public record QuantityRequest(

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad mínima es 1")
        @Max(value = CartLimits.MAX_QUANTITY_PER_ITEM,
                message = "La cantidad máxima por producto es 10")
        Integer quantity) {
}
