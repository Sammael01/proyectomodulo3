
package co.edu.unbosque.tstyle.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

<<<<<<< HEAD:backend/src/main/java/co/edu/unbosque/tstyle/service/UserService.java
import co.edu.unbosque.tstyle.dto.MessageResponse;
import co.edu.unbosque.tstyle.dto.RegisterRequest;
import co.edu.unbosque.tstyle.enums.Role;
import co.edu.unbosque.tstyle.exception.DuplicateResourceException;
import co.edu.unbosque.tstyle.model.User;
import co.edu.unbosque.tstyle.repository.UserRepository;
=======
import co.edu.unbosque.tstyle.audit.enums.AuditEventType;
import co.edu.unbosque.tstyle.audit.service.AuditService;
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
>>>>>>> b3d96ed4654d7773c3d07c2555796c9f2776c929:backend/src/main/java/co/edu/unbosque/tstyle/user/service/UserService.java

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
	private final AuditService auditService;

	private static final SecureRandom RANDOM = new SecureRandom();

	/**
	 * Constructor del servicio de usuarios.
	 *
	 * @param userRepository  repositorio de usuarios
	 * @param passwordEncoder codificador de contraseñas
	 * @param emailCenter     servicio encargado de enviar correos
	 * @param auditService    servicio encargado de registrar eventos
	 */
	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailCenter emailCenter,
			AuditService auditService) {

		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.emailCenter = emailCenter;
		this.auditService = auditService;
	}

	/**
	 * Registra un nuevo cliente y envía un código de verificación.
	 *
	 * @param request   datos necesarios para el registro
	 * @param ipAddress dirección IP de origen
	 * @return mensaje de confirmación
	 */
	@Transactional
	public MessageResponse registerUser(RegisterRequest request, String ipAddress) {

		String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

		if (userRepository.existsByEmail(email)) {
			throw new DuplicateResourceException("El correo electrónico ya se encuentra registrado");
		}

		String encryptedPassword = passwordEncoder.encode(request.getPassword());

		User user = new User(request.getName().trim(), email, encryptedPassword, Role.CLIENT);

		String verificationCode = generateVerificationCode();

		user.setVerificationCodeHash(passwordEncoder.encode(verificationCode));

		user.setVerificationExpiresAt(LocalDateTime.now().plusMinutes(10));

		user.setVerificationAttempts(0);
		user.setLastVerificationSentAt(LocalDateTime.now());

		userRepository.save(user);

		emailCenter.sendVerificationCode(email, verificationCode);

		auditService.registerEvent(AuditEventType.USER_REGISTERED, user.getId(), email, ipAddress, null, null);

		return new MessageResponse("Usuario registrado. Revisa tu correo para verificar la cuenta");
	}

	/**
	 * Verifica el código enviado al correo y activa la cuenta.
	 *
	 * @param request   correo y código de verificación
	 * @param ipAddress dirección IP de origen
	 * @return mensaje de confirmación
	 */
	@Transactional(noRollbackFor = BusinessException.class)
	public MessageResponse verifyEmail(VerifyEmailRequest request, String ipAddress) {

		String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("No se encontró un usuario con ese correo"));

		try {

			if (user.isEmailVerified()) {
				throw new BusinessException("El correo ya se encuentra verificado");
			}

			if (user.getVerificationCodeHash() == null || user.getVerificationExpiresAt() == null) {
				throw new BusinessException("Solicita un nuevo código de verificación");
			}

			if (user.getVerificationExpiresAt().isBefore(LocalDateTime.now())) {
				user.setVerificationCodeHash(null);
				user.setVerificationExpiresAt(null);
				throw new BusinessException("El código ha vencido. Solicita uno nuevo");
			}

			if (user.getVerificationAttempts() >= 5) {
				throw new BusinessException("Superaste el máximo de intentos. Solicita un nuevo código");
			}

			user.setVerificationAttempts(user.getVerificationAttempts() + 1);

			if (!passwordEncoder.matches(request.getCode(), user.getVerificationCodeHash())) {

				throw new BusinessException("El código de verificación es incorrecto");
			}

			user.setEmailVerified(true);
			user.setEnabled(true);
			user.setVerificationCodeHash(null);
			user.setVerificationExpiresAt(null);
			user.setVerificationAttempts(0);

			userRepository.save(user);

			auditService.registerEvent(AuditEventType.EMAIL_VERIFICATION_SUCCESS, user.getId(), email, ipAddress, null,
					null);

			return new MessageResponse("Correo verificado correctamente");

		} catch (BusinessException exception) {

			auditService.registerEvent(AuditEventType.EMAIL_VERIFICATION_FAILURE, user.getId(), email, ipAddress, null,
					null);

			throw exception;
		}
	}

	/**
	 * Genera y envía un nuevo código de verificación.
	 *
	 * @param request   correo del usuario
	 * @param ipAddress dirección IP de origen
	 * @return mensaje de confirmación
	 */
	@Transactional
	public MessageResponse resendVerificationCode(ResendVerificationRequest request, String ipAddress) {

		String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("No se encontró un usuario con ese correo"));

		if (user.isEmailVerified()) {
			throw new BusinessException("El correo ya se encuentra verificado");
		}

		LocalDateTime now = LocalDateTime.now();

		if (user.getLastVerificationSentAt() != null && user.getLastVerificationSentAt().plusSeconds(60).isAfter(now)) {
			throw new BusinessException("Debes esperar 60 segundos antes de solicitar otro código");
		}

		String verificationCode = generateVerificationCode();

		user.setVerificationCodeHash(passwordEncoder.encode(verificationCode));

		user.setVerificationExpiresAt(now.plusMinutes(10));
		user.setVerificationAttempts(0);
		user.setLastVerificationSentAt(now);

		userRepository.save(user);

		emailCenter.sendVerificationCode(email, verificationCode);

		auditService.registerEvent(AuditEventType.VERIFICATION_CODE_RESENT, user.getId(), email, ipAddress, null, null);

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
