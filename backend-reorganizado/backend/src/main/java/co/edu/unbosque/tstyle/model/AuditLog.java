package co.edu.unbosque.tstyle.model;

import java.time.LocalDateTime;

import co.edu.unbosque.tstyle.enums.LocationSource;
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
 * Evento de auditoría. El usuario se guarda solo como id y correo (sin llave
 * foránea) para conservar el historial aunque el usuario cambie.
 *
 * @author Equipo T Style
 */
@Entity
@Table(name = "tstyle_audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(length = 150)
    private String userEmail;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(length = 255)
    private String detail;

    @Column(length = 45)
    private String ip;

    @Column(length = 255)
    private String userAgent;

    /**
     * Latitud enviada por el navegador (solo con permiso del usuario).
     */
    private Double latitude;

    /**
     * Longitud enviada por el navegador (solo con permiso del usuario).
     */
    private Double longitude;

    /**
     * Precisión de las coordenadas, en metros.
     */
    private Double accuracyMeters;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private LocationSource locationSource;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Constructor vacío requerido por JPA.
     */
    public AuditLog() {
        super();
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getAccuracyMeters() {
        return accuracyMeters;
    }

    public void setAccuracyMeters(Double accuracyMeters) {
        this.accuracyMeters = accuracyMeters;
    }

    public LocationSource getLocationSource() {
        return locationSource;
    }

    public void setLocationSource(LocationSource locationSource) {
        this.locationSource = locationSource;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
