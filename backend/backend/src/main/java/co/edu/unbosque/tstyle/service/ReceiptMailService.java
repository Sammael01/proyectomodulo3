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
import co.edu.unbosque.tstyle.model.User;
import co.edu.unbosque.tstyle.repository.PurchaseOrderRepository;
import co.edu.unbosque.tstyle.repository.UserRepository;
import jakarta.mail.internet.MimeMessage;

/**
 * Envía el comprobante PDF al correo VERIFICADO del comprador.
 *
 * <p>No es transaccional a propósito: se ejecuta cuando el pedido ya fue
 * confirmado. Si el envío falla, el pedido queda intacto, el estado del correo
 * pasa a FAILED y se puede reenviar sin crear otro pedido. Nunca lanza
 * excepciones hacia quien lo llama.</p>
 *
 * @author Equipo T Style
 */
@Service
public class ReceiptMailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReceiptMailService.class);

    private final OrderService orderService;
    private final ReceiptPdfService receiptPdfService;
    private final PurchaseOrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String from;

    /**
     * Constructor del servicio de correo de comprobantes.
     *
     * @param orderService servicio de pedidos
     * @param receiptPdfService generador del PDF
     * @param orderRepository repositorio de pedidos
     * @param userRepository repositorio de usuarios
     * @param mailSenderProvider cliente SMTP (puede no estar configurado)
     * @param from remitente; por defecto {@code spring.mail.username}
     */
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

    /**
     * Intenta enviar el comprobante de un pedido y guarda el resultado en el
     * campo {@code emailStatus} del pedido.
     *
     * @param orderId identificador del pedido
     */
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

        // Solo se envía a un correo verificado; mientras tanto queda pendiente.
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
