package co.edu.unbosque.tstyle.audit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.tstyle.audit.model.AuditLog;

/**
 * Acceso a datos de la auditoría.
 *
 * @author Equipo T Style
 */
@Repository
public interface AuditLogRepository
        extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {
}
