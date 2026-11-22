package co.edu.unbosque.tstyle.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.edu.unbosque.tstyle.client.MovieClient;
import co.edu.unbosque.tstyle.client.TmdbClient;
import co.edu.unbosque.tstyle.dto.MovieDetailResponse;
import co.edu.unbosque.tstyle.exception.BusinessException;

class MovieServiceTest {

	    private TmdbClient tmdbClient;
	    private MovieService service;

	    @BeforeEach
	    void setUp() {
	        tmdbClient = mock(TmdbClient.class);
	        service = new MovieService(tmdbClient);
	    }

	    @Test
	    void shouldPreferOfficialYouTubeTrailer() {
	        MovieClient.Videos videos = new MovieClient.Videos(List.of(
	                new MovieClient.Video("AAAAAA11111", "YouTube", "Trailer", false),
	                new MovieClient.Video("BBBBBB22222", "YouTube", "Trailer", true),
	                new MovieClient.Video("CCCCCC33333", "Vimeo", "Trailer", true)));
	        when(tmdbClient.getMovie(550L)).thenReturn(movieWith(videos));

	        MovieDetailResponse response = service.getById(550L);

	        assertEquals("https://www.youtube.com/watch?v=BBBBBB22222", response.trailerUrl());
	        assertEquals("https://image.tmdb.org/t/p/w500/p.jpg", response.posterUrl());
	    }

	    @Test
	    void shouldReturnNullTrailerWhenThereIsNone() {
	        when(tmdbClient.getMovie(550L)).thenReturn(movieWith(null));

	        assertNull(service.getById(550L).trailerUrl());
	    }

	    @Test
	    void shouldCacheSearchResults() {
	        when(tmdbClient.searchMovies("batman", 1))
	                .thenReturn(new MovieClient.SearchResponse(1, List.of(), 1, 0));

	        service.search("batman", 0);
	        service.search(" Batman ", 0);

	        verify(tmdbClient, times(1)).searchMovies("batman", 1);
	    }

	    @Test
	    void shouldRejectTooShortSearch() {
	        assertThrows(BusinessException.class, () -> service.search("a", 0));
	    }

	    @Test
	    void shouldRejectPageBeyondLimit() {
	        assertThrows(BusinessException.class, () -> service.search("batman", 20));
	    }

	    private MovieClient.Movie movieWith(MovieClient.Videos videos) {
	        return new MovieClient.Movie(550L, "Fight Club", "Resumen", "/p.jpg",
	                "1999-10-15", 8.4, videos);
	    }
}