package co.edu.unbosque.tstyle.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.tstyle.dto.OrderResponse;
import co.edu.unbosque.tstyle.dto.PageResponse;
import co.edu.unbosque.tstyle.dto.SalesReportResponse;
import co.edu.unbosque.tstyle.dto.TopProductResponse;
import co.edu.unbosque.tstyle.dto.UpdateOrderStatusRequest;
import co.edu.unbosque.tstyle.dto.WishlistReportResponse;
import co.edu.unbosque.tstyle.enums.EmailStatus;
import co.edu.unbosque.tstyle.enums.OrderStatus;
import co.edu.unbosque.tstyle.model.AuditLog;
import co.edu.unbosque.tstyle.model.User;
import co.edu.unbosque.tstyle.security.CurrentUserService;
import co.edu.unbosque.tstyle.service.AdminService;
import co.edu.unbosque.tstyle.service.AuditService;
import co.edu.unbosque.tstyle.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * Endpoints de administración (solo rol ADMIN): pedidos, reportes y auditoría.
 * Los reportes usan por defecto los últimos 30 días.
 *
 * @author Equipo T Style
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final int DEFAULT_REPORT_DAYS = 30;
    private static final int MAX_PAGE_SIZE = 100;

    private final AdminService adminService;
    private final OrderService orderService;
    private final AuditService auditService;
    private final CurrentUserService currentUserService;

    /**
     * Constructor del controlador de administración.
     *
     * @param adminService servicio de administración
     * @param orderService servicio de pedidos
     * @param auditService servicio de auditoría
     * @param currentUserService acceso al usuario autenticado
     */
    public AdminController(
            AdminService adminService,
            OrderService orderService,
            AuditService auditService,
            CurrentUserService currentUserService) {

        this.adminService = adminService;
        this.orderService = orderService;
        this.auditService = auditService;
        this.currentUserService = currentUserService;
    }

    // ------------------------------------------------------------ pedidos

    /**
     * Lista pedidos con filtros opcionales. Para ver los comprobantes no enviados:
     * {@code ?emailStatus=FAILED}.
     *
     * @param status estado del pedido
     * @param emailStatus estado del envío del comprobante
     * @param page número de página (desde 0)
     * @param size tamaño de página (máximo 100)
     * @return página de pedidos, del más reciente al más antiguo
     */
    @GetMapping("/orders")
    public PageResponse<OrderResponse> listOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) EmailStatus emailStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        currentUserService.requireAdmin();

        return adminService.listOrders(status, emailStatus, newestFirst(page, size));
    }

    /**
     * Consulta cualquier pedido, incluido el motivo del último fallo de correo.
     *
     * @param id identificador del pedido
     * @return pedido
     */
    @GetMapping("/orders/{id}")
    public OrderResponse getOrder(@PathVariable Long id) {
        return orderService.getOrder(currentUserService.requireAdmin(), id);
    }

    /**
     * Cambia el estado de un pedido.
     *
     * @param id identificador del pedido
     * @param request nuevo estado
     * @param http solicitud HTTP, usada para la auditoría
     * @return pedido actualizado
     */
    @PatchMapping("/orders/{id}/status")
    public OrderResponse updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            HttpServletRequest http) {

        User admin = currentUserService.requireAdmin();

        OrderResponse updated = adminService.updateStatus(id, request.status());

        auditService.registerEvent(
                admin, "ORDER_STATUS_CHANGED",
                "Pedido " + updated.orderNumber() + " a " + updated.status(),
                http, null, null, null);

        return updated;
    }

    // ----------------------------------------------------------- reportes

    /**
     * Reporte de ventas del periodo.
     *
     * @param from primer día (yyyy-MM-dd)
     * @param to último día (yyyy-MM-dd)
     * @return totales del periodo
     */
    @GetMapping("/reports/sales")
    public SalesReportResponse salesReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        currentUserService.requireAdmin();

        LocalDate end = (to == null) ? LocalDate.now() : to;
        LocalDate start = (from == null) ? end.minusDays(DEFAULT_REPORT_DAYS) : from;

        return adminService.salesReport(start, end);
    }

    /**
     * Productos más vendidos del periodo.
     *
     * @param from primer día (yyyy-MM-dd)
     * @param to último día (yyyy-MM-dd)
     * @param limit cantidad máxima de productos
     * @return productos más vendidos
     */
    @GetMapping("/reports/top-products")
    public List<TopProductResponse> topProducts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "10") int limit) {

        currentUserService.requireAdmin();

        LocalDate end = (to == null) ? LocalDate.now() : to;
        LocalDate start = (from == null) ? end.minusDays(DEFAULT_REPORT_DAYS) : from;

        return adminService.topProducts(start, end, limit);
    }

    /**
     * Productos más guardados en las wishlists.
     *
     * @param limit cantidad máxima de productos
     * @return productos más guardados
     */
    @GetMapping("/reports/wishlist")
    public List<WishlistReportResponse> wishlistReport(@RequestParam(defaultValue = "10") int limit) {

        currentUserService.requireAdmin();

        return adminService.wishlistReport(limit);
    }

    // ---------------------------------------------------------- auditoría

    /**
     * Consulta los eventos de auditoría, del más reciente al más antiguo.
     *
     * @param page número de página (desde 0)
     * @param size tamaño de página (máximo 100)
     * @return página de eventos
     */
    @GetMapping("/audit")
    public PageResponse<AuditLog> audit(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        currentUserService.requireAdmin();

        return auditService.list(newestFirst(page, size));
    }

    /**
     * Eventos con coordenadas (enviadas por el navegador con permiso del usuario),
     * listos para dibujarse como marcadores en Google Maps. Los eventos que solo
     * tienen IP no traen coordenadas.
     *
     * @param page número de página (desde 0)
     * @param size tamaño de página (máximo 100)
     * @return página de eventos con ubicación
     */
    @GetMapping("/audit/locations")
    public PageResponse<AuditLog> auditLocations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {

        currentUserService.requireAdmin();

        return auditService.locations(newestFirst(page, size));
    }

    private Pageable newestFirst(int page, int size) {
        return PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, "createdAt"));
    }
}
