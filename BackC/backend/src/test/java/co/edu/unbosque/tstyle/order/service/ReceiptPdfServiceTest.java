package co.edu.unbosque.tstyle.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import co.edu.unbosque.tstyle.order.dto.OrderDetailResponse;
import co.edu.unbosque.tstyle.order.dto.OrderItemResponse;
import co.edu.unbosque.tstyle.order.enums.EmailStatus;
import co.edu.unbosque.tstyle.order.enums.OrderStatus;

/**
 * Pruebas unitarias para ReceiptPdfService.
 *
 * @author Equipo T Style
 */
class ReceiptPdfServiceTest {

    @Test
    void generateReturnsAValidPdfDocument() {

        OrderDetailResponse order = new OrderDetailResponse(
                1L,
                "TS-2026-000001",
                LocalDateTime.of(2026, 11, 15, 10, 30),
                OrderStatus.PAID,
                5L,
                "Ana Pérez",
                "ana@tstyle.com",
                List.of(
                        new OrderItemResponse(10L, "Camisa", new BigDecimal("50000.00"), 2,
                                new BigDecimal("100000.00")),
                        new OrderItemResponse(11L, "Pantalón", new BigDecimal("80000.50"), 1,
                                new BigDecimal("80000.50"))),
                new BigDecimal("180000.50"),
                EmailStatus.PENDING,
                null,
                null);

        byte[] pdf = new ReceiptPdfService().generate(order);

        assertTrue(pdf.length > 500, "El PDF no debería estar vacío");
        assertEquals("%PDF", new String(pdf, 0, 4, StandardCharsets.ISO_8859_1));
    }
}
