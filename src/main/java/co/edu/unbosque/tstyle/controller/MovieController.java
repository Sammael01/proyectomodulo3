package co.edu.unbosque.tstyle.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.tstyle.dto.MovieDetailResponse;
import co.edu.unbosque.tstyle.dto.MovieSummaryResponse;
import co.edu.unbosque.tstyle.dto.PageResponse;
import co.edu.unbosque.tstyle.service.MovieService;

/**
 * Controlador de consulta de películas y tráilers.
 *
 * @author Sebastián
 */
@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    /**
     * Busca películas por texto.
     *
     */
    @GetMapping
    public ResponseEntity<PageResponse<MovieSummaryResponse>> search(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(movieService.search(query, page));
    }

    /**
     * Consulta una película con su tráiler.
     *
     * @param id identificador de la película en TMDB
     * @return detalle de la película
     */
    @GetMapping("/{id}")
    public ResponseEntity<MovieDetailResponse> getById(@PathVariable long id) {
        return ResponseEntity.ok(movieService.getById(id));
    }
}