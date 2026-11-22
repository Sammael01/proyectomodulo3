package co.edu.unbosque.tstyle.provider;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import co.edu.unbosque.tstyle.dto.CatalogImportResult;
import co.edu.unbosque.tstyle.service.CatalogImportService;

/**
 * Importa el catálogo al arrancar la aplicación cuando
 * catalog.import.enabled=true. Si la fuente falla (archivo ausente, JSON
 * inválido o error de base de datos) se registra el problema y la aplicación
 * arranca igual. No expone ningún endpoint: la importación por HTTP debe
 * esperar a la autorización por rol ADMIN del módulo de seguridad.
 *
 * @author Sebastián
 */
@Component
@ConditionalOnProperty(name = "catalog.import.enabled", havingValue = "true")
public class CatalogImportRunner implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogImportRunner.class);

    private final List<ProviderAdapter> adapters;
    private final CatalogImportService importService;

    /**
     * Constructor del importador de arranque.
     *
     * @param adapters fuentes de productos disponibles
     * @param importService servicio que valida y guarda los registros
     */
    public CatalogImportRunner(List<ProviderAdapter> adapters, CatalogImportService importService) {
        this.adapters = adapters;
        this.importService = importService;
    }

    @Override
    public void run(ApplicationArguments args) {
        for (ProviderAdapter adapter : adapters) {
            try {
                CatalogImportResult result = importService.importRecords(adapter.fetchProducts());
                LOGGER.info("Catálogo importado: {} creados, {} actualizados, {} rechazados",
                        result.created(), result.updated(), result.failed());
                result.errors().forEach(error -> LOGGER.warn("Registro rechazado: {}", error));
            } catch (RuntimeException exception) {
                LOGGER.warn("No se pudo importar el catálogo; la aplicación continúa: {}",
                        exception.getMessage());
            }
        }
    }
}
