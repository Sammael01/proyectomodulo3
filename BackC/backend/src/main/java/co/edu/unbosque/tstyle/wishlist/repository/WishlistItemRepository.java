package co.edu.unbosque.tstyle.wishlist.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.tstyle.wishlist.enums.WishlistStatus;
import co.edu.unbosque.tstyle.wishlist.model.WishlistItem;

/**
 * Acceso a datos de la wishlist.
 *
 * @author Equipo T Style
 */
@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    /**
     * Lista toda la wishlist de un usuario con sus productos.
     *
     * @param userId identificador del usuario
     * @return elementos de la wishlist, los más recientes primero
     */
    @Query("select w from WishlistItem w join fetch w.product "
            + "where w.user.id = :userId order by w.updatedAt desc")
    List<WishlistItem> findAllByUser(@Param("userId") Long userId);

    /**
     * Lista la wishlist de un usuario filtrada por estado.
     *
     * @param userId identificador del usuario
     * @param status estado a filtrar
     * @return elementos de la wishlist, los más recientes primero
     */
    @Query("select w from WishlistItem w join fetch w.product "
            + "where w.user.id = :userId and w.status = :status order by w.updatedAt desc")
    List<WishlistItem> findAllByUserAndStatus(
            @Param("userId") Long userId, @Param("status") WishlistStatus status);

    /**
     * Busca un producto dentro de la wishlist de un usuario.
     *
     * @param userId identificador del usuario
     * @param productId identificador del producto
     * @return elemento encontrado, si existe
     */
    Optional<WishlistItem> findByUserIdAndProductId(Long userId, Long productId);

    /**
     * Reporte: productos más guardados en las wishlists de todos los usuarios.
     * Cada fila trae: id del producto, nombre, veces guardado y cuántas son "Me gusta".
     *
     * @param liked estado que se cuenta como "Me gusta"
     * @param pageable límite de filas
     * @return filas del reporte
     */
    @Query("select p.id, max(p.name), count(w), "
            + "sum(case when w.status = :liked then 1 else 0 end) "
            + "from WishlistItem w join w.product p "
            + "group by p.id order by count(w) desc")
    List<Object[]> findMostSaved(@Param("liked") WishlistStatus liked, Pageable pageable);
}
