package co.edu.unbosque.tstyle.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.tstyle.dto.CatalogImportResult;
import co.edu.unbosque.tstyle.dto.ProductImportRequest;
import co.edu.unbosque.tstyle.model.Category;
import co.edu.unbosque.tstyle.model.Product;
import co.edu.unbosque.tstyle.model.Provider;
import co.edu.unbosque.tstyle.repository.CategoryRepository;
import co.edu.unbosque.tstyle.repository.ProductRepository;
import co.edu.unbosque.tstyle.repository.ProviderRepository;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

/**
 * Importa y valida registros del JSON de datos del catálogo.
 * Un producto se identifica por proveedor e identificador externo,
 * nunca por su título.
 *
 * @author Sebastián
 */
@Service
public class CatalogImportService {

    private final ProviderRepository providerRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final Validator validator;

    public CatalogImportService(ProviderRepository providerRepository,
            CategoryRepository categoryRepository,
            ProductRepository productRepository,
            Validator validator) {
        this.providerRepository = providerRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.validator = validator;
    }

    /**
     * Importa una lista de registros. Los registros inválidos se omiten
     * y se reportan, sin detener la importación de los demás.
     *
     *
     */
    @Transactional
    public CatalogImportResult importRecords(List<ProductImportRequest> records) {

        int created = 0;
        int updated = 0;
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < records.size(); i++) {
            ProductImportRequest record = records.get(i);
            int position = i + 1;

            if (record == null) {
                errors.add("Registro " + position + ": está vacío");
                continue;
            }

            Set<ConstraintViolation<ProductImportRequest>> violations = validator.validate(record);
            if (!violations.isEmpty()) {
                errors.add("Registro " + position + ": " + describe(violations));
                continue;
            }

            if (upsert(record)) {
                created++;
            } else {
                updated++;
            }
        }

        return new CatalogImportResult(created, updated, errors.size(), List.copyOf(errors));
    }

    private boolean upsert(ProductImportRequest record) {

        String providerName = record.provider().trim();
        String categoryName = record.category().trim();

        Provider provider = providerRepository.findByName(providerName)
                .orElseGet(() -> providerRepository.save(new Provider(providerName)));

        Category category = categoryRepository.findByName(categoryName)
                .orElseGet(() -> categoryRepository.save(new Category(categoryName)));

        Optional<Product> existing = productRepository
                .findByProviderIdAndExternalId(provider.getId(), record.externalId().trim());

        Product product = existing.orElseGet(Product::new);

        product.setProvider(provider);
        product.setCategory(category);
        product.setExternalId(record.externalId().trim());
        product.setName(record.name().trim());
        product.setDescription(record.description());
        product.setImageUrl(record.imageUrl().trim());
        product.setPrice(record.price());
        product.setCurrency(record.currency().trim().toUpperCase(Locale.ROOT));
        product.setExternalUrl(record.externalUrl().trim());
        product.setProviderAvailable(record.providerAvailable());
        product.setLastSyncedAt(LocalDateTime.now());

        productRepository.save(product);

        return existing.isEmpty();
    }

    private String describe(Set<ConstraintViolation<ProductImportRequest>> violations) {
        return violations.stream()
                .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                .sorted()
                .collect(Collectors.joining("; "));
    }
}