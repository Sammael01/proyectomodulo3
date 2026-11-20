package co.edu.unbosque.tstyle.security;

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import co.edu.unbosque.tstyle.enums.Role;
import co.edu.unbosque.tstyle.exception.ForbiddenException;
import co.edu.unbosque.tstyle.exception.UnauthorizedException;
import co.edu.unbosque.tstyle.model.User;
import co.edu.unbosque.tstyle.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Entrega el usuario que realiza la solicitud a los módulos de carrito,
 * wishlist, pedidos y administración.
 *
 * <p><b>ESPACIO PARA EL JWT.</b> Este es el único lugar que sabe cómo
 * identificar al usuario. Mientras no exista el login/JWT, se toma el id del
 * encabezado {@code X-User-Id} (solo para desarrollo: cualquiera podría
 * hacerse pasar por otro usuario). Cuando el JWT esté listo, basta con
 * reemplazar el cuerpo de {@link #resolveUserId()} para leer el usuario del
 * token; ningún controlador ni servicio necesita cambios.</p>
 *
 * @author Equipo T Style
 */
@Service
public class CurrentUserService {

    /** Encabezado temporal con el id del usuario (se elimina al llegar el JWT). */
    public static final String USER_HEADER = "X-User-Id";

    private final UserRepository userRepository;

    /**
     * Constructor del servicio.
     *
     * @param userRepository repositorio de usuarios
     */
    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Obtiene el usuario de la solicitud actual.
     *
     * @return usuario habilitado
     * @throws UnauthorizedException si no hay un usuario identificado
     * @throws ForbiddenException si la cuenta está deshabilitada
     */
    public User requireUser() {

        User user = userRepository.findById(resolveUserId())
                .orElseThrow(() -> new UnauthorizedException("Usuario no autenticado"));

        if (!user.isEnabled()) {
            throw new ForbiddenException("La cuenta del usuario está deshabilitada");
        }

        return user;
    }

    /**
     * Obtiene el usuario de la solicitud y exige que sea administrador.
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
     * <p>TODO (JWT): reemplazar por la lectura del usuario autenticado,
     * por ejemplo desde el contexto de seguridad.</p>
     *
     * @return id del usuario
     */
    private Long resolveUserId() {

        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {

            HttpServletRequest request = attributes.getRequest();
            String header = request.getHeader(USER_HEADER);

            if (header != null && !header.isBlank()) {
                try {
                    return Long.valueOf(header.trim());
                } catch (NumberFormatException e) {
                    throw new UnauthorizedException("El identificador de usuario no es válido");
                }
            }
        }

        throw new UnauthorizedException("Debes indicar el usuario en el encabezado " + USER_HEADER);
    }
}
