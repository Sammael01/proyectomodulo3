package co.edu.unbosque.tstyle.cart.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.tstyle.cart.model.CartItem;
import jakarta.persistence.LockModeType;

/**
 * Acceso a datos del carrito de compras.
 *
 * @author Equipo T Style
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * Lista las líneas del carrito de un usuario junto con su producto.
     *
     * @param userId identificador del usuario
     * @return líneas del carrito
     */
    @Query("select c from CartItem c join fetch c.product where c.user.id = :userId order by c.id")
    List<CartItem> findAllByUserWithProduct(@Param("userId") Long userId);

    /**
     * Busca la línea de un producto en el carrito de un usuario.
     *
     * @param userId identificador del usuario
     * @param productId identificador del producto
     * @return línea encontrada, si existe
     */
    Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId);

    /**
     * Lista las líneas del carrito bloqueándolas hasta terminar la transacción.
     * Se usa en el checkout: si el usuario hace doble clic, la segunda solicitud
     * espera a la primera y encuentra el carrito ya vacío (no duplica el pedido).
     *
     * @param userId identificador del usuario
     * @return líneas del carrito bloqueadas
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CartItem c where c.user.id = :userId")
    List<CartItem> findAllByUserForUpdate(@Param("userId") Long userId);

    /**
     * Vacía el carrito de un usuario.
     *
     * @param userId identificador del usuario
     */
    @Modifying
    @Transactional
    @Query("delete from CartItem c where c.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
