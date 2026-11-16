package co.edu.unbosque.tstyle.audit.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.tstyle.audit.dto.AuditLogResponse;
import co.edu.unbosque.tstyle.audit.enums.LocationSource;
import co.edu.unbosque.tstyle.audit.model.AuditLog;
import co.edu.unbosque.tstyle.audit.repository.AuditLogRepository;
import co.edu.unbosque.tstyle.common.PageResponse;
import co.edu.unbosque.tstyle.user.model.User;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Registra y consulta los eventos de auditoría.
 *
 * <p>Registrar un evento nunca debe romper la operación de negocio: si
 * falla el guardado, solo se deja un aviso en el log.</p>
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
     * <p>La IP se toma de {@code request.getRemoteAddr()}. Si el backend queda
     * detrás de un proxy, se debe configurar {@code server.forward-headers-strategy}
     * para que esa IP sea la del cliente real; no se lee X-Forwarded-For a mano
     * porque cualquiera puede falsificarlo.</p>
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

            // Solo hay GPS si llegaron latitud y longitud; de lo contrario únicamente la IP.
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
     * Consulta eventos de auditoría con filtros opcionales.
     *
     * @param userId filtrar por usuario (opcional)
     * @param action filtrar por código de acción (opcional)
     * @param from fecha inicial incluida (opcional)
     * @param to fecha final excluida (opcional)
     * @param onlyWithLocation true para traer solo eventos con coordenadas
     * @param pageable paginación y orden
     * @return página de eventos
     */
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> search(
            Long userId, String action, LocalDateTime from, LocalDateTime to,
            boolean onlyWithLocation, Pageable pageable) {

        Specification<AuditLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (userId != null) {
                predicates.add(cb.equal(root.get("userId"), userId));
            }
            if (action != null && !action.isBlank()) {
                predicates.add(cb.equal(root.get("action"), action.trim().toUpperCase(Locale.ROOT)));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.<LocalDateTime>get("createdAt"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThan(root.<LocalDateTime>get("createdAt"), to));
            }
            if (onlyWithLocation) {
                predicates.add(cb.isNotNull(root.get("latitude")));
                predicates.add(cb.isNotNull(root.get("longitude")));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<AuditLogResponse> page = auditLogRepository.findAll(spec, pageable)
                .map(AuditLogResponse::from);

        return PageResponse.from(page);
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
