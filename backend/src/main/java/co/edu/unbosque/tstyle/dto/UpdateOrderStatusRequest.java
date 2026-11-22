package co.edu.unbosque.tstyle.dto;

import co.edu.unbosque.tstyle.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(

        @NotNull(message = "El estado es obligatorio")
        OrderStatus status) {
}
