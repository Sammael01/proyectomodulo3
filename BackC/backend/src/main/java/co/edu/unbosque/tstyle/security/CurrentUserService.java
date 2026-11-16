package co.edu.unbosque.tstyle.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import co.edu.unbosque.tstyle.exception.ForbiddenException;
import co.edu.unbosque.tstyle.exception.UnauthorizedException;
import co.edu.unbosque.tstyle.user.enums.Role;
import co.edu.unbosque.tstyle.user.model.User;
import co.edu.unbosque.tstyle.user.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Entrega el usuario autenticado a los módulos de carrito, wishlist,
 * pedidos y administración.
 *
 * <p>Este es el ÚNICO punto del módulo que sabe cómo identificar al usuario.
 * El proyecto base todavía no tiene login/JWT, así que mientras tanto se
 * admite (solo si se activa explícitamente) el encabezado {@code X-User-Id}
 * para poder probar con Postman o curl.</p>
 *
 * <p><b>Cuando exista el login/JWT:</b> basta con reemplazar el cuerpo de
 * {@link #resolveUserId()} para leer el id (o correo) del usuario desde el
 * contexto de seguridad. Ningún controlador ni servicio necesita cambios.</p>
 *
 * @author Equipo T Style
 */
@Service
public class CurrentUserService {

    /** Encabezado temporal de desarrollo con el id del usuario. */
    public static final String DEV_USER_HEADER = "X-User-Id";

    private final UserRepository userRepository;
    private final boolean devHeaderEnabled;

    /**
     * Constructor del servicio.
     *
     * @param userRepository repositorio de usuarios
     * @param devHeaderEnabled propiedad {@code tstyle.auth.dev-header-enabled};
     *        debe ser {@code false} fuera de desarrollo
     */
    public CurrentUserService(
            UserRepository userRepository,
            @Value("${tstyle.auth.dev-header-enabled:false}") boolean devHeaderEnabled) {

        this.userRepository = userRepository;
        this.devHeaderEnabled = devHeaderEnabled;
    }

    /**
     * Obtiene el usuario autenticado de la solicitud actual.
     *
     * @return usuario habilitado
     * @throws UnauthorizedException si no hay un usuario identificado
     * @throws ForbiddenException si la cuenta está deshabilitada
     */
    public User requireUser() {

        Long userId = resolveUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("Usuario no autenticado"));

        if (!user.isEnabled()) {
            throw new ForbiddenException("La cuenta del usuario está deshabilitada");
        }

        return user;
    }

    /**
     * Obtiene el usuario autenticado y exige que sea administrador.
     *
     * @return usuario con rol ADMIN
     * @throws ForbiddenException si el usuario no es administrador
     */
    public User requireAdmin() {

        User user = requireUser();

        if (user.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Esta operación es solo para administradores");
        }

        return user;
    }

    /**
     * Identifica al usuario de la solicitud actual.
     *
     * <p>PUNTO DE INTEGRACIÓN con el login/JWT del equipo.</p>
     *
     * @return id del usuario
     */
    private Long resolveUserId() {

        if (devHeaderEnabled) {
            HttpServletRequest request = currentRequest();
            String header = request == null ? null : request.getHeader(DEV_USER_HEADER);

            if (header != null && !header.isBlank()) {
                try {
                    return Long.valueOf(header.trim());
                } catch (NumberFormatException e) {
                    throw new UnauthorizedException("El identificador de usuario no es válido");
                }
            }
        }

        throw new UnauthorizedException("Debes iniciar sesión para realizar esta acción");
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }
}
