package co.edu.unbosque.tstyle.dto;

import java.math.BigDecimal;

public record ProductResponse(
	Long id,
	String provider,
	String externalId,
	String name,
	String imageUrl,
	BigDecimal price,
	String currency,
	String externalUrl,
	Boolean providerAvalaible,
	int stock) {
}
