package co.edu.unbosque.tstyle.service;

import java.time.Clock;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import co.edu.unbosque.tstyle.cache.TtlCache;
import co.edu.unbosque.tstyle.client.MovieClient;
import co.edu.unbosque.tstyle.client.TmdbClient;
import co.edu.unbosque.tstyle.dto.MovieDetailResponse;
import co.edu.unbosque.tstyle.dto.MovieSummaryResponse;
import co.edu.unbosque.tstyle.dto.PageResponse;
import co.edu.unbosque.tstyle.exception.BusinessException;
import co.edu.unbosque.tstyle.exception.ExternalServiceException;

/**
 * Consulta de películas y tráilers a través de TMDB, con límites y caché.
 *
 * @author Sebastián
 */
@Service
public class MovieService {

    private static final String POSTER_BASE_URL = "https://image.tmdb.org/t/p/w500";
    private static final String YOUTUBE_WATCH_URL = "https://www.youtube.com/watch?v=";
    private static final int MIN_QUERY_LENGTH = 2;
    private static final int MAX_QUERY_LENGTH = 100;
    private static final int MAX_PAGE_INDEX = 19;

    private final TmdbClient tmdbClient;
    private final TtlCache<String, PageResponse<MovieSummaryResponse>> searchCache;
    private final TtlCache<Long, MovieDetailResponse> detailCache;

    /**
     * Constructor del servicio de películas.
     *
     * @param tmdbClient cliente de la API de TMDB
     */
    public MovieService(TmdbClient tmdbClient) {
        this.tmdbClient = tmdbClient;
        Clock clock = Clock.systemUTC();
        this.searchCache = new TtlCache<>(200, Duration.ofMinutes(10), clock);
        this.detailCache = new TtlCache<>(200, Duration.ofHours(1), clock);
    }

    /**
     * Busca películas por texto.
     *
     * @param query texto a buscar, entre 2 y 100 caracteres
     * @param page página empezando en 0, máximo 19
     * @return página de películas
     */
    public PageResponse<MovieSummaryResponse> search(String query, int page) {

        String term = query == null ? "" : query.trim();
        if (term.length() < MIN_QUERY_LENGTH || term.length() > MAX_QUERY_LENGTH) {
            throw new BusinessException("La búsqueda debe tener entre 2 y 100 caracteres");
        }
        if (page < 0 || page > MAX_PAGE_INDEX) {
            throw new BusinessException("La página debe estar entre 0 y 19");
        }

        String cacheKey = term.toLowerCase(Locale.ROOT) + "|" + page;
        var cached = searchCache.get(cacheKey);
        if (cached.isPresent()) {
            return cached.get();
        }

        MovieClient.SearchResponse result = tmdbClient.searchMovies(term, page + 1);
        if (result == null) {
            throw new ExternalServiceException("El servicio de películas no devolvió datos");
        }

        List<MovieSummaryResponse> content = result.results() == null
                ? List.of()
                : result.results().stream().map(this::toSummary).toList();

        PageResponse<MovieSummaryResponse> response = new PageResponse<>(content, page,
                content.size(), result.totalResults(),
                Math.min(result.totalPages(), MAX_PAGE_INDEX + 1));

        searchCache.put(cacheKey, response);
        return response;
    }

    /**
     * Consulta el detalle de una película con su tráiler, si existe.
     *
     * @param id identificador de la película en TMDB
     * @return detalle de la película
     */
    public MovieDetailResponse getById(long id) {

        if (id <= 0) {
            throw new BusinessException("El identificador de la película no es válido");
        }

        var cached = detailCache.get(id);
        if (cached.isPresent()) {
            return cached.get();
        }

        MovieClient.Movie movie = tmdbClient.getMovie(id);
        if (movie == null) {
            throw new ExternalServiceException("El servicio de películas no devolvió datos");
        }

        MovieDetailResponse response = new MovieDetailResponse(movie.id(), movie.title(),
                movie.overview(), posterUrl(movie.posterPath()), movie.releaseDate(),
                movie.voteAverage(), findTrailerUrl(movie));

        detailCache.put(id, response);
        return response;
    }

    private MovieSummaryResponse toSummary(MovieClient.Movie movie) {
        return new MovieSummaryResponse(movie.id(), movie.title(), movie.overview(),
                posterUrl(movie.posterPath()), movie.releaseDate(), movie.voteAverage());
    }

    private String posterUrl(String posterPath) {
        return (posterPath == null || posterPath.isBlank()) ? null : POSTER_BASE_URL + posterPath;
    }

    private String findTrailerUrl(MovieClient.Movie movie) {
        if (movie.videos() == null || movie.videos().results() == null) {
            return null;
        }
        return movie.videos().results().stream()
                .filter(video -> "YouTube".equals(video.site())
                        && "Trailer".equals(video.type())
                        && video.key() != null
                        && video.key().matches("[A-Za-z0-9_-]{6,20}"))
                .sorted(Comparator.comparing(
                        (MovieClient.Video video) -> !Boolean.TRUE.equals(video.official())))
                .findFirst()
                .map(video -> YOUTUBE_WATCH_URL + video.key())
                .orElse(null);
    }
}