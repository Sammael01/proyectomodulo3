
package co.edu.unbosque.tstyle.user.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.tstyle.email.EmailCenter;
import co.edu.unbosque.tstyle.exception.BusinessException;
import co.edu.unbosque.tstyle.exception.DuplicateResourceException;
import co.edu.unbosque.tstyle.exception.ResourceNotFoundException;
import co.edu.unbosque.tstyle.security.dto.MessageResponse;
import co.edu.unbosque.tstyle.security.dto.RegisterRequest;
import co.edu.unbosque.tstyle.security.dto.ResendVerificationRequest;
import co.edu.unbosque.tstyle.security.dto.VerifyEmailRequest;
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
    private final EmailCenter emailCenter;

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Constructor del servicio de usuarios.
     *
     * @param userRepository repositorio de usuarios
     * @param passwordEncoder codificador de contraseñas
     * @param emailCenter servicio encargado de enviar correos
     */
    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            EmailCenter emailCenter) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailCenter = emailCenter;
    }

    /**
     * Registra un nuevo cliente y envía un código de verificación.
     *
     * @param request datos necesarios para el registro
     * @return mensaje de confirmación
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

        String verificationCode = generateVerificationCode();

        user.setVerificationCodeHash(
            passwordEncoder.encode(verificationCode)
        );

        user.setVerificationExpiresAt(
            LocalDateTime.now().plusMinutes(10)
        );

        user.setVerificationAttempts(0);
        user.setLastVerificationSentAt(LocalDateTime.now());

        userRepository.save(user);

        emailCenter.sendVerificationCode(email, verificationCode);

        return new MessageResponse(
            "Usuario registrado. Revisa tu correo para verificar la cuenta"
        );
    }

    /**
     * Verifica el código enviado al correo y activa la cuenta.
     *
     * @param request correo y código de verificación
     * @return mensaje de confirmación
     */
    @Transactional(noRollbackFor = BusinessException.class)
    public MessageResponse verifyEmail(VerifyEmailRequest request) {

        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No se encontró un usuario con ese correo"
            ));

        if (user.isEmailVerified()) {
            throw new BusinessException("El correo ya se encuentra verificado");
        }

        if (user.getVerificationCodeHash() == null
                || user.getVerificationExpiresAt() == null) {
            throw new BusinessException("Solicita un nuevo código de verificación");
        }

        if (user.getVerificationExpiresAt().isBefore(LocalDateTime.now())) {
            user.setVerificationCodeHash(null);
            user.setVerificationExpiresAt(null);
            throw new BusinessException("El código ha vencido. Solicita uno nuevo");
        }

        if (user.getVerificationAttempts() >= 5) {
            throw new BusinessException(
                "Superaste el máximo de intentos. Solicita un nuevo código"
            );
        }

        user.setVerificationAttempts(user.getVerificationAttempts() + 1);

        if (!passwordEncoder.matches(
                request.getCode(),
                user.getVerificationCodeHash())) {

            throw new BusinessException("El código de verificación es incorrecto");
        }

        user.setEmailVerified(true);
        user.setEnabled(true);
        user.setVerificationCodeHash(null);
        user.setVerificationExpiresAt(null);
        user.setVerificationAttempts(0);

        userRepository.save(user);

        return new MessageResponse("Correo verificado correctamente");
    }

    /**
     * Genera y envía un nuevo código de verificación.
     *
     * @param request correo del usuario
     * @return mensaje de confirmación
     */
    @Transactional
    public MessageResponse resendVerificationCode(
            ResendVerificationRequest request) {

        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No se encontró un usuario con ese correo"
            ));

        if (user.isEmailVerified()) {
            throw new BusinessException("El correo ya se encuentra verificado");
        }

        LocalDateTime now = LocalDateTime.now();

        if (user.getLastVerificationSentAt() != null
                && user.getLastVerificationSentAt().plusSeconds(60).isAfter(now)) {
            throw new BusinessException(
                "Debes esperar 60 segundos antes de solicitar otro código"
            );
        }

        String verificationCode = generateVerificationCode();

        user.setVerificationCodeHash(
            passwordEncoder.encode(verificationCode)
        );

        user.setVerificationExpiresAt(now.plusMinutes(10));
        user.setVerificationAttempts(0);
        user.setLastVerificationSentAt(now);

        userRepository.save(user);

        emailCenter.sendVerificationCode(email, verificationCode);

        return new MessageResponse("Se envió un nuevo código de verificación");
    }

    /**
     * Genera un código numérico de seis dígitos.
     *
     * @return código de verificación
     */
    private String generateVerificationCode() {

        int code = RANDOM.nextInt(1000000);

        return String.format("%06d", code);
    }
}
