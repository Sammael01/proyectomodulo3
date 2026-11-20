package co.edu.unbosque.tstyle.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Línea del carrito de un usuario. La restricción única (usuario, producto)
 * garantiza una sola línea por producto.
 *
 * @author Equipo T Style
 */
@Entity
@Table(name = "tstyle_cart_items",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_cart_user_product",
                columnNames = { "user_id", "product_id" }))
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;

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

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
