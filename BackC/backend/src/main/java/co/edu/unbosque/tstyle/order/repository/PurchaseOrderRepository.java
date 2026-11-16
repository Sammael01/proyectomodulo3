package co.edu.unbosque.tstyle.order.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.tstyle.order.enums.EmailStatus;
import co.edu.unbosque.tstyle.order.enums.OrderStatus;
import co.edu.unbosque.tstyle.order.model.PurchaseOrder;
import jakarta.persistence.LockModeType;

/**
 * Acceso a datos de los pedidos.
 *
 * @author Equipo T Style
 */
@Repository
public interface PurchaseOrderRepository
        extends JpaRepository<PurchaseOrder, Long>, JpaSpecificationExecutor<PurchaseOrder> {

    /**
     * Busca un pedido trayendo de una vez su comprador y su detalle.
     *
     * @param id identificador del pedido
     * @return pedido encontrado, si existe
     */
    @EntityGraph(attributePaths = { "user", "items" })
    Optional<PurchaseOrder> findWithDetailsById(Long id);

    /**
     * Busca un pedido bloqueándolo hasta terminar la transacción. Se usa al cambiar
     * su estado, para que dos administradores no apliquen el mismo cambio a la vez
     * (por ejemplo, devolver dos veces el stock de una cancelación).
     *
     * @param id identificador del pedido
     * @return pedido bloqueado, si existe
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from PurchaseOrder o where o.id = :id")
    Optional<PurchaseOrder> findByIdForUpdate(@Param("id") Long id);

    /**
     * Lista los pedidos de un usuario, del más reciente al más antiguo.
     *
     * @param userId identificador del usuario
     * @return pedidos del usuario
     */
    @Query("select o from PurchaseOrder o join fetch o.user "
            + "where o.user.id = :userId order by o.createdAt desc")
    List<PurchaseOrder> findAllByUser(@Param("userId") Long userId);

    /**
     * Pedidos de un rango de fechas que no tengan el estado excluido.
     * Alimenta el reporte de ventas.
     *
     * @param excluded estado que se omite (CANCELLED)
     * @param dateFrom fecha inicial (incluida)
     * @param dateTo fecha final (excluida)
     * @return pedidos del rango
     */
    @Query("select o from PurchaseOrder o where o.status <> :excluded "
            + "and o.createdAt >= :dateFrom and o.createdAt < :dateTo order by o.createdAt")
    List<PurchaseOrder> findForSalesReport(
            @Param("excluded") OrderStatus excluded,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo);

    /**
     * Actualiza el estado del envío del comprobante sin tocar el resto del pedido.
     *
     * @param id identificador del pedido
     * @param status nuevo estado del correo
     * @param sentAt fecha de envío (solo si fue enviado)
     * @param error motivo del fallo (solo si falló)
     * @return filas actualizadas
     */
    @Modifying
    @Transactional
    @Query("update PurchaseOrder o set o.emailStatus = :status, "
            + "o.emailSentAt = :sentAt, o.emailError = :error where o.id = :id")
    int updateEmailStatus(
            @Param("id") Long id,
            @Param("status") EmailStatus status,
            @Param("sentAt") LocalDateTime sentAt,
            @Param("error") String error);
}
