package co.edu.unbosque.tstyle.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.tstyle.model.CartItem;
import jakarta.persistence.LockModeType;

/**
 * Acceso a datos del carrito.
 *
 * @author Equipo T Style
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUserIdOrderById(Long userId);

    Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId);

    void deleteByUserId(Long userId);

    /**
     * Carrito bloqueado hasta terminar la transacción. Se usa en el checkout:
     * si el usuario hace doble clic, la segunda solicitud espera a la primera
     * y encuentra el carrito vacío, así no se duplica el pedido.
     *
     * @param userId identificador del usuario
     * @return líneas del carrito
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CartItem c where c.user.id = :userId")
    List<CartItem> findForCheckout(@Param("userId") Long userId);
}
