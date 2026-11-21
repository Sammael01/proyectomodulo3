package co.edu.unbosque.tstyle.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import co.edu.unbosque.tstyle.dto.ProductImportRequest;
import co.edu.unbosque.tstyle.exception.ExternalServiceException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Pruebas de lectura del JSON de datos del catálogo con datos controlados.
 *
 * @author Sebastián
 */
class JsonCatalogAdapterTest {

    private JsonCatalogAdapter adapter(String path) {
        return new JsonCatalogAdapter(JsonMapper.builder().build(), new DefaultResourceLoader(), path);
    }

    @Test
    void shouldReadProductsFromJsonFile() {
        List<ProductImportRequest> products =
                adapter("classpath:catalog/catalogo-prueba.json").fetchProducts();

        assertEquals(2, products.size());
        assertEquals("A-1", products.get(0).externalId());
        assertEquals("COP", products.get(0).currency());
        assertEquals(Boolean.TRUE, products.get(0).providerAvailable());
        assertNull(products.get(1).providerAvailable());
    }

    @Test
    void shouldFailWhenPathIsNotConfigured() {
        assertThrows(ExternalServiceException.class, () -> adapter("").fetchProducts());
    }

    @Test
    void shouldFailWhenFileDoesNotExist() {
        assertThrows(ExternalServiceException.class,
                () -> adapter("classpath:catalog/no-existe.json").fetchProducts());
    }

    @Test
    void shouldFailWhenJsonIsNotAProductList() {
        assertThrows(ExternalServiceException.class,
                () -> adapter("classpath:catalog/catalogo-invalido.json").fetchProducts());
    }
}
