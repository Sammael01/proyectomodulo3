package co.edu.unbosque.tstyle.user.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.unbosque.tstyle.config.SecurityConfig;
import co.edu.unbosque.tstyle.controller.MovieController;
import co.edu.unbosque.tstyle.dto.MovieDetailResponse;
import co.edu.unbosque.tstyle.dto.MovieSummaryResponse;
import co.edu.unbosque.tstyle.dto.PageResponse;
import co.edu.unbosque.tstyle.exception.BusinessException;
import co.edu.unbosque.tstyle.exception.ExternalServiceException;
import co.edu.unbosque.tstyle.security.service.JwtService;
import co.edu.unbosque.tstyle.service.MovieService;
import co.edu.unbosque.tstyle.user.repository.UserRepository;

/**
 * Pruebas HTTP de la consulta de películas.
 *
 * @author Sebastián
 */
@WebMvcTest(MovieController.class)
@Import({ SecurityConfig.class, MovieControllerTest.TestConfig.class })
@WithMockUser
class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MovieService movieService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void shouldSearchMovies() throws Exception {
        MovieSummaryResponse movie = new MovieSummaryResponse(268L, "Batman", "Resumen",
                "https://image.tmdb.org/t/p/w500/a.jpg", "1989-06-23", 7.2);
        when(movieService.search("batman", 0))
                .thenReturn(new PageResponse<>(List.of(movie), 0, 1, 1, 1));

        mockMvc.perform(get("/api/movies").param("query", "batman"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Batman"));
    }

    @Test
    void shouldReturnMovieWithTrailer() throws Exception {
        when(movieService.getById(268L)).thenReturn(new MovieDetailResponse(268L, "Batman",
                "Resumen", null, "1989-06-23", 7.2, "https://www.youtube.com/watch?v=AAAAAA11111"));

        mockMvc.perform(get("/api/movies/268"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trailerUrl").value("https://www.youtube.com/watch?v=AAAAAA11111"));
    }

    @Test
    void shouldReturnBadRequestForInvalidSearch() throws Exception {
        when(movieService.search("a", 0))
                .thenThrow(new BusinessException("La búsqueda debe tener entre 2 y 100 caracteres"));

        mockMvc.perform(get("/api/movies").param("query", "a"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BUSINESS_ERROR"));
    }

    @Test
    void shouldReturnServiceUnavailableWhenTmdbFails() throws Exception {
        when(movieService.getById(268L))
                .thenThrow(new ExternalServiceException("El servicio de películas no está disponible en este momento"));

        mockMvc.perform(get("/api/movies/268"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("EXTERNAL_SERVICE_ERROR"));
    }

    /**
     * Activa Spring Security en la prueba, igual que en SecurityConfigTest.
     */
    @TestConfiguration
    @EnableWebSecurity
    static class TestConfig {
    }
}
