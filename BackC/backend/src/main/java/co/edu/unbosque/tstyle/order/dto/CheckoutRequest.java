package co.edu.unbosque.tstyle.order.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

/**
 * Datos opcionales del checkout.
 *
 * <p>Las coordenadas solo deben enviarse si el usuario concedió el permiso de
 * ubicación al navegador. Si no se envían, la auditoría guarda únicamente la IP,
 * que NO equivale a una ubicación GPS.</p>
 *
 * @param latitude latitud del navegador (-90 a 90)
 * @param longitude longitud del navegador (-180 a 180)
 * @param accuracyMeters precisión reportada por el navegador, en metros
 *
 * @author Equipo T Style
 */
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
