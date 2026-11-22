
package co.edu.unbosque.tstyle.audit.model;

import java.time.LocalDateTime;

import co.edu.unbosque.tstyle.audit.enums.AuditEventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Representa un evento de auditoría registrado en T Style.
 *
 * Almacena información sobre las acciones realizadas en el sistema, incluyendo
 * el usuario relacionado, la fecha, la dirección IP y la ubicación cuando esté
 * disponible.
 *
 * @author Equipo T Style
 * @version 1.0
 */
@Entity
@Table(name = "tstyle_audit_events")
public class AuditEvent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 60)
	private AuditEventType eventType;

	private Long userId;

	@Column(length = 150)
	private String userEmail;

	@Column(nullable = false)
	private LocalDateTime timestamp;

	@Column(length = 45)
	private String ipAddress;

	@Column(length = 255)
	private String location;

	@Column(length = 50)
	private String locationSource;

	public AuditEvent() {
	}

	public AuditEvent(AuditEventType eventType, Long userId, String userEmail, String ipAddress, String location,
			String locationSource) {
		this.eventType = eventType;
		this.userId = userId;
		this.userEmail = userEmail;
		this.ipAddress = ipAddress;
		this.location = location;
		this.locationSource = locationSource;
	}

	@PrePersist
	protected void onCreate() {
		if (timestamp == null) {
			timestamp = LocalDateTime.now();
		}
	}

	public Long getId() {
		return id;
	}

	public AuditEventType getEventType() {
		return eventType;
	}

	public void setEventType(AuditEventType eventType) {
		this.eventType = eventType;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getLocationSource() {
		return locationSource;
	}

	public void setLocationSource(String locationSource) {
		this.locationSource = locationSource;
	}
}
