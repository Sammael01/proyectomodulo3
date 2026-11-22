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

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
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
