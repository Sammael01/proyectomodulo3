package co.edu.unbosque.tstyle.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Ventas de un día dentro del reporte de ventas.
 *
 * @param date día
 * @param orders cantidad de pedidos
 * @param total valor vendido
 *
 * @author Equipo T Style
 */
public record DailySalesResponse(LocalDate date, long orders, BigDecimal total) {
}
