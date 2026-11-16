package co.edu.unbosque.tstyle.cart.model;

import java.time.LocalDateTime;

import co.edu.unbosque.tstyle.product.model.Product;
import co.edu.unbosque.tstyle.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Línea del carrito de compras de un usuario.
 *
 * <p>Un usuario solo puede tener una línea por producto
 * (restricción única usuario + producto).</p>
 *
 * @author Equipo T Style
 */
@Entity
@Table(name = "tstyle_cart_items",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_cart_user_product",
                columnNames = { "user_id", "product_id" }))
public class CartItem {

    /**
     * Identificador único de la línea.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Dueño del carrito.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Producto agregado al carrito.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /**
     * Unidades del producto en el carrito.
     */
    @Column(nullable = false)
    private int quantity;

    /**
     * Fecha y hora en que se creó la línea.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Fecha y hora de la última modificación.
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Constructor vacío requerido por JPA.
     */
    public CartItem() {
        super();
    }

    /**
     * Constructor con los atributos principales.
     *
     * @param user dueño del carrito
     * @param product producto agregado
     * @param quantity unidades
     */
    public CartItem(User user, Product product, int quantity) {
        this.user = user;
        this.product = product;
        this.quantity = quantity;
    }

    /**
     * Establece las fechas antes de guardar la línea.
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * Actualiza la fecha de modificación antes de cada cambio.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
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

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
