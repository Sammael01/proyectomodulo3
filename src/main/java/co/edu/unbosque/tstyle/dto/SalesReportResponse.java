package co.edu.unbosque.tstyle.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SalesReportResponse(
        LocalDate from,
        LocalDate to,
        long orderCount,
        BigDecimal totalSales,
        BigDecimal averageTicket) {
}
