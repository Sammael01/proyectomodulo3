package co.edu.unbosque.tstyle.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import co.edu.unbosque.tstyle.dto.ProductResponse;
import co.edu.unbosque.tstyle.model.Category;
import co.edu.unbosque.tstyle.model.Product;
import co.edu.unbosque.tstyle.model.Provider;

/**
 * Pruebas del contrato ProductResponse que consumen wishlist y carrito.
 *
 * @author Sebastián
 */
class ProductMapperTest {

    private final ProductMapper mapper = new ProductMapper();

    @Test
    void shouldExposeTheAgreedContractFields() {
        ProductResponse response = mapper.toResponse(product(Boolean.TRUE));

        assertEquals(7L, response.id());
        assertEquals("Proveedor", response.provider());
        assertEquals("X-1", response.externalId());
        assertEquals("Camiseta", response.name());
        assertEquals("https://example.com/img.jpg", response.imageUrl());
        assertEquals(new BigDecimal("59900.00"), response.price());
        assertEquals("COP", response.currency());
        assertEquals("https://example.com/p/1", response.externalUrl());
        assertEquals(Boolean.TRUE, response.providerAvailable());
    }

    @Test
    void shouldKeepAvailabilityNullWhenProviderDoesNotReportIt() {
        assertNull(mapper.toResponse(product(null)).providerAvailable());
    }

    private Product product(Boolean available) {
        Product product = new Product();
        product.setId(7L);
        product.setProvider(new Provider("Proveedor"));
        product.setCategory(new Category("Camisetas"));
        product.setExternalId("X-1");
        product.setName("Camiseta");
        product.setImageUrl("https://example.com/img.jpg");
        product.setPrice(new BigDecimal("59900.00"));
        product.setCurrency("COP");
        product.setExternalUrl("https://example.com/p/1");
        product.setProviderAvailable(available);
        return product;
    }
}
