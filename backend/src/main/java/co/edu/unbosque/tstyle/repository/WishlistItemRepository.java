package co.edu.unbosque.tstyle.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.tstyle.enums.WishlistStatus;
import co.edu.unbosque.tstyle.model.WishlistItem;

/**
 * Acceso a datos de la wishlist.
 *
 * @author Equipo T Style
 */
@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    List<WishlistItem> findByUserIdOrderByUpdatedAtDesc(Long userId);

    List<WishlistItem> findByUserIdAndStatusOrderByUpdatedAtDesc(Long userId, WishlistStatus status);

    Optional<WishlistItem> findByUserIdAndProductId(Long userId, Long productId);

    /**
     * Reporte: productos más guardados en las wishlists. Cada fila trae:
     * id del producto, nombre, veces guardado y cuántas son "Me gusta".
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
