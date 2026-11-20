package co.edu.unbosque.tstyle.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.tstyle.dto.PageResponse;
import co.edu.unbosque.tstyle.enums.LocationSource;
import co.edu.unbosque.tstyle.model.AuditLog;
import co.edu.unbosque.tstyle.model.User;
import co.edu.unbosque.tstyle.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Registra y consulta los eventos de auditoría. Registrar un evento nunca
 * rompe la operación de negocio: si falla el guardado, solo se deja un aviso en el log.
 *
 * @author Equipo T Style
 */
@Service
public class AuditService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;

    /**
     * Constructor del servicio de auditoría.
     *
     * @param auditLogRepository repositorio de auditoría
     */
    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Registra un evento de auditoría.
     *
     * <p>La IP sale de {@code request.getRemoteAddr()}. Detrás de un proxy hay que
     * configurar {@code server.forward-headers-strategy}; no se lee X-Forwarded-For
     * a mano porque cualquiera puede falsificarlo.</p>
     *
     * @param user usuario que ejecuta la acción (puede ser null)
     * @param action código de la acción, por ejemplo CHECKOUT
     * @param detail descripción breve
     * @param request solicitud HTTP (puede ser null)
     * @param latitude latitud del navegador, solo si el usuario dio permiso
     * @param longitude longitud del navegador, solo si el usuario dio permiso
     * @param accuracyMeters precisión de las coordenadas en metros
     */
    public void registerEvent(
            User user, String action, String detail, HttpServletRequest request,
            Double latitude, Double longitude, Double accuracyMeters) {

        try {
            AuditLog log = new AuditLog();

            if (user != null) {
                log.setUserId(user.getId());
                log.setUserEmail(user.getEmail());
            }

            log.setAction(action);
            log.setDetail(truncate(detail, 255));

            if (request != null) {
                log.setIp(truncate(request.getRemoteAddr(), 45));
                log.setUserAgent(truncate(request.getHeader("User-Agent"), 255));
            }

            // Solo hay GPS si llegaron latitud y longitud; de lo contrario, únicamente la IP.
            if (latitude != null && longitude != null) {
                log.setLocationSource(LocationSource.GPS);
                log.setLatitude(latitude);
                log.setLongitude(longitude);
                log.setAccuracyMeters(accuracyMeters);
            } else {
                log.setLocationSource(LocationSource.IP);
            }

            auditLogRepository.save(log);

        } catch (Exception e) {
            LOGGER.warn("No fue posible registrar el evento de auditoría {}", action, e);
        }
    }

    /**
     * Consulta todos los eventos de auditoría.
     *
     * @param pageable paginación y orden
     * @return página de eventos
     */
    @Transactional(readOnly = true)
    public PageResponse<AuditLog> list(Pageable pageable) {
        return PageResponse.from(auditLogRepository.findAll(pageable));
    }

    /**
     * Consulta solo los eventos que tienen coordenadas (para el mapa).
     *
     * @param pageable paginación y orden
     * @return página de eventos con ubicación
     */
    @Transactional(readOnly = true)
    public PageResponse<AuditLog> locations(Pageable pageable) {
        return PageResponse.from(auditLogRepository.findByLatitudeIsNotNullAndLongitudeIsNotNull(pageable));
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
