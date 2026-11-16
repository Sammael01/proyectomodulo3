package co.edu.unbosque.tstyle.admin.dto;

import java.math.BigDecimal;

/**
 * Producto dentro del reporte de más vendidos.
 *
 * @param productId identificador del producto
 * @param name nombre del producto
 * @param unitsSold unidades vendidas
 * @param revenue valor vendido
 *
 * @author Equipo T Style
 */
public record TopProductResponse(
        Long productId,
        String name,
        long unitsSold,
        BigDecimal revenue) {
}
