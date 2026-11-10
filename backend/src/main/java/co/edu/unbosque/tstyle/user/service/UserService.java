
package co.edu.unbosque.tstyle.user.service;

import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.tstyle.exception.DuplicateResourceException;
import co.edu.unbosque.tstyle.security.dto.MessageResponse;
import co.edu.unbosque.tstyle.security.dto.RegisterRequest;
import co.edu.unbosque.tstyle.user.enums.Role;
import co.edu.unbosque.tstyle.user.model.User;
import co.edu.unbosque.tstyle.user.repository.UserRepository;

/**
 * Contiene la lógica de negocio relacionada con los usuarios de T Style.
 *
 * @author Equipo T Style
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructor del servicio de usuarios.
     *
     * @param userRepository repositorio de usuarios
     * @param passwordEncoder codificador de contraseñas
     */
    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registra un nuevo cliente en la plataforma.
     *
     * @param request datos necesarios para el registro
     * @return mensaje de confirmación
     * @throws DuplicateResourceException si el correo ya está registrado
     */
    @Transactional
    public MessageResponse registerUser(RegisterRequest request) {

        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException(
                "El correo electrónico ya se encuentra registrado"
            );
        }

        String encryptedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User(
            request.getName().trim(),
            email,
            encryptedPassword,
            Role.CLIENT
        );

        userRepository.save(user);

        return new MessageResponse("Usuario registrado correctamente");
    }
}
