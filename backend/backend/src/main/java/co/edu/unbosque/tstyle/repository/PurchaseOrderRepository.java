package co.edu.unbosque.tstyle.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.tstyle.enums.EmailStatus;
import co.edu.unbosque.tstyle.enums.OrderStatus;
import co.edu.unbosque.tstyle.model.PurchaseOrder;
import jakarta.persistence.LockModeType;

/**
 * Acceso a datos de los pedidos.
 *
 * @author Equipo T Style
 */
@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    List<PurchaseOrder> findByUserIdOrderByCreatedAtDesc(Long userId);

    Page<PurchaseOrder> findByStatus(OrderStatus status, Pageable pageable);

    Page<PurchaseOrder> findByEmailStatus(EmailStatus emailStatus, Pageable pageable);

    Page<PurchaseOrder> findByStatusAndEmailStatus(
            OrderStatus status, EmailStatus emailStatus, Pageable pageable);

    /**
     * Busca un pedido bloqueándolo hasta terminar la transacción, para que dos
     * administradores no apliquen el mismo cambio a la vez (por ejemplo,
     * devolver dos veces el stock de una cancelación).
     *
     * @param id identificador del pedido
     * @return pedido bloqueado, si existe
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from PurchaseOrder o where o.id = :id")
    Optional<PurchaseOrder> findByIdForUpdate(@Param("id") Long id);

    /**
     * Pedidos de un rango de fechas, sin los del estado excluido (reporte de ventas).
     *
     * @param excluded estado que se omite (CANCELLED)
     * @param dateFrom fecha inicial (incluida)
     * @param dateTo fecha final (excluida)
     * @return pedidos del rango
     */
    @Query("select o from PurchaseOrder o where o.status <> :excluded "
            + "and o.createdAt >= :dateFrom and o.createdAt < :dateTo")
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
