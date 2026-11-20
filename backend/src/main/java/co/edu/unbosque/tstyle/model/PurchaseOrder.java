package co.edu.unbosque.tstyle.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import co.edu.unbosque.tstyle.enums.EmailStatus;
import co.edu.unbosque.tstyle.enums.OrderStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Pedido de compra. Se llama PurchaseOrder (y no Order) porque ORDER es una
 * palabra reservada de SQL.
 *
 * @author Equipo T Style
 */
@Entity
@Table(name = "tstyle_orders")
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmailStatus emailStatus;

    private LocalDateTime emailSentAt;

    /**
     * Motivo del último fallo de correo (solo se muestra a administradores).
     */
    @Column(length = 255)
    private String emailError;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL)
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

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
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
     * Número de pedido legible, por ejemplo TS-2026-000123. Se deriva del año
     * y del id, por eso es único y no se guarda aparte.
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

    public LocalDateTime getEmailSentAt() {
        return emailSentAt;
    }

    public String getEmailError() {
        return emailError;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<OrderItem> getItems() {
        return items;
    }
}
