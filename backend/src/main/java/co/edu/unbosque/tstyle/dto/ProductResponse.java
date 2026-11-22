package co.edu.unbosque.tstyle.dto;

import java.math.BigDecimal;

/**
 * Representa un producto del catálogo tal como lo consumen
 * el frontend, la wishlist y el carrito.
 *
 * @param id identificador interno del producto
 * @param provider nombre del proveedor de origen
 * @param externalId identificador del producto en el proveedor
 * @param name nombre del producto
 * @param imageUrl URL de la imagen entregada por el proveedor
 * @param price precio original informado por el proveedor
 * @param currency moneda del precio, por ejemplo COP
 * @param externalUrl URL de la página original del producto
 * @param providerAvailable disponibilidad informada por el proveedor,
 * o null cuando el proveedor no la informa
 * 
 * @author Sebastián
 */
public record ProductResponse(
	Long id,
	String provider,
	String externalId,
	String name,
	String imageUrl,
	BigDecimal price,
	String currency,
	String externalUrl,
	Boolean providerAvalaible) {
}
