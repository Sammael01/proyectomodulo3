package co.edu.unbosque.tstyle.admin.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.tstyle.admin.dto.SalesReportResponse;
import co.edu.unbosque.tstyle.admin.dto.TopProductResponse;
import co.edu.unbosque.tstyle.admin.dto.WishlistReportResponse;
import co.edu.unbosque.tstyle.admin.service.ReportService;
import co.edu.unbosque.tstyle.security.CurrentUserService;

/**
 * Endpoints de reportes de administración (solo rol ADMIN).
 * Si no se envían fechas, se usan los últimos 30 días.
 *
 * @author Equipo T Style
 */
@RestController
@RequestMapping("/api/admin/reports")
public class AdminReportController {

    private static final int DEFAULT_DAYS = 30;

    private final ReportService reportService;
    private final CurrentUserService currentUserService;

    /**
     * Constructor del controlador de reportes.
     *
     * @param reportService servicio de reportes
     * @param currentUserService acceso al usuario autenticado
     */
    public AdminReportController(
            ReportService reportService,
            CurrentUserService currentUserService) {

        this.reportService = reportService;
        this.currentUserService = currentUserService;
    }

    /**
     * Reporte de ventas del periodo con desglose diario.
     *
     * @param from primer día (yyyy-MM-dd)
     * @param to último día (yyyy-MM-dd)
     * @return reporte de ventas
     */
    @GetMapping("/sales")
    public SalesReportResponse sales(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        currentUserService.requireAdmin();

        LocalDate end = (to == null) ? LocalDate.now() : to;
        LocalDate start = (from == null) ? end.minusDays(DEFAULT_DAYS) : from;

        return reportService.sales(start, end);
    }

    /**
     * Productos más vendidos del periodo.
     *
     * @param from primer día (yyyy-MM-dd)
     * @param to último día (yyyy-MM-dd)
     * @param limit cantidad máxima de productos
     * @return productos más vendidos
     */
    @GetMapping("/top-products")
    public List<TopProductResponse> topProducts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "10") int limit) {

        currentUserService.requireAdmin();

        LocalDate end = (to == null) ? LocalDate.now() : to;
        LocalDate start = (from == null) ? end.minusDays(DEFAULT_DAYS) : from;

        return reportService.topProducts(start, end, limit);
    }

    /**
     * Productos más guardados en las wishlists.
     *
     * @param limit cantidad máxima de productos
     * @return productos más guardados
     */
    @GetMapping("/wishlist")
    public List<WishlistReportResponse> wishlist(
            @RequestParam(defaultValue = "10") int limit) {

        currentUserService.requireAdmin();

        return reportService.mostSavedInWishlists(limit);
    }
}
