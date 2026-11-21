package co.edu.unbosque.tstyle.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import co.edu.unbosque.tstyle.exception.ExternalServiceException;
import co.edu.unbosque.tstyle.exception.ResourceNotFoundException;

class TmdbClientTest {

	 private static final String BASE_URL = "https://api.themoviedb.org/3";
	    private static final String TEST_TOKEN = "token-de-prueba";

	    @Test
	    void shouldSendBearerTokenAndParseSearchResults() {
	        RestClient.Builder builder = RestClient.builder();
	        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
	        TmdbClient client = new TmdbClient(builder, BASE_URL, TEST_TOKEN);

	        server.expect(request -> assertTrue(
	                        request.getURI().getPath().endsWith("/search/movie")
	                                && request.getURI().getQuery().contains("query=batman")))
	                .andExpect(method(HttpMethod.GET))
	                .andExpect(header("Authorization", "Bearer " + TEST_TOKEN))
	                .andRespond(withSuccess("""
	                        {"page":1,"total_pages":3,"total_results":45,
	                         "results":[{"id":268,"title":"Batman","poster_path":"/a.jpg",
	                                     "release_date":"1989-06-23","vote_average":7.2,
	                                     "campo_desconocido":"se ignora"}]}
	                        """, MediaType.APPLICATION_JSON));

	        MovieClient.SearchResponse result = client.searchMovies("batman", 1);

	        assertEquals(45, result.totalResults());
	        assertEquals("Batman", result.results().get(0).title());
	        assertEquals("/a.jpg", result.results().get(0).posterPath());
	        server.verify();
	    }

	    @Test
	    void shouldMapNotFoundToResourceNotFound() {
	        RestClient.Builder builder = RestClient.builder();
	        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
	        TmdbClient client = new TmdbClient(builder, BASE_URL, TEST_TOKEN);

	        server.expect(request -> { }).andRespond(withResourceNotFound());

	        assertThrows(ResourceNotFoundException.class, () -> client.getMovie(1L));
	    }

	    @Test
	    void shouldMapServerErrorToExternalServiceException() {
	        RestClient.Builder builder = RestClient.builder();
	        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
	        TmdbClient client = new TmdbClient(builder, BASE_URL, TEST_TOKEN);

	        server.expect(request -> { }).andRespond(withServerError());

	        assertThrows(ExternalServiceException.class, () -> client.getMovie(1L));
	    }

	    @Test
	    void shouldFailClearlyWhenTokenIsMissing() {
	        TmdbClient client = new TmdbClient(RestClient.builder(), BASE_URL, "");

	        assertThrows(ExternalServiceException.class, () -> client.searchMovies("batman", 1));
	    }
}