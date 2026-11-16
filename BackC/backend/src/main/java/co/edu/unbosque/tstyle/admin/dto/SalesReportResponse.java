package co.edu.unbosque.tstyle.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Reporte de ventas de un periodo. Los pedidos cancelados no se cuentan.
 *
 * @param from primer día del periodo
 * @param to último día del periodo (incluido)
 * @param orderCount cantidad de pedidos
 * @param totalSales valor total vendido
 * @param averageTicket valor promedio por pedido
 * @param daily desglose por día
 *
 * @author Equipo T Style
 */
public record SalesReportResponse(
        LocalDate from,
        LocalDate to,
        long orderCount,
        BigDecimal totalSales,
        BigDecimal averageTicket,
        List<DailySalesResponse> daily) {
}
