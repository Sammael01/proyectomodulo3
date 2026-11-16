package co.edu.unbosque.tstyle.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.tstyle.product.model.Product;

/**
 * Acceso a datos de los productos de T Style.
 *
 * @author Equipo T Style
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Descuenta stock de forma atómica: solo actúa si el producto está activo
     * y tiene unidades suficientes, así dos compras simultáneas nunca dejan
     * el inventario en negativo.
     *
     * @param id identificador del producto
     * @param quantity unidades a descontar
     * @return 1 si se descontó, 0 si no había stock suficiente
     */
    @Modifying
    @Transactional
    @Query("update Product p set p.stock = p.stock - :quantity "
            + "where p.id = :id and p.active = true and p.stock >= :quantity")
    int decreaseStock(@Param("id") Long id, @Param("quantity") int quantity);

    /**
     * Devuelve stock al inventario (por ejemplo, al cancelar un pedido).
     *
     * @param id identificador del producto
     * @param quantity unidades a devolver
     * @return 1 si el producto existe, 0 en caso contrario
     */
    @Modifying
    @Transactional
    @Query("update Product p set p.stock = p.stock + :quantity where p.id = :id")
    int increaseStock(@Param("id") Long id, @Param("quantity") int quantity);
}
