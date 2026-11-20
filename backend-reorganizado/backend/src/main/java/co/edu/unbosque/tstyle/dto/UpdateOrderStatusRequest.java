package co.edu.unbosque.tstyle.dto;

import co.edu.unbosque.tstyle.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Nuevo estado que un administrador asigna a un pedido.
 *
 * @param status estado deseado
 *
 * @author Equipo T Style
 */
public record UpdateOrderStatusRequest(

        @NotNull(message = "El estado es obligatorio")
        OrderStatus status) {
}
