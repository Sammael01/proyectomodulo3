package co.edu.unbosque.tstyle.provider;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;

import co.edu.unbosque.tstyle.dto.CatalogImportResult;
import co.edu.unbosque.tstyle.dto.ProductImportRequest;
import co.edu.unbosque.tstyle.exception.ExternalServiceException;
import co.edu.unbosque.tstyle.service.CatalogImportService;

/**
 * Pruebas del importador de arranque: un catálogo no disponible
 * no debe impedir que el backend inicie.
 *
 * @author Sebastián
 */
class CatalogImportRunnerTest {

    private final ApplicationArguments arguments = mock(ApplicationArguments.class);
    private final CatalogImportService importService = mock(CatalogImportService.class);
    private final ProviderAdapter adapter = mock(ProviderAdapter.class);

    @Test
    void shouldNotFailStartupWhenCatalogFileIsUnavailable() {
        when(adapter.fetchProducts()).thenThrow(new ExternalServiceException("No existe el archivo"));
        CatalogImportRunner runner = new CatalogImportRunner(List.of(adapter), importService);

        assertDoesNotThrow(() -> runner.run(arguments));

        verify(importService, never()).importRecords(anyList());
    }

    @Test
    void shouldNotFailStartupWhenDatabaseImportFails() {
        List<ProductImportRequest> records = List.of();
        when(adapter.fetchProducts()).thenReturn(records);
        when(importService.importRecords(records)).thenThrow(new IllegalStateException("BD no disponible"));
        CatalogImportRunner runner = new CatalogImportRunner(List.of(adapter), importService);

        assertDoesNotThrow(() -> runner.run(arguments));
    }

    @Test
    void shouldImportRecordsFromEveryAdapter() {
        List<ProductImportRequest> records = List.of();
        when(adapter.fetchProducts()).thenReturn(records);
        when(importService.importRecords(records))
                .thenReturn(new CatalogImportResult(0, 0, 0, List.of()));
        CatalogImportRunner runner = new CatalogImportRunner(List.of(adapter), importService);

        runner.run(arguments);

        verify(importService).importRecords(records);
    }
}
