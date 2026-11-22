package co.edu.unbosque.tstyle.provider;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import co.edu.unbosque.tstyle.dto.ProductImportRequest;
import co.edu.unbosque.tstyle.exception.ExternalServiceException;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * Lee el JSON de datos del catálogo (lista de productos) desde la ruta
 * configurada en catalog.import.path. No lee el JSON de esquema del profesor.
 *
 * @author Sebastián
 */
@Component
public class JsonCatalogAdapter implements ProviderAdapter {

    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;
    private final String path;

    /**
     * Constructor del adaptador.
     *
     * @param objectMapper conversor JSON de Spring Boot
     * @param resourceLoader cargador de recursos (classpath: o file:)
     * @param path ubicación del archivo JSON del catálogo
     */
    public JsonCatalogAdapter(ObjectMapper objectMapper, ResourceLoader resourceLoader,
            @Value("${catalog.import.path:}") String path) {
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
        this.path = path;
    }

    @Override
    public List<ProductImportRequest> fetchProducts() {
        if (path == null || path.isBlank()) {
            throw new ExternalServiceException("No se configuró catalog.import.path");
        }

        Resource resource = resourceLoader.getResource(path.trim());
        if (!resource.exists()) {
            throw new ExternalServiceException("No existe el archivo del catálogo: " + path);
        }

        try (InputStream input = resource.getInputStream()) {
            return objectMapper.readValue(input, new TypeReference<List<ProductImportRequest>>() { });
        } catch (IOException | JacksonException exception) {
            throw new ExternalServiceException("El archivo del catálogo no tiene un formato válido");
        }
    }
}
