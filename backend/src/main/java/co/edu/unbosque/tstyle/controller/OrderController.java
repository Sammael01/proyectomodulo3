package co.edu.unbosque.tstyle.controller;

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

import co.edu.unbosque.tstyle.dto.CheckoutRequest;
import co.edu.unbosque.tstyle.dto.OrderResponse;
import co.edu.unbosque.tstyle.user.model.User;
import co.edu.unbosque.tstyle.security.CurrentUserService;
import co.edu.unbosque.tstyle.service.OrderAuditService;
import co.edu.unbosque.tstyle.service.OrderService;
import co.edu.unbosque.tstyle.service.ReceiptMailService;
import co.edu.unbosque.tstyle.service.ReceiptPdfService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Pedidos", description = "Checkout simulado, historial y comprobantes del usuario autenticado")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;
    private final ReceiptPdfService receiptPdfService;
    private final ReceiptMailService receiptMailService;
    private final OrderAuditService orderAuditService;
    private final CurrentUserService currentUserService;

    public OrderController(
            OrderService orderService,
            ReceiptPdfService receiptPdfService,
            ReceiptMailService receiptMailService,
            OrderAuditService orderAuditService,
            CurrentUserService currentUserService) {
        this.orderService = orderService;
        this.receiptPdfService = receiptPdfService;
        this.receiptMailService = receiptMailService;
        this.orderAuditService = orderAuditService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(
            @Valid @RequestBody(required = false) CheckoutRequest body, HttpServletRequest http) {
        User user = currentUserService.requireUser();

        OrderResponse created = orderService.checkout(user);

        orderAuditService.orderCreated(user, created, http, body);

        receiptMailService.sendReceipt(created.id());

        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.getOrder(user, created.id()));
    }

    @GetMapping
    public List<OrderResponse> listMine() {
        return orderService.listMine(currentUserService.requireUser());
    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable Long id) {
        return orderService.getOrder(currentUserService.requireUser(), id);
    }

    @GetMapping("/{id}/receipt")
    public ResponseEntity<byte[]> downloadReceipt(@PathVariable Long id) {
        OrderResponse order = orderService.getOrder(currentUserService.requireUser(), id);

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename("comprobante-" + order.orderNumber() + ".pdf")
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(receiptPdfService.generate(order));
    }

    @PostMapping("/{id}/resend-receipt")
    public OrderResponse resendReceipt(@PathVariable Long id) {
        User user = currentUserService.requireUser();

        orderService.getOrder(user, id);

        receiptMailService.sendReceipt(id);

        return orderService.getOrder(user, id);
    }
}
