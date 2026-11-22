package co.edu.unbosque.tstyle.client;

import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import co.edu.unbosque.tstyle.exception.ExternalServiceException;
import co.edu.unbosque.tstyle.exception.ResourceNotFoundException;

/**
 * Cliente de la API de TMDB. El token se lee de la variable de entorno
 * TMDB_ACCESS_TOKEN y nunca se registra ni se devuelve al cliente.
 *
 * @author Sebastián
 */
@Component
public class TmdbClient {

    private static final String LANGUAGE = "es-ES";

    private final RestClient restClient;
    private final boolean configured;

    /**
     * Constructor del cliente.
     *
     * @param builder constructor de RestClient configurado por Spring Boot
     * @param baseUrl URL base de la API de TMDB
     * @param accessToken token de lectura (Bearer) de TMDB
     */
    public TmdbClient(RestClient.Builder builder,
            @Value("${tmdb.base-url:https://api.themoviedb.org/3}") String baseUrl,
            @Value("${eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI3ZjdhNzA0YTc0YzFkZWVmZmU0YzI1OWFjYjk2ODhiZCIsIm5iZiI6MTc5NDk5Nzk1MC4xOTYsInN1YiI6IjZhZmQ3ZWJlMzY0NDExZGM2YmYxODE4MiIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.3SG5OSoo-5nRmCjW1qLcpXnfCmvrdIWhb2kcohqrgt0}") String accessToken) {

        this.configured = accessToken != null && !accessToken.isBlank();

        builder.baseUrl(baseUrl);
        if (configured) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.trim());
        }
        this.restClient = builder.build();
    }

    /**
     * Busca películas por texto.
     *
     * @param query texto a buscar
     * @param page página de TMDB, empezando en 1
     * @return resultados de la búsqueda
     */
    public MovieClient.SearchResponse searchMovies(String query, int page) {
        return execute(() -> restClient.get()
                .uri("/search/movie?query={query}&language={language}&page={page}",
                        query, LANGUAGE, page)
                .retrieve()
                .body(MovieClient.SearchResponse.class));
    }

    /**
     * Consulta el detalle de una película, incluyendo sus videos.
     *
     * @param id identificador de la película en TMDB
     * @return detalle de la película
     */
    public MovieClient.Movie getMovie(long id) {
        return execute(() -> restClient.get()
                .uri("/movie/{id}?language={language}&append_to_response=videos"
                        + "&include_video_language=es,en", id, LANGUAGE)
                .retrieve()
                .body(MovieClient.Movie.class));
    }

    private <T> T execute(Supplier<T> call) {
        if (!configured) {
            throw new ExternalServiceException("El servicio de películas no está configurado");
        }
        try {
            return call.get();
        } catch (HttpClientErrorException.NotFound exception) {
            throw new ResourceNotFoundException("No se encontró la película solicitada");
        } catch (RestClientException exception) {
            throw new ExternalServiceException(
                    "El servicio de películas no está disponible en este momento");
        }
    }
}
