package co.edu.unbosque.tstyle.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.tstyle.dto.CatalogImportResult;
import co.edu.unbosque.tstyle.dto.ProductImportRequest;
import co.edu.unbosque.tstyle.model.Category;
import co.edu.unbosque.tstyle.model.Product;
import co.edu.unbosque.tstyle.model.Provider;
import co.edu.unbosque.tstyle.repository.CategoryRepository;
import co.edu.unbosque.tstyle.repository.ProductRepository;
import co.edu.unbosque.tstyle.repository.ProviderRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

/**
 * Pruebas unitarias de la importación del catálogo.
 *
 * @author Sebastián
 */
@ExtendWith(MockitoExtension.class)
class CatalogImportServiceTest {

    @Mock
    private ProviderRepository providerRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    private CatalogImportService service;

    @BeforeEach
    void setUp() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        service = new CatalogImportService(providerRepository, categoryRepository,
                productRepository, validator);
    }

    @Test
    void shouldCreateProductAndNormalizeFields() {
        when(providerRepository.findByName("Proveedor")).thenReturn(Optional.empty());
        when(providerRepository.save(any(Provider.class))).thenAnswer(call -> call.getArgument(0));
        when(categoryRepository.findByName("Camisetas")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenAnswer(call -> call.getArgument(0));

        CatalogImportResult result = service.importRecords(List.of(
                record(" Proveedor ", " X-1 ", "cop", new BigDecimal("59900"), " Camisetas ")));

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        Product saved = captor.getValue();

        assertEquals(1, result.created());
        assertEquals(0, result.updated());
        assertEquals(0, result.failed());
        assertEquals("X-1", saved.getExternalId());
        assertEquals("COP", saved.getCurrency());
        assertEquals(new BigDecimal("59900"), saved.getPrice());
        assertEquals(Boolean.TRUE, saved.getProviderAvailable());
        assertNotNull(saved.getLastSyncedAt());
    }

    @Test
    void shouldUpdateExistingProductByProviderAndExternalId() {
        Provider provider = new Provider("Proveedor");
        Category category = new Category("Camisetas");
        Product existing = new Product();
        when(providerRepository.findByName("Proveedor")).thenReturn(Optional.of(provider));
        when(categoryRepository.findByName("Camisetas")).thenReturn(Optional.of(category));
        when(productRepository.findByProviderIdAndExternalId(provider.getId(), "X-1"))
                .thenReturn(Optional.of(existing));

        CatalogImportResult result = service.importRecords(List.of(
                record("Proveedor", "X-1", "COP", new BigDecimal("70000"), "Camisetas")));

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());

        assertEquals(0, result.created());
        assertEquals(1, result.updated());
        assertSame(existing, captor.getValue());
        assertEquals(new BigDecimal("70000"), existing.getPrice());
        verify(providerRepository, never()).save(any(Provider.class));
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void shouldRejectInvalidRecordsAndKeepImportingTheRest() {
        when(providerRepository.findByName("Proveedor")).thenReturn(Optional.of(new Provider("Proveedor")));
        when(categoryRepository.findByName("Camisetas")).thenReturn(Optional.of(new Category("Camisetas")));

        List<ProductImportRequest> records = new ArrayList<>();
        records.add(record("Proveedor", "X-1", "CO", new BigDecimal("100"), "Camisetas"));
        records.add(record("Proveedor", "X-2", "COP", new BigDecimal("-5"), "Camisetas"));
        records.add(null);
        records.add(record("Proveedor", "X-3", "COP", new BigDecimal("100"), "Camisetas"));

        CatalogImportResult result = service.importRecords(records);

        assertEquals(1, result.created());
        assertEquals(3, result.failed());
        assertTrue(result.errors().get(0).startsWith("Registro 1:"));
        assertTrue(result.errors().get(0).contains("currency"));
        assertTrue(result.errors().get(1).contains("price"));
        assertEquals("Registro 3: está vacío", result.errors().get(2));
        verify(productRepository).save(any(Product.class));
    }

    private ProductImportRequest record(String provider, String externalId, String currency,
            BigDecimal price, String category) {
        return new ProductImportRequest(provider, externalId, "Camiseta", "Descripción",
                "https://example.com/img.jpg", price, currency, "https://example.com/p/1",
                category, true);
    }
}
