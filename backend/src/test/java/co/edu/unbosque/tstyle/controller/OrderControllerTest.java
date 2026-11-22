package co.edu.unbosque.tstyle.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

import co.edu.unbosque.tstyle.dto.CheckoutRequest;
import co.edu.unbosque.tstyle.dto.OrderItemResponse;
import co.edu.unbosque.tstyle.dto.OrderResponse;
import co.edu.unbosque.tstyle.enums.EmailStatus;
import co.edu.unbosque.tstyle.enums.OrderStatus;
import co.edu.unbosque.tstyle.user.enums.Role;
import co.edu.unbosque.tstyle.user.model.User;
import co.edu.unbosque.tstyle.security.CurrentUserService;
import co.edu.unbosque.tstyle.service.OrderAuditService;
import co.edu.unbosque.tstyle.service.OrderService;
import co.edu.unbosque.tstyle.service.ReceiptMailService;
import co.edu.unbosque.tstyle.service.ReceiptPdfService;
import jakarta.servlet.http.HttpServletRequest;

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
    private OrderAuditService orderAuditService;

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

        when(orderService.getOrder(user, 1L)).thenReturn(order(EmailStatus.FAILED));

        mockMvc.perform(post("/api/orders/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNumber").value("TS-2026-000001"))
                .andExpect(jsonPath("$.emailStatus").value("FAILED"));

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

        verify(orderAuditService).orderCreated(
                eq(user), any(OrderResponse.class), any(HttpServletRequest.class),
                eq(new CheckoutRequest(4.711, -74.0721, 25.0)));
    }

    @Test
    void checkoutWithoutCoordinatesRegistersOnlyTheIp() throws Exception {
        when(orderService.checkout(user)).thenReturn(order(EmailStatus.SENT));
        when(orderService.getOrder(user, 1L)).thenReturn(order(EmailStatus.SENT));

        mockMvc.perform(post("/api/orders/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isCreated());

        verify(orderAuditService).orderCreated(
                eq(user), any(OrderResponse.class), any(HttpServletRequest.class),
                eq(new CheckoutRequest(null, null, null)));
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
