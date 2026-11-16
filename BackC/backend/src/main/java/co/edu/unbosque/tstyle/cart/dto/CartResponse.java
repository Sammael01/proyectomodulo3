package co.edu.unbosque.tstyle.cart.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Resumen del carrito de un usuario.
 *
 * @param items líneas del carrito
 * @param totalItems total de unidades
 * @param total valor total calculado en el servidor
 *
 * @author Equipo T Style
 */
public record CartResponse(
        List<CartLineResponse> items,
        int totalItems,
        BigDecimal total) {
}
