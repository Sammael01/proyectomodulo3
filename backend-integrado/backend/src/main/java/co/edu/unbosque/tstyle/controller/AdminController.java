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
import co.edu.unbosque.tstyle.user.model.User;
import co.edu.unbosque.tstyle.security.CurrentUserService;
import co.edu.unbosque.tstyle.service.AdminService;
import co.edu.unbosque.tstyle.audit.service.AuditService;
import co.edu.unbosque.tstyle.dto.AuditEventResponse;
import co.edu.unbosque.tstyle.service.OrderAuditService;
import co.edu.unbosque.tstyle.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Administración", description = "Gestión de pedidos, reportes y auditoría para administradores")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private static final int DEFAULT_REPORT_DAYS = 30;
    private static final int MAX_PAGE_SIZE = 100;

    private final AdminService adminService;
    private final OrderService orderService;
    private final AuditService auditService;
    private final OrderAuditService orderAuditService;
    private final CurrentUserService currentUserService;

    public AdminController(
            AdminService adminService,
            OrderService orderService,
            AuditService auditService,
            OrderAuditService orderAuditService,
            CurrentUserService currentUserService) {
        this.adminService = adminService;
        this.orderService = orderService;
        this.auditService = auditService;
        this.orderAuditService = orderAuditService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/orders")
    public PageResponse<OrderResponse> listOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) EmailStatus emailStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        currentUserService.requireAdmin();

        return adminService.listOrders(status, emailStatus, newestFirst(page, size));
    }

    @GetMapping("/orders/{id}")
    public OrderResponse getOrder(@PathVariable Long id) {
        return orderService.getOrder(currentUserService.requireAdmin(), id);
    }

    @PatchMapping("/orders/{id}/status")
    public OrderResponse updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            HttpServletRequest http) {
        User admin = currentUserService.requireAdmin();

        OrderResponse updated = adminService.updateStatus(id, request.status());

        orderAuditService.orderStatusChanged(admin, updated, http);

        return updated;
    }

    @GetMapping("/reports/sales")
    public SalesReportResponse salesReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        currentUserService.requireAdmin();

        LocalDate end = (to == null) ? LocalDate.now() : to;
        LocalDate start = (from == null) ? end.minusDays(DEFAULT_REPORT_DAYS) : from;

        return adminService.salesReport(start, end);
    }

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

    @GetMapping("/reports/wishlist")
    public List<WishlistReportResponse> wishlistReport(@RequestParam(defaultValue = "10") int limit) {
        currentUserService.requireAdmin();

        return adminService.wishlistReport(limit);
    }

    @GetMapping("/audit")
    public PageResponse<AuditEventResponse> audit(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        currentUserService.requireAdmin();

        return PageResponse.from(auditService.findAll(newestFirst(page, size)).map(AuditEventResponse::from));
    }

    @GetMapping("/audit/locations")
    public PageResponse<AuditEventResponse> auditLocations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        currentUserService.requireAdmin();

        return PageResponse.from(auditService.findWithLocation(newestFirst(page, size)).map(AuditEventResponse::from));
    }

    private Pageable newestFirst(int page, int size) {
        return PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, "timestamp"));
    }
}
