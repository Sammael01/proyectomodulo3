package co.edu.unbosque.tstyle.dto;

import java.time.LocalDateTime;

import co.edu.unbosque.tstyle.audit.enums.AuditEventType;
import co.edu.unbosque.tstyle.audit.model.AuditEvent;

public record AuditEventResponse(
		Long id,
		AuditEventType eventType,
		Long userId,
		String userEmail,
		LocalDateTime timestamp,
		String ipAddress,
		String location,
		String locationSource,
		String detail) {
	public static AuditEventResponse from(AuditEvent event) {
		return new AuditEventResponse(
				event.getId(),
				event.getEventType(),
				event.getUserId(),
				event.getUserEmail(),
				event.getTimestamp(),
				event.getIpAddress(),
				event.getLocation(),
				event.getLocationSource(),
				event.getDetail());
	}
}
