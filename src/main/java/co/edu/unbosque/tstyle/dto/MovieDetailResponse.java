package co.edu.unbosque.tstyle.dto;

public record MovieDetailResponse(Long id, String title, String overview, String posterUrl, String releaseDate,
		Double voteAverage, String trailerUrl) {

}
