
package co.edu.unbosque.tstyle.audit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.tstyle.audit.model.AuditEvent;

/**
 * Repositorio para gestionar los eventos de auditoría de T Style.
 *
 * @author Equipo T Style
 * @version 1.0
 */
@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {

}
