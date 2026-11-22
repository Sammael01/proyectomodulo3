
package co.edu.unbosque.tstyle.security.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.tstyle.audit.enums.AuditEventType;
import co.edu.unbosque.tstyle.audit.service.AuditService;
import co.edu.unbosque.tstyle.email.EmailCenter;
import co.edu.unbosque.tstyle.exception.BusinessException;
import co.edu.unbosque.tstyle.exception.ResourceNotFoundException;
import co.edu.unbosque.tstyle.security.dto.AuthResponse;
import co.edu.unbosque.tstyle.security.dto.ForgotPasswordRequest;
import co.edu.unbosque.tstyle.security.dto.LoginRequest;
import co.edu.unbosque.tstyle.security.dto.MessageResponse;
import co.edu.unbosque.tstyle.security.dto.ResetPasswordRequest;
import co.edu.unbosque.tstyle.security.dto.UserProfileResponse;
import co.edu.unbosque.tstyle.user.model.User;
import co.edu.unbosque.tstyle.user.repository.UserRepository;

/**
 * Contiene la lógica de autenticación y recuperación de usuarios.
 *
 * Permite iniciar sesión, consultar el perfil autenticado, solicitar la
 * recuperación de contraseña y establecer una nueva contraseña mediante un
 * código enviado al correo.
 *
 * @author Equipo T Style
 * @version 1.0
 */
@Service
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final EmailCenter emailCenter;
	private final AuditService auditService;

	private static final SecureRandom RANDOM = new SecureRandom();

	/**
	 * Constructor del servicio de autenticación.
	 *
	 * @param userRepository  repositorio de usuarios
	 * @param passwordEncoder codificador de contraseñas
	 * @param jwtService      servicio encargado de generar tokens
	 * @param emailCenter     servicio encargado del envío de correos
	 * @param auditService    servicio de auditoría
	 */
	public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
			EmailCenter emailCenter, AuditService auditService) {

		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.emailCenter = emailCenter;
		this.auditService = auditService;
	}

	/**
	 * Autentica un usuario y genera su token JWT.
	 *
	 * @param request   correo y contraseña
	 * @param ipAddress dirección IP de la solicitud
	 * @return datos del usuario y token de autenticación
	 */
	public AuthResponse login(LoginRequest request, String ipAddress) {

		String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

		User user = userRepository.findByEmail(email).orElse(null);

		if (user == null) {
			auditService.registerEvent(AuditEventType.LOGIN_FAILURE, null, null, ipAddress, null, null);

			throw new BusinessException("Correo o contraseña incorrectos");
		}

		if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			auditService.registerEvent(AuditEventType.LOGIN_FAILURE, user.getId(), user.getEmail(), ipAddress, null,
					null);

			throw new BusinessException("Correo o contraseña incorrectos");
		}

		if (!user.isEmailVerified()) {
			auditService.registerEvent(AuditEventType.LOGIN_FAILURE, user.getId(), user.getEmail(), ipAddress, null,
					null);

			throw new BusinessException("Debes verificar tu correo electrónico");
		}

		if (!user.isEnabled()) {
			auditService.registerEvent(AuditEventType.LOGIN_FAILURE, user.getId(), user.getEmail(), ipAddress, null,
					null);

			throw new BusinessException("Tu cuenta se encuentra deshabilitada");
		}

		String token = jwtService.generateToken(user);

		auditService.registerEvent(AuditEventType.LOGIN_SUCCESS, user.getId(), user.getEmail(), ipAddress, null, null);

		return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole().name());
	}

	/**
	 * Consulta el perfil del usuario autenticado.
	 *
	 * @param email correo electrónico del usuario autenticado
	 * @return información básica del perfil del usuario
	 */
	public UserProfileResponse getAuthenticatedUser(String email) {

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("No se encontró el usuario autenticado"));

		return new UserProfileResponse(user.getId(), user.getName(), user.getEmail(), user.getRole().name());
	}

	/**
	 * Genera y envía un código para recuperar la contraseña.
	 *
	 * La respuesta es genérica para evitar revelar si el correo electrónico está
	 * registrado en la plataforma.
	 *
	 * @param request   correo electrónico del usuario
	 * @param ipAddress dirección IP de la solicitud
	 * @return mensaje de confirmación
	 */
	@Transactional
	public MessageResponse forgotPassword(ForgotPasswordRequest request, String ipAddress) {

		String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

		String responseMessage = "Si el correo está registrado y la cuenta está habilitada, "
				+ "recibirás un código de recuperación";

		User user = userRepository.findByEmail(email).orElse(null);

		if (user == null || !user.isEmailVerified() || !user.isEnabled()) {

			auditService.registerEvent(AuditEventType.PASSWORD_RESET_REQUEST, null, null, ipAddress, null, null);

			return new MessageResponse(responseMessage);
		}

		auditService.registerEvent(AuditEventType.PASSWORD_RESET_REQUEST, user.getId(), user.getEmail(), ipAddress,
				null, null);

		LocalDateTime now = LocalDateTime.now();

		if (user.getLastResetSentAt() != null && user.getLastResetSentAt().plusSeconds(60).isAfter(now)) {

			return new MessageResponse(responseMessage);
		}

		String resetCode = generateResetCode();

		user.setResetCodeHash(passwordEncoder.encode(resetCode));
		user.setResetExpiresAt(now.plusMinutes(10));
		user.setResetAttempts(0);
		user.setLastResetSentAt(now);

		userRepository.save(user);

		emailCenter.sendPasswordResetCode(email, resetCode);

		return new MessageResponse(responseMessage);
	}

	/**
	 * Valida el código de recuperación y establece una nueva contraseña.
	 *
	 * Al actualizar la contraseña, incrementa la versión de autenticación para
	 * invalidar los tokens JWT que fueron emitidos anteriormente.
	 *
	 * Los intentos fallidos y la expiración se conservan en la base de datos aunque
	 * el método genere una excepción de negocio.
	 *
	 * @param request   correo, código y nueva contraseña
	 * @param ipAddress dirección IP de la solicitud
	 * @return mensaje de confirmación
	 */
	@Transactional(noRollbackFor = BusinessException.class)
	public MessageResponse resetPassword(ResetPasswordRequest request, String ipAddress) {

		String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

		User user = userRepository.findByEmail(email).orElse(null);

		try {

			if (user == null) {
				throw new BusinessException("El código de recuperación no es válido");
			}

			if (!user.isEmailVerified() || !user.isEnabled()) {
				throw new BusinessException("El código de recuperación no es válido");
			}

			if (user.getResetCodeHash() == null || user.getResetExpiresAt() == null) {
				throw new BusinessException("No existe un código de recuperación activo");
			}

			if (user.getResetExpiresAt().isBefore(LocalDateTime.now())) {
				clearResetCode(user);

				throw new BusinessException("El código de recuperación ha vencido. Solicita uno nuevo");
			}

			if (user.getResetAttempts() >= 5) {
				throw new BusinessException("Superaste el máximo de intentos. Solicita un nuevo código");
			}

			user.setResetAttempts(user.getResetAttempts() + 1);

			if (!passwordEncoder.matches(request.getCode(), user.getResetCodeHash())) {

				throw new BusinessException("El código de recuperación es incorrecto");
			}

			LocalDateTime now = LocalDateTime.now();

			user.setPassword(passwordEncoder.encode(request.getNewPassword()));
			user.setPasswordChangedAt(now);
			user.setTokenVersion(user.getTokenVersion() + 1);

			clearResetCode(user);

			userRepository.save(user);

			auditService.registerEvent(AuditEventType.PASSWORD_RESET_SUCCESS, user.getId(), user.getEmail(), ipAddress,
					null, null);

			return new MessageResponse("La contraseña se actualizó correctamente");

		} catch (BusinessException exception) {

			auditService.registerEvent(AuditEventType.PASSWORD_RESET_FAILURE, user != null ? user.getId() : null,
					user != null ? user.getEmail() : null, ipAddress, null, null);

			throw exception;
		}
	}

	/**
	 * Genera un código numérico aleatorio de seis dígitos.
	 *
	 * @return código de recuperación
	 */
	private String generateResetCode() {

		int code = RANDOM.nextInt(1000000);

		return String.format("%06d", code);
	}

	/**
	 * Elimina los datos temporales de recuperación de contraseña.
	 *
	 * @param user usuario cuyos datos de recuperación se limpiarán
	 */
	private void clearResetCode(User user) {

		user.setResetCodeHash(null);
		user.setResetExpiresAt(null);
		user.setResetAttempts(0);
	}
}
