
package co.edu.unbosque.tstyle.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unbosque.tstyle.model.Product;

/**
 * Acceso a datos de los productos del catálogo y del inventario.
 *
 * @author Equipo T Style
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByProviderIdAndExternalId(Long providerId, String externalId);

    /**
     * Busca productos de proveedores activos con filtros opcionales.
     *
     * @param categoryId categoría a filtrar, o null para todas
     * @param providerId proveedor a filtrar, o null para todos
     * @param search texto contenido en el nombre, o null para omitirlo
     * @param pageable paginación y orden
     * @return página de productos
     */
    @EntityGraph(attributePaths = { "provider", "category" })
    @Query("""
            SELECT p FROM Product p
            WHERE p.provider.active = true
              AND (:categoryId IS NULL OR p.category.id = :categoryId)
              AND (:providerId IS NULL OR p.provider.id = :providerId)
              AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<Product> search(@Param("categoryId") Long categoryId,
            @Param("providerId") Long providerId,
            @Param("search") String search,
            Pageable pageable);

    /**
     * Aumenta las existencias de un producto.
     *
     * @param productId identificador del producto
     * @param quantity cantidad que se agrega
     * @return cantidad de registros modificados
     */
    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock + :quantity WHERE p.id = :productId")
    int increaseStock(@Param("productId") Long productId,
            @Param("quantity") int quantity);

    /**
     * Disminuye las existencias si hay unidades suficientes.
     *
     * @param productId identificador del producto
     * @param quantity cantidad que se descuenta
     * @return cantidad de registros modificados
     */
    @Modifying
    @Query("""
            UPDATE Product p
            SET p.stock = p.stock - :quantity
            WHERE p.id = :productId AND p.stock >= :quantity
            """)
    int decreaseStock(@Param("productId") long productId,
            @Param("quantity") int quantity);
}
