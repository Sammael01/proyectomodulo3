package co.edu.unbosque.tstyle.model;

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
 * <p>Versión MÍNIMA con lo que necesitan el carrito, la wishlist y los pedidos.
 * Si el equipo crea el módulo de catálogo, se reemplaza por la oficial
 * conservando estos campos.</p>
 *
 * @author Equipo T Style
 */
@Entity
@Table(name = "tstyle_products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int stock;

    @Column(nullable = false)
    private boolean active = true;

    /**
     * Constructor vacío requerido por JPA.
     */
    public Product() {
        super();
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
