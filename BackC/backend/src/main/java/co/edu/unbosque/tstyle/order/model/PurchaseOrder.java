package co.edu.unbosque.tstyle.order.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import co.edu.unbosque.tstyle.order.enums.EmailStatus;
import co.edu.unbosque.tstyle.order.enums.OrderStatus;
import co.edu.unbosque.tstyle.user.model.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Pedido de compra de T Style.
 *
 * <p>Se llama PurchaseOrder (y no Order) porque ORDER es una palabra
 * reservada de SQL y de JPQL.</p>
 *
 * @author Equipo T Style
 */
@Entity
@Table(name = "tstyle_orders")
public class PurchaseOrder {

    /**
     * Identificador único del pedido.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Usuario que realizó la compra.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Estado del pedido.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    /**
     * Valor total del pedido calculado en el servidor.
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    /**
     * Estado del envío del comprobante por correo.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmailStatus emailStatus;

    /**
     * Fecha y hora en que se envió el comprobante.
     */
    private LocalDateTime emailSentAt;

    /**
     * Motivo del último fallo de envío (solo para administración).
     */
    @Column(length = 255)
    private String emailError;

    /**
     * Fecha y hora de creación del pedido.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Detalle del pedido con el precio aplicado a cada producto.
     */
    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<OrderItem> items = new ArrayList<>();

    /**
     * Constructor vacío requerido por JPA.
     */
    public PurchaseOrder() {
        super();
    }

    /**
     * Crea un pedido pagado (pago simulado) para un usuario.
     *
     * @param user comprador
     */
    public PurchaseOrder(User user) {
        this.user = user;
        this.status = OrderStatus.PAID;
        this.emailStatus = EmailStatus.PENDING;
        this.total = BigDecimal.ZERO;
    }

    /**
     * Establece la fecha de creación antes de guardar el pedido.
     */
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    /**
     * Agrega una línea al pedido.
     *
     * @param item línea del pedido
     */
    public void addItem(OrderItem item) {
        this.items.add(item);
    }

    /**
     * Número de pedido legible, por ejemplo {@code TS-2026-000123}.
     * Se deriva del año y del id, por eso es único y no se guarda aparte.
     *
     * @return número de pedido, o null si el pedido aún no se ha guardado
     */
    public String getOrderNumber() {
        if (id == null || createdAt == null) {
            return null;
        }
        return String.format("TS-%d-%06d", createdAt.getYear(), id);
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public EmailStatus getEmailStatus() {
        return emailStatus;
    }

    public void setEmailStatus(EmailStatus emailStatus) {
        this.emailStatus = emailStatus;
    }

    public LocalDateTime getEmailSentAt() {
        return emailSentAt;
    }

    public void setEmailSentAt(LocalDateTime emailSentAt) {
        this.emailSentAt = emailSentAt;
    }

    public String getEmailError() {
        return emailError;
    }

    public void setEmailError(String emailError) {
        this.emailError = emailError;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<OrderItem> getItems() {
        return items;
    }
}
