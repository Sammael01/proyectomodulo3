package co.edu.unbosque.tstyle.admin.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.tstyle.admin.dto.UpdateOrderStatusRequest;
import co.edu.unbosque.tstyle.admin.service.AdminOrderService;
import co.edu.unbosque.tstyle.audit.service.AuditService;
import co.edu.unbosque.tstyle.common.PageParams;
import co.edu.unbosque.tstyle.common.PageResponse;
import co.edu.unbosque.tstyle.order.dto.OrderDetailResponse;
import co.edu.unbosque.tstyle.order.dto.OrderSummaryResponse;
import co.edu.unbosque.tstyle.order.enums.EmailStatus;
import co.edu.unbosque.tstyle.order.enums.OrderStatus;
import co.edu.unbosque.tstyle.order.service.OrderService;
import co.edu.unbosque.tstyle.security.CurrentUserService;
import co.edu.unbosque.tstyle.user.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * Endpoints de administración de pedidos (solo rol ADMIN).
 *
 * @author Equipo T Style
 */
@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;
    private final OrderService orderService;
    private final AuditService auditService;
    private final CurrentUserService currentUserService;

    /**
     * Constructor del controlador de administración de pedidos.
     *
     * @param adminOrderService servicio de administración de pedidos
     * @param orderService servicio de pedidos
     * @param auditService servicio de auditoría
     * @param currentUserService acceso al usuario autenticado
     */
    public AdminOrderController(
            AdminOrderService adminOrderService,
            OrderService orderService,
            AuditService auditService,
            CurrentUserService currentUserService) {

        this.adminOrderService = adminOrderService;
        this.orderService = orderService;
        this.auditService = auditService;
        this.currentUserService = currentUserService;
    }

    /**
     * Lista pedidos con filtros opcionales. Para ver los de correo fallido:
     * {@code ?emailStatus=FAILED}.
     *
     * @param status estado del pedido
     * @param emailStatus estado del envío del comprobante
     * @param userId comprador
     * @param from fecha inicial (yyyy-MM-dd), incluida
     * @param to fecha final (yyyy-MM-dd), incluida
     * @param page número de página (desde 0)
     * @param size tamaño de página (máximo 100)
     * @return página de pedidos, del más reciente al más antiguo
     */
    @GetMapping
    public PageResponse<OrderSummaryResponse> search(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) EmailStatus emailStatus,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        currentUserService.requireAdmin();

        return adminOrderService.search(
                status, emailStatus, userId,
                from == null ? null : from.atStartOfDay(),
                to == null ? null : to.plusDays(1).atStartOfDay(),
                PageParams.newestFirst(page, size));
    }

    /**
     * Consulta el detalle de cualquier pedido, incluido el motivo del último
     * fallo de correo.
     *
     * @param id identificador del pedido
     * @return detalle del pedido
     */
    @GetMapping("/{id}")
    public OrderDetailResponse getOrder(@PathVariable Long id) {

        User admin = currentUserService.requireAdmin();

        return orderService.getOrder(admin, id);
    }

    /**
     * Cambia el estado de un pedido.
     *
     * @param id identificador del pedido
     * @param request nuevo estado
     * @param http solicitud HTTP, usada para la auditoría
     * @return pedido actualizado
     */
    @PatchMapping("/{id}/status")
    public OrderDetailResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            HttpServletRequest http) {

        User admin = currentUserService.requireAdmin();

        OrderDetailResponse updated = adminOrderService.updateStatus(id, request.status());

        auditService.registerEvent(
                admin, "ORDER_STATUS_CHANGED",
                "Pedido " + updated.orderNumber() + " a " + updated.status(),
                http, null, null, null);

        return updated;
    }
}
