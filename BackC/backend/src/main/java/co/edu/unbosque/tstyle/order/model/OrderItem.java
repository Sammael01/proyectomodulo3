package co.edu.unbosque.tstyle.order.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Línea de un pedido.
 *
 * <p>Guarda una copia del nombre y del precio unitario aplicado al momento
 * de la compra, de modo que el historial no cambia si luego se modifica
 * el producto. Por eso el producto se guarda solo como id (sin llave foránea).</p>
 *
 * @author Equipo T Style
 */
@Entity
@Table(name = "tstyle_order_items")
public class OrderItem {

    /**
     * Identificador único de la línea.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Pedido al que pertenece la línea.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    /**
     * Identificador del producto comprado.
     */
    @Column(nullable = false)
    private Long productId;

    /**
     * Nombre del producto al momento de la compra.
     */
    @Column(nullable = false, length = 150)
    private String productName;

    /**
     * Precio unitario aplicado al momento de la compra.
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    /**
     * Unidades compradas.
     */
    @Column(nullable = false)
    private int quantity;

    /**
     * Precio unitario por cantidad.
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    /**
     * Constructor vacío requerido por JPA.
     */
    public OrderItem() {
        super();
    }

    /**
     * Crea una línea de pedido y calcula su subtotal.
     *
     * @param purchaseOrder pedido al que pertenece
     * @param productId identificador del producto
     * @param productName nombre del producto
     * @param unitPrice precio unitario aplicado
     * @param quantity unidades compradas
     */
    public OrderItem(PurchaseOrder purchaseOrder, Long productId, String productName,
            BigDecimal unitPrice, int quantity) {

        this.purchaseOrder = purchaseOrder;
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.subtotal = unitPrice
                .multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public Long getId() {
        return id;
    }

    public PurchaseOrder getPurchaseOrder() {
        return purchaseOrder;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }
}
