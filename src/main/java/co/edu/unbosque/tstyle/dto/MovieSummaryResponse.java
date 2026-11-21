package co.edu.unbosque.tstyle.dto;

public record MovieSummaryResponse(Long id, String title, String overview, String posterUrl, String releaseDate,
		Double voteAverage) {

}
