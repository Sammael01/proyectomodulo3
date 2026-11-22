
package co.edu.unbosque.tstyle.audit.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.tstyle.audit.enums.AuditEventType;
import co.edu.unbosque.tstyle.audit.model.AuditEvent;
import co.edu.unbosque.tstyle.audit.repository.AuditEventRepository;

/**
 * Servicio encargado de registrar los eventos de auditoría de T Style.
 *
 * Cada evento se guarda en una transacción independiente para evitar que los
 * registros de auditoría dependan de la transacción principal.
 *
 * @author Equipo T Style
 * @version 1.0
 */
@Service
public class AuditService {

	private final AuditEventRepository auditEventRepository;

	public AuditService(AuditEventRepository auditEventRepository) {
		this.auditEventRepository = auditEventRepository;
	}

	/**
	 * Registra un evento de auditoría.
	 *
	 * @param eventType      tipo de evento
	 * @param userId         identificador del usuario, si existe
	 * @param userEmail      correo del usuario, si está disponible
	 * @param ipAddress      dirección IP de origen
	 * @param location       ubicación, si está disponible
	 * @param locationSource origen de la información de ubicación
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void registerEvent(AuditEventType eventType, Long userId, String userEmail, String ipAddress,
			String location, String locationSource) {

		AuditEvent event = new AuditEvent(eventType, userId, userEmail, ipAddress, location, locationSource);

		auditEventRepository.save(event);
	}
}
