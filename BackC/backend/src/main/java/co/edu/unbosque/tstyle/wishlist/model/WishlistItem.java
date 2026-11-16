package co.edu.unbosque.tstyle.wishlist.model;

import java.time.LocalDateTime;

import co.edu.unbosque.tstyle.product.model.Product;
import co.edu.unbosque.tstyle.user.model.User;
import co.edu.unbosque.tstyle.wishlist.enums.WishlistStatus;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Producto guardado en la wishlist personal de un usuario.
 *
 * <p>La restricción única usuario + producto garantiza que un producto
 * no se guarde dos veces aunque falle una validación del código.</p>
 *
 * @author Equipo T Style
 */
@Entity
@Table(name = "tstyle_wishlist_items",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_wishlist_user_product",
                columnNames = { "user_id", "product_id" }))
public class WishlistItem {

    /**
     * Identificador único del registro.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Dueño de la wishlist.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Producto guardado.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /**
     * Estado asignado por el usuario.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WishlistStatus status;

    /**
     * Fecha y hora en que se guardó el producto.
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
    public WishlistItem() {
        super();
    }

    /**
     * Constructor con los atributos principales.
     *
     * @param user dueño de la wishlist
     * @param product producto guardado
     * @param status estado inicial
     */
    public WishlistItem(User user, Product product, WishlistStatus status) {
        this.user = user;
        this.product = product;
        this.status = status;
    }

    /**
     * Establece las fechas antes de guardar el registro.
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

    public WishlistStatus getStatus() {
        return status;
    }

    public void setStatus(WishlistStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
