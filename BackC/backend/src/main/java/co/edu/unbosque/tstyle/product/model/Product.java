package co.edu.unbosque.tstyle.product.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Producto del catálogo de T Style.
 *
 * <p>Versión MÍNIMA creada solo con lo que necesitan el carrito, la wishlist
 * y los pedidos (id, nombre, precio, stock y estado). Si el equipo ya tiene
 * (o crea) el módulo de catálogo, esta clase se reemplaza por la oficial
 * conservando esos campos.</p>
 *
 * @author Equipo T Style
 */
@Entity
@Table(name = "tstyle_products")
public class Product {

    /**
     * Identificador único del producto.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre del producto.
     */
    @Column(nullable = false, length = 150)
    private String name;

    /**
     * Precio unitario vigente.
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    /**
     * Unidades disponibles en inventario.
     */
    @Column(nullable = false)
    private int stock;

    /**
     * Indica si el producto se puede comprar.
     */
    @Column(nullable = false)
    private boolean active;

    /**
     * Constructor vacío requerido por JPA.
     */
    public Product() {
        this.active = true;
    }

    /**
     * Constructor con los atributos principales.
     *
     * @param name nombre del producto
     * @param price precio unitario
     * @param stock unidades disponibles
     */
    public Product(String name, BigDecimal price, int stock) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.active = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
