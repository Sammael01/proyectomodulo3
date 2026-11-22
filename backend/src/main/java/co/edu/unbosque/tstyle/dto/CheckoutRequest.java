package co.edu.unbosque.tstyle.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

public record CheckoutRequest(

        @DecimalMin(value = "-90.0", message = "La latitud debe estar entre -90 y 90")
        @DecimalMax(value = "90.0", message = "La latitud debe estar entre -90 y 90")
        Double latitude,

        @DecimalMin(value = "-180.0", message = "La longitud debe estar entre -180 y 180")
        @DecimalMax(value = "180.0", message = "La longitud debe estar entre -180 y 180")
        Double longitude,

        @DecimalMin(value = "0.0", message = "La precisión no puede ser negativa")
        Double accuracyMeters) {
}
