package co.edu.unbosque.tstyle.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.unbosque.tstyle.config.SecurityConfig;
import co.edu.unbosque.tstyle.dto.MovieSummaryResponse;
import co.edu.unbosque.tstyle.dto.PageResponse;
import co.edu.unbosque.tstyle.dto.ProductResponse;
import co.edu.unbosque.tstyle.security.service.JwtService;
import co.edu.unbosque.tstyle.service.MovieService;
import co.edu.unbosque.tstyle.service.ProductService;
import co.edu.unbosque.tstyle.user.enums.Role;
import co.edu.unbosque.tstyle.user.model.User;
import co.edu.unbosque.tstyle.user.repository.UserRepository;

/**
 * Pruebas de autorización del catálogo con la seguridad JWT del módulo A:
 * la consulta de productos es pública y de solo lectura, las películas exigen
 * autenticación y las rutas administrativas siguen restringidas al rol ADMIN.
 *
 * @author Sebastián
 */
@WebMvcTest({ ProductController.class, MovieController.class })
@Import({ SecurityConfig.class, CatalogSecurityTest.TestConfig.class })
class CatalogSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private MovieService movieService;

    @Test
    void productListIsPublic() throws Exception {
        when(productService.search(isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageResponse<>(List.of(), 0, 12, 0, 0));

        mockMvc.perform(get("/api/products")).andExpect(status().isOk());
    }

    @Test
    void productDetailIsPublic() throws Exception {
        when(productService.getById(5L)).thenReturn(new ProductResponse(5L, "Proveedor", "X-1",
                "Camiseta", "https://example.com/img.jpg", new BigDecimal("59900"), "COP",
                "https://example.com/p/1", true));

        mockMvc.perform(get("/api/products/5")).andExpect(status().isOk());
    }

    @Test
    void productsCannotBeModifiedWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/products").contentType("application/json").content("{}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/products/5").contentType("application/json").content("{}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(patch("/api/products/5").contentType("application/json").content("{}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/products/5")).andExpect(status().isUnauthorized());
    }

    @Test
    void moviesRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/movies").param("query", "batman"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/movies/268")).andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedClientCanSearchMovies() throws Exception {
        when(movieService.search("batman", 0))
                .thenReturn(new PageResponse<MovieSummaryResponse>(List.of(), 0, 0, 0, 0));

        mockMvc.perform(get("/api/movies").param("query", "batman")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenFor(Role.CLIENT)))
                .andExpect(status().isOk());
    }

    @Test
    void adminRoutesRemainProtected() throws Exception {
        mockMvc.perform(get("/api/admin/test")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/admin/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenFor(Role.CLIENT)))
                .andExpect(status().isForbidden());
    }

    private String tokenFor(Role role) {
        User user = new User();
        user.setId(1L);
        user.setEmail(role.name().toLowerCase() + "@tstyle.test");
        user.setRole(role);
        user.setEnabled(true);
        user.setEmailVerified(true);
        user.setTokenVersion(0);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        return jwtService.generateToken(user);
    }

    /**
     * Dependencias auxiliares para generar y validar tokens JWT en las pruebas.
     */
    @TestConfiguration
    @EnableWebSecurity
    static class TestConfig {

        @Bean
        JwtService jwtService() {
            return new JwtService(Base64.getEncoder().encodeToString(new byte[64]), 60000);
        }

        @Bean
        UserRepository userRepository() {
            return org.mockito.Mockito.mock(UserRepository.class);
        }
    }
}
