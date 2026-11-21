package co.edu.unbosque.tstyle.mapper;

import org.springframework.stereotype.Component;

import co.edu.unbosque.tstyle.dto.ProductResponse;
import co.edu.unbosque.tstyle.model.Product;

@Component
public class ProductMapper {
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
                product.getProviderAvalaible(),
                product.getStock());
    }
}
