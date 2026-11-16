package co.edu.unbosque.tstyle.admin.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.tstyle.audit.dto.AuditLogResponse;
import co.edu.unbosque.tstyle.audit.service.AuditService;
import co.edu.unbosque.tstyle.common.PageParams;
import co.edu.unbosque.tstyle.common.PageResponse;
import co.edu.unbosque.tstyle.security.CurrentUserService;

/**
 * Endpoints de consulta de auditoría y ubicaciones (solo rol ADMIN).
 *
 * <p>{@code /locations} entrega solo los eventos que tienen coordenadas
 * (las enviadas por el navegador con permiso del usuario), listos para dibujarse
 * como marcadores en Google Maps. Los eventos con solo IP no traen coordenadas.</p>
 *
 * @author Equipo T Style
 */
@RestController
@RequestMapping("/api/admin/audit")
public class AdminAuditController {

    private final AuditService auditService;
    private final CurrentUserService currentUserService;

    /**
     * Constructor del controlador de auditoría.
     *
     * @param auditService servicio de auditoría
     * @param currentUserService acceso al usuario autenticado
     */
    public AdminAuditController(
            AuditService auditService,
            CurrentUserService currentUserService) {

        this.auditService = auditService;
        this.currentUserService = currentUserService;
    }

    /**
     * Consulta eventos de auditoría con filtros opcionales.
     *
     * @param userId usuario que ejecutó la acción
     * @param action código de la acción, por ejemplo CHECKOUT
     * @param from fecha inicial (yyyy-MM-dd), incluida
     * @param to fecha final (yyyy-MM-dd), incluida
     * @param page número de página (desde 0)
     * @param size tamaño de página (máximo 100)
     * @return página de eventos, del más reciente al más antiguo
     */
    @GetMapping
    public PageResponse<AuditLogResponse> search(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        currentUserService.requireAdmin();

        return auditService.search(
                userId, action,
                from == null ? null : from.atStartOfDay(),
                to == null ? null : to.plusDays(1).atStartOfDay(),
                false,
                PageParams.newestFirst(page, size));
    }

    /**
     * Eventos con coordenadas para mostrar en el mapa.
     *
     * @param userId usuario que ejecutó la acción
     * @param from fecha inicial (yyyy-MM-dd), incluida
     * @param to fecha final (yyyy-MM-dd), incluida
     * @param page número de página (desde 0)
     * @param size tamaño de página (máximo 100)
     * @return eventos con latitud y longitud
     */
    @GetMapping("/locations")
    public PageResponse<AuditLogResponse> locations(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {

        currentUserService.requireAdmin();

        return auditService.search(
                userId, null,
                from == null ? null : from.atStartOfDay(),
                to == null ? null : to.plusDays(1).atStartOfDay(),
                true,
                PageParams.newestFirst(page, size));
    }
}
