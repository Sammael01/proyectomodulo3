package co.edu.unbosque.tstyle.audit.model;

import java.time.LocalDateTime;

import co.edu.unbosque.tstyle.audit.enums.LocationSource;
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
 * Evento de auditoría de la plataforma T Style.
 *
 * <p>El usuario se guarda solo como id y correo (sin llave foránea) para que
 * el historial se conserve aunque el usuario cambie.</p>
 *
 * @author Equipo T Style
 */
@Entity
@Table(name = "tstyle_audit_logs")
public class AuditLog {

    /**
     * Identificador único del evento.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Usuario que ejecutó la acción.
     */
    private Long userId;

    /**
     * Correo del usuario que ejecutó la acción.
     */
    @Column(length = 150)
    private String userEmail;

    /**
     * Código de la acción, por ejemplo CHECKOUT.
     */
    @Column(nullable = false, length = 50)
    private String action;

    /**
     * Descripción breve del evento.
     */
    @Column(length = 255)
    private String detail;

    /**
     * Dirección IP de la solicitud.
     */
    @Column(length = 45)
    private String ip;

    /**
     * Navegador o cliente que hizo la solicitud.
     */
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

    /**
     * Origen de la ubicación registrada.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private LocationSource locationSource;

    /**
     * Fecha y hora del evento.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Constructor vacío requerido por JPA.
     */
    public AuditLog() {
        super();
    }

    /**
     * Establece la fecha del evento antes de guardarlo.
     */
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
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
