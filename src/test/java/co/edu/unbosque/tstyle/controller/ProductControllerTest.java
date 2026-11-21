package co.edu.unbosque.tstyle.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.unbosque.tstyle.dto.PageResponse;
import co.edu.unbosque.tstyle.dto.ProductResponse;
import co.edu.unbosque.tstyle.exception.ResourceNotFoundException;
import co.edu.unbosque.tstyle.service.ProductService;

/**
 * Pruebas HTTP del catálogo de productos.
 *
 * @author Sebastián
 */
@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    void shouldListProductsWithDefaultPagination() throws Exception {
        when(productService.search(isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageResponse<>(List.of(response()), 0, 12, 1, 1));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Camiseta"))
                .andExpect(jsonPath("$.content[0].providerAvailable").value(true))
                .andExpect(jsonPath("$.totalElements").value(1));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(productService).search(isNull(), isNull(), isNull(), captor.capture());
        assertEquals(0, captor.getValue().getPageNumber());
        assertEquals(12, captor.getValue().getPageSize());
        assertEquals(Sort.Direction.ASC, captor.getValue().getSort().getOrderFor("name").getDirection());
    }

    @Test
    void shouldCapPageSizeAtFifty() throws Exception {
        when(productService.search(isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageResponse<>(List.of(), 0, 50, 0, 0));

        mockMvc.perform(get("/api/products").param("size", "500")).andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(productService).search(isNull(), isNull(), isNull(), captor.capture());
        assertEquals(50, captor.getValue().getPageSize());
    }

    @Test
    void shouldRejectUnknownSortField() throws Exception {
        mockMvc.perform(get("/api/products").param("sortBy", "password"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BUSINESS_ERROR"));
    }

    @Test
    void shouldRejectTooLongSearch() throws Exception {
        mockMvc.perform(get("/api/products").param("search", "a".repeat(101)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BUSINESS_ERROR"));
    }

    @Test
    void shouldRejectNonNumericCategory() throws Exception {
        mockMvc.perform(get("/api/products").param("categoryId", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void shouldReturnProductById() throws Exception {
        when(productService.getById(5L)).thenReturn(response());

        mockMvc.perform(get("/api/products/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalUrl").value("https://example.com/p/1"));
    }

    @Test
    void shouldReturnNotFoundForUnknownProduct() throws Exception {
        when(productService.getById(99L))
                .thenThrow(new ResourceNotFoundException("No se encontró el producto con id 99"));

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    private ProductResponse response() {
        return new ProductResponse(5L, "Proveedor", "X-1", "Camiseta",
                "https://example.com/img.jpg", new BigDecimal("59900"), "COP",
                "https://example.com/p/1", true);
    }
}
