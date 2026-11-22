package co.edu.unbosque.tstyle.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import co.edu.unbosque.tstyle.dto.PageResponse;
import co.edu.unbosque.tstyle.dto.ProductResponse;
import co.edu.unbosque.tstyle.exception.ResourceNotFoundException;
import co.edu.unbosque.tstyle.mapper.ProductMapper;
import co.edu.unbosque.tstyle.model.Category;
import co.edu.unbosque.tstyle.model.Product;
import co.edu.unbosque.tstyle.model.Provider;
import co.edu.unbosque.tstyle.repository.ProductRepository;

/**
 * Pruebas unitarias de la consulta del catálogo.
 *
 * @author Sebastián
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    private ProductService service;

    @BeforeEach
    void setUp() {
        service = new ProductService(productRepository, new ProductMapper());
    }

    @Test
    void shouldMapPageAndIgnoreBlankSearch() {
        Pageable pageable = PageRequest.of(0, 12);
        when(productRepository.search(isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(product(true)), pageable, 1));

        PageResponse<ProductResponse> response = service.search(null, null, "   ", pageable);

        assertEquals(1, response.content().size());
        assertEquals("Camiseta", response.content().get(0).name());
        assertEquals("Proveedor", response.content().get(0).provider());
        assertEquals(1, response.totalElements());
        assertEquals(12, response.size());
    }

    @Test
    void shouldTrimSearchTermBeforeQuerying() {
        Pageable pageable = PageRequest.of(0, 12);
        when(productRepository.search(isNull(), isNull(), any(String.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        service.search(null, null, "  camiseta ", pageable);

        verify(productRepository).search(null, null, "camiseta", pageable);
    }

    @Test
    void shouldReturnProductById() {
        when(productRepository.findById(5L)).thenReturn(Optional.of(product(true)));

        assertEquals("Camiseta", service.getById(5L).name());
    }

    @Test
    void shouldFailWhenProductDoesNotExist() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getById(99L));
    }

    @Test
    void shouldHideProductsOfInactiveProviders() {
        Product product = product(true);
        product.getProvider().setActive(false);
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));

        assertThrows(ResourceNotFoundException.class, () -> service.getById(5L));
    }

    private Product product(boolean available) {
        Product product = new Product();
        product.setId(5L);
        product.setProvider(new Provider("Proveedor"));
        product.setCategory(new Category("Camisetas"));
        product.setExternalId("X-1");
        product.setName("Camiseta");
        product.setImageUrl("https://example.com/img.jpg");
        product.setPrice(new BigDecimal("59900"));
        product.setCurrency("COP");
        product.setExternalUrl("https://example.com/p/1");
        product.setProviderAvailable(available);
        return product;
    }
}
