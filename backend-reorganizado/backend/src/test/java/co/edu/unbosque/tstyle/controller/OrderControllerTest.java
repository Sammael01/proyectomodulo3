package co.edu.unbosque.tstyle.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.unbosque.tstyle.dto.OrderItemResponse;
import co.edu.unbosque.tstyle.dto.OrderResponse;
import co.edu.unbosque.tstyle.enums.EmailStatus;
import co.edu.unbosque.tstyle.enums.OrderStatus;
import co.edu.unbosque.tstyle.enums.Role;
import co.edu.unbosque.tstyle.model.User;
import co.edu.unbosque.tstyle.security.CurrentUserService;
import co.edu.unbosque.tstyle.service.AuditService;
import co.edu.unbosque.tstyle.service.OrderService;
import co.edu.unbosque.tstyle.service.ReceiptMailService;
import co.edu.unbosque.tstyle.service.ReceiptPdfService;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Pruebas HTTP para el controlador de pedidos.
 *
 * @author Equipo T Style
 */
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private ReceiptPdfService receiptPdfService;

    @MockitoBean
    private ReceiptMailService receiptMailService;

    @MockitoBean
    private AuditService auditService;

    @MockitoBean
    private CurrentUserService currentUserService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("Ana Pérez", "ana@tstyle.com", "hash", Role.CLIENT);
        user.setId(1L);

        when(currentUserService.requireUser()).thenReturn(user);
    }

    private OrderResponse order(EmailStatus emailStatus) {
        return new OrderResponse(
                1L, "TS-2026-000001", LocalDateTime.of(2026, 11, 15, 10, 30),
                OrderStatus.PAID, 1L, "Ana Pérez", "ana@tstyle.com",
                List.of(new OrderItemResponse(10L, "Camisa", new BigDecimal("50000.00"), 2,
                        new BigDecimal("100000.00"))),
                new BigDecimal("100000.00"), emailStatus, null, null);
    }

    @Test
    void checkoutReturnsCreatedOrderEvenWhenReceiptEmailFailed() throws Exception {

        when(orderService.checkout(user)).thenReturn(order(EmailStatus.PENDING));
        // Después del intento de correo, la lectura final refleja el fallo.
        when(orderService.getOrder(user, 1L)).thenReturn(order(EmailStatus.FAILED));

        mockMvc.perform(post("/api/orders/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNumber").value("TS-2026-000001"))
                .andExpect(jsonPath("$.emailStatus").value("FAILED"));

        // El pedido se crea una sola vez y el correo se intenta aparte.
        verify(orderService, times(1)).checkout(user);
        verify(receiptMailService).sendReceipt(1L);
    }

    @Test
    void checkoutRegistersGpsOnlyWhenBrowserSentCoordinates() throws Exception {

        when(orderService.checkout(user)).thenReturn(order(EmailStatus.SENT));
        when(orderService.getOrder(user, 1L)).thenReturn(order(EmailStatus.SENT));

        mockMvc.perform(post("/api/orders/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"latitude\": 4.711, \"longitude\": -74.0721, \"accuracyMeters\": 25.0}"))
                .andExpect(status().isCreated());

        verify(auditService).registerEvent(
                eq(user), eq("CHECKOUT"), anyString(), any(HttpServletRequest.class),
                eq(4.711), eq(-74.0721), eq(25.0));
    }

    @Test
    void checkoutWithoutCoordinatesRegistersOnlyTheIp() throws Exception {

        when(orderService.checkout(user)).thenReturn(order(EmailStatus.SENT));
        when(orderService.getOrder(user, 1L)).thenReturn(order(EmailStatus.SENT));

        mockMvc.perform(post("/api/orders/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isCreated());

        verify(auditService).registerEvent(
                eq(user), eq("CHECKOUT"), anyString(), any(HttpServletRequest.class),
                isNull(), isNull(), isNull());
    }

    @Test
    void checkoutRejectsOutOfRangeLatitude() throws Exception {

        mockMvc.perform(post("/api/orders/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"latitude\": 123.0, \"longitude\": -74.0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void downloadReceiptReturnsPdf() throws Exception {

        OrderResponse detail = order(EmailStatus.SENT);

        when(orderService.getOrder(user, 1L)).thenReturn(detail);
        when(receiptPdfService.generate(detail)).thenReturn("%PDF-prueba".getBytes());

        mockMvc.perform(get("/api/orders/1/receipt"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }
}
