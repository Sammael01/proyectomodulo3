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

/**
 * Controlador de consulta pública del catálogo de productos.
 *
 * @author Equipo T Style
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final int MAX_PAGE_SIZE = 50;
    private static final int MAX_SEARCH_LENGTH = 100;
    private static final Set<String> SORT_FIELDS = Set.of("name", "price", "id");

    private final ProductService productService;

    /**
     * Constructor del controlador de productos.
     *
     * @param productService servicio de consulta del catálogo
     */
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Lista productos con filtros, paginación y orden.
     *
     * @param categoryId categoría a filtrar, opcional
     * @param providerId proveedor a filtrar, opcional
     * @param search texto contenido en el nombre, opcional (máximo 100 caracteres)
     * @param page página empezando en 0
     * @param size tamaño de página, entre 1 y 50
     * @param sortBy campo de orden: name, price o id
     * @param direction asc o desc
     * @return página de productos
     */
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

        if (search != null && search.length() > MAX_SEARCH_LENGTH) {
            throw new BusinessException("La búsqueda no puede superar 100 caracteres");
        }

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        PageRequest pageable = PageRequest.of(safePage, safeSize, Sort.by(sortDirection, sortBy));

        return ResponseEntity.ok(productService.search(categoryId, providerId, search, pageable));
    }

    /**
     * Consulta un producto por su identificador.
     *
     * @param id identificador interno del producto
     * @return producto encontrado
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }
}