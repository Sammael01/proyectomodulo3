package co.edu.unbosque.tstyle.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.DefaultResourceLoader;

import co.edu.unbosque.tstyle.dto.ProductImportRequest;
import co.edu.unbosque.tstyle.exception.ExternalServiceException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Pruebas de lectura del JSON de datos del catálogo con archivos temporales
 * controlados; no dependen de archivos en src/test/resources.
 *
 * @author Sebastián
 */
class JsonCatalogAdapterTest {

    private static final String VALID_JSON = """
            [
              {"provider": "ProveedorPrueba", "externalId": "A-1", "name": "Camiseta de prueba",
               "description": "Registro controlado", "imageUrl": "https://example.com/img/a-1.jpg",
               "price": 59900, "currency": "COP", "externalUrl": "https://example.com/p/a-1",
               "category": "Camisetas", "providerAvailable": true},
              {"provider": "ProveedorPrueba", "externalId": "A-2", "name": "Pantalón de prueba",
               "imageUrl": "https://example.com/img/a-2.jpg", "price": 89900, "currency": "COP",
               "externalUrl": "https://example.com/p/a-2", "category": "Pantalones"}
            ]
            """;

    @TempDir
    Path tempDir;

    private JsonCatalogAdapter adapter(String path) {
        return new JsonCatalogAdapter(JsonMapper.builder().build(), new DefaultResourceLoader(), path);
    }

    private String write(String name, String content) throws IOException {
        Path file = tempDir.resolve(name);
        Files.writeString(file, content);
        return file.toUri().toString();
    }

    @Test
    void shouldReadProductsFromJsonFile() throws IOException {
        List<ProductImportRequest> products = adapter(write("catalogo.json", VALID_JSON)).fetchProducts();

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
        String missing = tempDir.resolve("no-existe.json").toUri().toString();

        assertThrows(ExternalServiceException.class, () -> adapter(missing).fetchProducts());
    }

    @Test
    void shouldFailWhenJsonIsNotAProductList() throws IOException {
        String path = write("invalido.json", "{\"no\":\"es una lista\"}");

        assertThrows(ExternalServiceException.class, () -> adapter(path).fetchProducts());
    }
}
