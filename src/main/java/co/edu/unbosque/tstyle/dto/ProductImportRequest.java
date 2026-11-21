package co.edu.unbosque.tstyle.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Representa un registro del JSON con datos del catálogo
 * antes de ser importado.
 *
 * @author Sebastián
 */
public record ProductImportRequest(

        @NotBlank @Size(max = 50)
        String provider,

        @NotBlank @Size(max = 100)
        String externalId,

        @NotBlank @Size(max = 200)
        String name,

        @Size(max = 2000)
        String description,

        @NotBlank @Size(max = 500)
        String imageUrl,

        @NotNull @DecimalMin("0.0")
        BigDecimal price,

        @NotBlank @Size(min = 3, max = 3)
        String currency,

        @NotBlank @Size(max = 500)
        String externalUrl,

        @NotBlank @Size(max = 100)
        String category,

        Boolean providerAvailable) {
}