package co.edu.unbosque.tstyle.order.controller;

import java.util.List;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.tstyle.audit.service.AuditService;
import co.edu.unbosque.tstyle.order.dto.CheckoutRequest;
import co.edu.unbosque.tstyle.order.dto.OrderDetailResponse;
import co.edu.unbosque.tstyle.order.dto.OrderSummaryResponse;
import co.edu.unbosque.tstyle.order.service.OrderService;
import co.edu.unbosque.tstyle.order.service.ReceiptMailService;
import co.edu.unbosque.tstyle.order.service.ReceiptPdfService;
import co.edu.unbosque.tstyle.security.CurrentUserService;
import co.edu.unbosque.tstyle.user.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * Controlador del checkout y de los pedidos del usuario autenticado.
 *
 * @author Equipo T Style
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final ReceiptPdfService receiptPdfService;
    private final ReceiptMailService receiptMailService;
    private final AuditService auditService;
    private final CurrentUserService currentUserService;

    /**
     * Constructor del controlador de pedidos.
     *
     * @param orderService servicio de pedidos
     * @param receiptPdfService generador del comprobante PDF
     * @param receiptMailService envío del comprobante por correo
     * @param auditService servicio de auditoría
     * @param currentUserService acceso al usuario autenticado
     */
    public OrderController(
            OrderService orderService,
            ReceiptPdfService receiptPdfService,
            ReceiptMailService receiptMailService,
            AuditService auditService,
            CurrentUserService currentUserService) {

        this.orderService = orderService;
        this.receiptPdfService = receiptPdfService;
        this.receiptMailService = receiptMailService;
        this.auditService = auditService;
        this.currentUserService = currentUserService;
    }

    /**
     * Realiza el checkout simulado del carrito.
     *
     * <p>Siempre responde 201 si el pedido se creó; el campo {@code emailStatus}
     * indica si el comprobante se envió (SENT), falló (FAILED) o está pendiente
     * (PENDING). Un fallo de correo no afecta el pedido.</p>
     *
     * @param body coordenadas opcionales del navegador (solo con permiso del usuario)
     * @param http solicitud HTTP, usada para la auditoría
     * @return pedido creado
     */
    @PostMapping("/checkout")
    public ResponseEntity<OrderDetailResponse> checkout(
            @Valid @RequestBody(required = false) CheckoutRequest body,
            HttpServletRequest http) {

        User user = currentUserService.requireUser();

        // 1) Transacción de base de datos: al regresar, el pedido ya está confirmado.
        OrderDetailResponse created = orderService.checkout(user);

        // 2) Auditoría y correo fuera de la transacción: no pueden afectar el pedido.
        auditService.registerEvent(
                user, "CHECKOUT", "Pedido " + created.orderNumber(), http,
                body == null ? null : body.latitude(),
                body == null ? null : body.longitude(),
                body == null ? null : body.accuracyMeters());

        receiptMailService.sendReceipt(created.id());

        // 3) Se vuelve a leer para devolver el estado final del correo.
        OrderDetailResponse result = orderService.getOrder(user, created.id());

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * Lista los pedidos del usuario.
     *
     * @return resúmenes de sus pedidos
     */
    @GetMapping
    public List<OrderSummaryResponse> listMine() {
        return orderService.listMine(currentUserService.requireUser());
    }

    /**
     * Consulta el detalle de un pedido propio (o cualquiera si es administrador).
     *
     * @param id identificador del pedido
     * @return detalle del pedido
     */
    @GetMapping("/{id}")
    public OrderDetailResponse getOrder(@PathVariable Long id) {
        return orderService.getOrder(currentUserService.requireUser(), id);
    }

    /**
     * Descarga el comprobante PDF de un pedido.
     *
     * @param id identificador del pedido
     * @return archivo PDF
     */
    @GetMapping("/{id}/receipt")
    public ResponseEntity<byte[]> downloadReceipt(@PathVariable Long id) {

        OrderDetailResponse order = orderService.getOrder(currentUserService.requireUser(), id);

        byte[] pdf = receiptPdfService.generate(order);

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename("comprobante-" + order.orderNumber() + ".pdf")
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(pdf);
    }

    /**
     * Reintenta el envío del comprobante al correo, sin crear otro pedido.
     * Revisa el campo {@code emailStatus} de la respuesta para saber el resultado.
     *
     * @param id identificador del pedido
     * @return pedido con el estado de correo actualizado
     */
    @PostMapping("/{id}/resend-receipt")
    public OrderDetailResponse resendReceipt(@PathVariable Long id) {

        User user = currentUserService.requireUser();

        // Valida que el pedido sea del usuario (o que sea administrador).
        orderService.getOrder(user, id);

        receiptMailService.sendReceipt(id);

        return orderService.getOrder(user, id);
    }
}
