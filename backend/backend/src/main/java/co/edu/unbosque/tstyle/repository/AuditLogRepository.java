package co.edu.unbosque.tstyle.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.tstyle.model.AuditLog;

/**
 * Acceso a datos de la auditoría.
 *
 * @author Equipo T Style
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Eventos que tienen coordenadas (las que envió el navegador con permiso).
     *
     * @param pageable paginación y orden
     * @return página de eventos con ubicación
     */
    Page<AuditLog> findByLatitudeIsNotNullAndLongitudeIsNotNull(Pageable pageable);
}
