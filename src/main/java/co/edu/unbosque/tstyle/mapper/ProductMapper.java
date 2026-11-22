package co.edu.unbosque.tstyle.mapper;

import org.springframework.stereotype.Component;

import co.edu.unbosque.tstyle.dto.ProductResponse;
import co.edu.unbosque.tstyle.model.Product;

/**
 * Convierte entidades de producto en respuestas para el frontend.
 *
 * @author Equipo T Style
 */
@Component
public class ProductMapper {

    /**
     * Convierte un producto en su respuesta pública.
     *
     * @param product entidad a convertir
     * @return respuesta con los datos del producto
     */
    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getProvider().getName(),
                product.getExternalId(),
                product.getName(),
                product.getImageUrl(),
                product.getPrice(),
                product.getCurrency(),
                product.getExternalUrl(),
                product.getProviderAvailable());
    }
}