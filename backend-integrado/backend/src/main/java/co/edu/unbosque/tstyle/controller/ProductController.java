package co.edu.unbosque.tstyle.controller;

import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.tstyle.exception.BusinessException;
import co.edu.unbosque.tstyle.dto.PageResponse;
import co.edu.unbosque.tstyle.dto.ProductResponse;
import co.edu.unbosque.tstyle.service.ProductService;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Productos", description = "Consulta pública del catálogo de productos")
@RestController
@RequestMapping("/api/products")
public class ProductController {
    private static final int MAX_PAGE_SIZE = 50;
    private static final Set<String> SORT_FIELDS = Set.of("name", "price", "id");

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<ProductResponse>> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long providerId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        if (!SORT_FIELDS.contains(sortBy)) {
            throw new BusinessException("Campo de orden no permitido: " + sortBy);
        }

        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction)
                ? Sort.Direction.DESC : Sort.Direction.ASC;

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        PageRequest pageable = PageRequest.of(safePage, safeSize, Sort.by(sortDirection, sortBy));

        return ResponseEntity.ok(productService.search(categoryId, providerId, search, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }
}
