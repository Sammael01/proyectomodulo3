package co.edu.unbosque.tstyle.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import co.edu.unbosque.tstyle.dto.OrderItemResponse;
import co.edu.unbosque.tstyle.dto.OrderResponse;
import co.edu.unbosque.tstyle.enums.EmailStatus;
import co.edu.unbosque.tstyle.enums.OrderStatus;
import co.edu.unbosque.tstyle.user.enums.Role;
import co.edu.unbosque.tstyle.model.PurchaseOrder;
import co.edu.unbosque.tstyle.user.model.User;
import co.edu.unbosque.tstyle.repository.PurchaseOrderRepository;
import co.edu.unbosque.tstyle.user.repository.UserRepository;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

@ExtendWith(MockitoExtension.class)
class ReceiptMailServiceTest {
    @Mock
    private OrderService orderService;

    @Mock
    private ReceiptPdfService receiptPdfService;

    @Mock
    private PurchaseOrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ObjectProvider<JavaMailSender> mailSenderProvider;

    @Mock
    private JavaMailSender mailSender;

    private ReceiptMailService service;
    private User buyer;
    private OrderResponse order;

    @BeforeEach
    void setUp() {
        service = new ReceiptMailService(
                orderService, receiptPdfService, orderRepository,
                userRepository, mailSenderProvider, "no-reply@tstyle.test");

        buyer = new User("Ana Pérez", "ana@tstyle.com", "hash", Role.CLIENT);
        buyer.setId(5L);
        buyer.setEmailVerified(true);

        order = new OrderResponse(
                1L, "TS-2026-000001", LocalDateTime.of(2026, 11, 15, 10, 30),
                OrderStatus.PAID, 5L, "Ana Pérez", "ana@tstyle.com",
                List.of(new OrderItemResponse(10L, "Camisa", new BigDecimal("50000.00"), 2,
                        new BigDecimal("100000.00"))),
                new BigDecimal("100000.00"), EmailStatus.PENDING, null, null);
    }

    @Test
    void sendReceiptMarksOrderAsSentWhenEmailIsDelivered() {
        when(orderService.getOrderForReceipt(1L)).thenReturn(order);
        when(userRepository.findById(5L)).thenReturn(Optional.of(buyer));
        when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);
        when(receiptPdfService.generate(order)).thenReturn(new byte[] { 1, 2, 3 });
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));

        service.sendReceipt(1L);

        verify(mailSender).send(any(MimeMessage.class));
        verify(orderRepository).updateEmailStatus(
                eq(1L), eq(EmailStatus.SENT), any(LocalDateTime.class), isNull());
    }

    @Test
    void sendReceiptMarksFailedButDoesNotTouchTheOrderWhenSmtpFails() {
        when(orderService.getOrderForReceipt(1L)).thenReturn(order);
        when(userRepository.findById(5L)).thenReturn(Optional.of(buyer));
        when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);
        when(receiptPdfService.generate(order)).thenReturn(new byte[] { 1, 2, 3 });
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
        doThrow(new MailSendException("SMTP caído")).when(mailSender).send(any(MimeMessage.class));

        assertDoesNotThrow(() -> service.sendReceipt(1L));

        verify(orderRepository).updateEmailStatus(
                eq(1L), eq(EmailStatus.FAILED), isNull(), contains("SMTP"));

        verify(orderRepository, never()).save(any(PurchaseOrder.class));
    }

    @Test
    void sendReceiptStaysPendingWhenEmailIsNotVerified() {
        buyer.setEmailVerified(false);

        when(orderService.getOrderForReceipt(1L)).thenReturn(order);
        when(userRepository.findById(5L)).thenReturn(Optional.of(buyer));

        service.sendReceipt(1L);

        verify(orderRepository).updateEmailStatus(
                eq(1L), eq(EmailStatus.PENDING), isNull(), anyString());
        verifyNoInteractions(mailSender);
    }

    @Test
    void sendReceiptMarksFailedWhenMailServerIsNotConfigured() {
        when(orderService.getOrderForReceipt(1L)).thenReturn(order);
        when(userRepository.findById(5L)).thenReturn(Optional.of(buyer));
        when(mailSenderProvider.getIfAvailable()).thenReturn(null);

        service.sendReceipt(1L);

        verify(orderRepository).updateEmailStatus(
                eq(1L), eq(EmailStatus.FAILED), isNull(), anyString());
    }
}
