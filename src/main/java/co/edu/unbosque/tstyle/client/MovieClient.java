package co.edu.unbosque.tstyle.client;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * Modelos internos de las respuestas de la API de TMDB
 * 
 * @author Sebastián
 */
public final class MovieClient {

	private MovieClient() {
		// Clase contenedora de modelos; no se instancia.
	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
    public record SearchResponse(
            int page,
            List<Movie> results,
            @JsonProperty("total_pages") int totalPages,
            @JsonProperty("total_results") long totalResults) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Movie(
            Long id,
            String title,
            String overview,
            @JsonProperty("poster_path") String posterPath,
            @JsonProperty("release_date") String releaseDate,
            @JsonProperty("vote_average") Double voteAverage,
            Videos videos) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Videos(List<Video> results) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Video(String key, String site, String type, Boolean official) {
    }
}
