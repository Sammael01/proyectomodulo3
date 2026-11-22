package co.edu.unbosque.tstyle.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import co.edu.unbosque.tstyle.dto.OrderResponse;
import co.edu.unbosque.tstyle.enums.EmailStatus;
import co.edu.unbosque.tstyle.user.model.User;
import co.edu.unbosque.tstyle.repository.PurchaseOrderRepository;
import co.edu.unbosque.tstyle.user.repository.UserRepository;
import jakarta.mail.internet.MimeMessage;

@Service
public class ReceiptMailService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReceiptMailService.class);

    private final OrderService orderService;
    private final ReceiptPdfService receiptPdfService;
    private final PurchaseOrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String from;

    public ReceiptMailService(
            OrderService orderService,
            ReceiptPdfService receiptPdfService,
            PurchaseOrderRepository orderRepository,
            UserRepository userRepository,
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${tstyle.mail.from:${spring.mail.username:no-reply@tstyle.local}}") String from) {
        this.orderService = orderService;
        this.receiptPdfService = receiptPdfService;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.mailSenderProvider = mailSenderProvider;
        this.from = from;
    }

    public void sendReceipt(Long orderId) {
        try {
            attemptSend(orderId);
        } catch (Exception e) {
            LOGGER.error("No fue posible procesar el comprobante del pedido {}", orderId, e);
        }
    }

    private void attemptSend(Long orderId) {
        OrderResponse order = orderService.getOrderForReceipt(orderId);

        User buyer = userRepository.findById(order.userId()).orElse(null);

        if (buyer == null || !buyer.isEmailVerified()) {
            orderRepository.updateEmailStatus(
                    orderId, EmailStatus.PENDING, null, "El correo del comprador no está verificado");
            return;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();

        if (mailSender == null) {
            orderRepository.updateEmailStatus(
                    orderId, EmailStatus.FAILED, null, "El servidor de correo no está configurado");
            return;
        }

        String error = null;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(buyer.getEmail());
            helper.setSubject("Comprobante de tu pedido " + order.orderNumber() + " - T Style");
            helper.setText("Hola " + order.buyerName() + ",\n\n"
                    + "Gracias por tu compra en T Style. Adjuntamos el comprobante de tu pedido "
                    + order.orderNumber() + " por un total de " + ReceiptPdfService.money(order.total())
                    + ".\n\nEsta compra se realizó con pago simulado.\n\nEquipo T Style");
            helper.addAttachment(
                    "comprobante-" + order.orderNumber() + ".pdf",
                    new ByteArrayResource(receiptPdfService.generate(order)),
                    "application/pdf");

            mailSender.send(message);
        } catch (Exception e) {
            LOGGER.warn("Falló el envío del comprobante del pedido {}", orderId, e);
            error = describe(e);
        }

        if (error == null) {
            orderRepository.updateEmailStatus(orderId, EmailStatus.SENT, LocalDateTime.now(), null);
        } else {
            orderRepository.updateEmailStatus(orderId, EmailStatus.FAILED, null, error);
        }
    }

    private String describe(Exception e) {
        String message = (e.getMessage() == null || e.getMessage().isBlank())
                ? e.getClass().getSimpleName()
                : e.getMessage();
        return message.length() <= 250 ? message : message.substring(0, 250);
    }
}
