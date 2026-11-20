package co.edu.unbosque.tstyle.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.tstyle.enums.OrderStatus;
import co.edu.unbosque.tstyle.model.OrderItem;

/**
 * Acceso a datos del detalle de los pedidos.
 *
 * @author Equipo T Style
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Reporte: productos más vendidos en un rango de fechas. Cada fila trae:
     * id del producto, nombre, unidades vendidas y valor vendido.
     *
     * @param excluded estado que se omite (CANCELLED)
     * @param dateFrom fecha inicial (incluida)
     * @param dateTo fecha final (excluida)
     * @param pageable límite de filas
     * @return filas del reporte
     */
    @Query("select i.productId, max(i.productName), sum(i.quantity), sum(i.subtotal) "
            + "from OrderItem i "
            + "where i.purchaseOrder.status <> :excluded "
            + "and i.purchaseOrder.createdAt >= :dateFrom "
            + "and i.purchaseOrder.createdAt < :dateTo "
            + "group by i.productId order by sum(i.quantity) desc")
    List<Object[]> findTopProducts(
            @Param("excluded") OrderStatus excluded,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            Pageable pageable);
}
