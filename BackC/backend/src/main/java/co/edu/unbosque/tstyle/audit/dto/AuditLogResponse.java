package co.edu.unbosque.tstyle.audit.dto;

import java.time.LocalDateTime;

import co.edu.unbosque.tstyle.audit.enums.LocationSource;
import co.edu.unbosque.tstyle.audit.model.AuditLog;

/**
 * Evento de auditoría tal como lo recibe el panel de administración.
 *
 * <p>Para dibujar marcadores en Google Maps se usan solo los eventos con
 * {@code latitude} y {@code longitude}. Los de origen IP no traen coordenadas.</p>
 *
 * @param id identificador del evento
 * @param userId usuario que ejecutó la acción
 * @param userEmail correo del usuario
 * @param action código de la acción
 * @param detail descripción breve
 * @param ip dirección IP de la solicitud
 * @param locationSource origen de la ubicación (IP o GPS)
 * @param latitude latitud (solo GPS)
 * @param longitude longitud (solo GPS)
 * @param accuracyMeters precisión en metros (solo GPS)
 * @param createdAt fecha y hora del evento
 *
 * @author Equipo T Style
 */
public record AuditLogResponse(
        Long id,
        Long userId,
        String userEmail,
        String action,
        String detail,
        String ip,
        LocationSource locationSource,
        Double latitude,
        Double longitude,
        Double accuracyMeters,
        LocalDateTime createdAt) {

    /**
     * Convierte la entidad en su respuesta.
     *
     * @param log evento de auditoría
     * @return respuesta para el cliente
     */
    public static AuditLogResponse from(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getUserId(),
                log.getUserEmail(),
                log.getAction(),
                log.getDetail(),
                log.getIp(),
                log.getLocationSource(),
                log.getLatitude(),
                log.getLongitude(),
                log.getAccuracyMeters(),
                log.getCreatedAt());
    }
}
