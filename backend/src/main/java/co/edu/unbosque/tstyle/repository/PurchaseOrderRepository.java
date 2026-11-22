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

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    List<PurchaseOrder> findByUserIdOrderByCreatedAtDesc(Long userId);

    Page<PurchaseOrder> findByStatus(OrderStatus status, Pageable pageable);

    Page<PurchaseOrder> findByEmailStatus(EmailStatus emailStatus, Pageable pageable);

    Page<PurchaseOrder> findByStatusAndEmailStatus(
            OrderStatus status, EmailStatus emailStatus, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from PurchaseOrder o where o.id = :id")
    Optional<PurchaseOrder> findByIdForUpdate(@Param("id") Long id);

    @Query("select o from PurchaseOrder o where o.status <> :excluded "
            + "and o.createdAt >= :dateFrom and o.createdAt < :dateTo")
    List<PurchaseOrder> findForSalesReport(
            @Param("excluded") OrderStatus excluded,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo);

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
