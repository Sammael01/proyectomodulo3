
package co.edu.unbosque.tstyle.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

<<<<<<< HEAD
import co.edu.unbosque.tstyle.dto.MessageResponse;
import co.edu.unbosque.tstyle.dto.RegisterRequest;
import co.edu.unbosque.tstyle.enums.Role;
import co.edu.unbosque.tstyle.exception.DuplicateResourceException;
import co.edu.unbosque.tstyle.model.User;
import co.edu.unbosque.tstyle.repository.UserRepository;
import co.edu.unbosque.tstyle.service.UserService;
=======
import co.edu.unbosque.tstyle.audit.enums.AuditEventType;
import co.edu.unbosque.tstyle.audit.service.AuditService;
import co.edu.unbosque.tstyle.email.EmailCenter;
import co.edu.unbosque.tstyle.exception.BusinessException;
import co.edu.unbosque.tstyle.exception.DuplicateResourceException;
import co.edu.unbosque.tstyle.security.dto.MessageResponse;
import co.edu.unbosque.tstyle.security.dto.RegisterRequest;
import co.edu.unbosque.tstyle.security.dto.ResendVerificationRequest;
import co.edu.unbosque.tstyle.security.dto.VerifyEmailRequest;
import co.edu.unbosque.tstyle.user.enums.Role;
import co.edu.unbosque.tstyle.user.model.User;
import co.edu.unbosque.tstyle.user.repository.UserRepository;
>>>>>>> b3d96ed4654d7773c3d07c2555796c9f2776c929

/**
 * Pruebas unitarias para UserService.
 *
 * @author Equipo T Style
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private EmailCenter emailCenter;

	@Mock
	private AuditService auditService;

	@InjectMocks
	private UserService userService;

	private RegisterRequest registerRequest;

	@BeforeEach
	void setUp() {

		registerRequest = new RegisterRequest("Daniel Zapata", "daniel@tstyle.com", "Password123!");

		lenient().when(passwordEncoder.encode(any(String.class)))
				.thenAnswer(invocation -> "hashed-" + invocation.getArgument(0));
	}

	/**
	 * Comprueba el registro exitoso y el envío del código.
	 */
	@Test
	void testRegisterUserSuccessfully() {

		when(userRepository.existsByEmail("daniel@tstyle.com")).thenReturn(false);

		MessageResponse response = userService.registerUser(registerRequest, "127.0.0.1");

		assertEquals("Usuario registrado. Revisa tu correo para verificar la cuenta", response.getMessage());

		verify(userRepository).save(any(User.class));
		verify(emailCenter).sendVerificationCode(eq("daniel@tstyle.com"), any(String.class));

		verify(auditService).registerEvent(eq(AuditEventType.USER_REGISTERED), isNull(), eq("daniel@tstyle.com"),
				eq("127.0.0.1"), isNull(), isNull());
	}

	/**
	 * Comprueba que la contraseña se almacene cifrada.
	 */
	@Test
	void testRegisterUserEncryptsPassword() {

		when(userRepository.existsByEmail("daniel@tstyle.com")).thenReturn(false);

		userService.registerUser(registerRequest, "127.0.0.1");

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

		verify(userRepository).save(userCaptor.capture());

		User savedUser = userCaptor.getValue();

		assertNotEquals("Password123!", savedUser.getPassword());
		assertEquals("hashed-Password123!", savedUser.getPassword());
	}

	/**
	 * Comprueba que el usuario quede pendiente de verificación.
	 */
	@Test
	void testRegisterUserRemainsDisabled() {

		when(userRepository.existsByEmail("daniel@tstyle.com")).thenReturn(false);

		userService.registerUser(registerRequest, "127.0.0.1");

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

		verify(userRepository).save(userCaptor.capture());

		User savedUser = userCaptor.getValue();

		assertEquals(Role.CLIENT, savedUser.getRole());
		assertFalse(savedUser.isEmailVerified());
		assertFalse(savedUser.isEnabled());
		assertEquals(0, savedUser.getVerificationAttempts());
		assertTrue(savedUser.getVerificationExpiresAt().isAfter(LocalDateTime.now()));
		assertNotEquals(null, savedUser.getVerificationCodeHash());
	}

	/**
	 * Comprueba que se normalice el correo electrónico.
	 */
	@Test
	void testRegisterUserNormalizesEmail() {

		registerRequest.setEmail("  DANIEL@TSTYLE.COM  ");

		when(userRepository.existsByEmail("daniel@tstyle.com")).thenReturn(false);

		userService.registerUser(registerRequest, "127.0.0.1");

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

		verify(userRepository).save(userCaptor.capture());

		assertEquals("daniel@tstyle.com", userCaptor.getValue().getEmail());
	}

	/**
	 * Comprueba que no se permita registrar un correo duplicado.
	 */
	@Test
	void testRegisterUserWithExistingEmail() {

		when(userRepository.existsByEmail("daniel@tstyle.com")).thenReturn(true);

		assertThrows(DuplicateResourceException.class, () -> userService.registerUser(registerRequest, "127.0.0.1"));

		verify(userRepository, never()).save(any(User.class));
		verify(emailCenter, never()).sendVerificationCode(any(String.class), any(String.class));
	}

	/**
	 * Comprueba que un código correcto active la cuenta y genere auditoría.
	 */
	@Test
	void testVerifyEmailSuccessfully() {

		User user = createUnverifiedUser();

		when(userRepository.findByEmail("daniel@tstyle.com")).thenReturn(Optional.of(user));

		when(passwordEncoder.matches("123456", "hashed-code")).thenReturn(true);

		MessageResponse response = userService.verifyEmail(new VerifyEmailRequest("daniel@tstyle.com", "123456"),
				"127.0.0.1");

		assertEquals("Correo verificado correctamente", response.getMessage());
		assertTrue(user.isEmailVerified());
		assertTrue(user.isEnabled());
		assertEquals(null, user.getVerificationCodeHash());
		assertEquals(0, user.getVerificationAttempts());

		verify(userRepository).save(user);

		verify(auditService).registerEvent(eq(AuditEventType.EMAIL_VERIFICATION_SUCCESS), isNull(),
				eq("daniel@tstyle.com"), eq("127.0.0.1"), isNull(), isNull());
	}

	/**
	 * Comprueba que un código incorrecto aumente los intentos y genere auditoría.
	 */
	@Test
	void testVerifyEmailWithIncorrectCode() {

		User user = createUnverifiedUser();

		when(userRepository.findByEmail("daniel@tstyle.com")).thenReturn(Optional.of(user));

		when(passwordEncoder.matches("999999", "hashed-code")).thenReturn(false);

		assertThrows(BusinessException.class,
				() -> userService.verifyEmail(new VerifyEmailRequest("daniel@tstyle.com", "999999"), "127.0.0.1"));

		assertEquals(1, user.getVerificationAttempts());
		assertFalse(user.isEmailVerified());

		verify(auditService).registerEvent(eq(AuditEventType.EMAIL_VERIFICATION_FAILURE), isNull(),
				eq("daniel@tstyle.com"), eq("127.0.0.1"), isNull(), isNull());
	}

	/**
	 * Comprueba que se bloquee la verificación después de cinco intentos.
	 */
	@Test
	void testVerifyEmailWithMaximumAttempts() {

		User user = createUnverifiedUser();
		user.setVerificationAttempts(5);

		when(userRepository.findByEmail("daniel@tstyle.com")).thenReturn(Optional.of(user));

		assertThrows(BusinessException.class,
				() -> userService.verifyEmail(new VerifyEmailRequest("daniel@tstyle.com", "123456"), "127.0.0.1"));

		assertEquals(5, user.getVerificationAttempts());

		verify(passwordEncoder, never()).matches(any(String.class), any(String.class));
	}

	/**
	 * Comprueba que no se permita utilizar un código vencido.
	 */
	@Test
	void testVerifyEmailWithExpiredCode() {

		User user = createUnverifiedUser();
		user.setVerificationExpiresAt(LocalDateTime.now().minusMinutes(1));

		when(userRepository.findByEmail("daniel@tstyle.com")).thenReturn(Optional.of(user));

		assertThrows(BusinessException.class,
				() -> userService.verifyEmail(new VerifyEmailRequest("daniel@tstyle.com", "123456"), "127.0.0.1"));

		assertEquals(null, user.getVerificationCodeHash());
		assertEquals(null, user.getVerificationExpiresAt());

		verify(passwordEncoder, never()).matches(any(String.class), any(String.class));
	}

	/**
	 * Comprueba que no se pueda verificar una cuenta dos veces.
	 */
	@Test
	void testVerifyEmailAlreadyVerified() {

		User user = createUnverifiedUser();
		user.setEmailVerified(true);
		user.setEnabled(true);

		when(userRepository.findByEmail("daniel@tstyle.com")).thenReturn(Optional.of(user));

		assertThrows(BusinessException.class,
				() -> userService.verifyEmail(new VerifyEmailRequest("daniel@tstyle.com", "123456"), "127.0.0.1"));

		verify(passwordEncoder, never()).matches(any(String.class), any(String.class));
		verify(userRepository, never()).save(user);
	}

	/**
	 * Comprueba que se pueda solicitar un nuevo código y genere auditoría.
	 */
	@Test
	void testResendVerificationCodeSuccessfully() {

		User user = createUnverifiedUser();
		user.setLastVerificationSentAt(LocalDateTime.now().minusMinutes(2));

		when(userRepository.findByEmail("daniel@tstyle.com")).thenReturn(Optional.of(user));

		MessageResponse response = userService
				.resendVerificationCode(new ResendVerificationRequest("daniel@tstyle.com"), "127.0.0.1");

		assertEquals("Se envió un nuevo código de verificación", response.getMessage());

		assertEquals(0, user.getVerificationAttempts());
		assertTrue(user.getVerificationExpiresAt().isAfter(LocalDateTime.now()));

		verify(userRepository).save(user);
		verify(emailCenter).sendVerificationCode(eq("daniel@tstyle.com"), any(String.class));

		verify(auditService).registerEvent(eq(AuditEventType.VERIFICATION_CODE_RESENT), isNull(),
				eq("daniel@tstyle.com"), eq("127.0.0.1"), isNull(), isNull());
	}

	/**
	 * Comprueba que se respete el tiempo de espera para reenviar un código.
	 */
	@Test
	void testResendVerificationCodeTooSoon() {

		User user = createUnverifiedUser();
		user.setLastVerificationSentAt(LocalDateTime.now().minusSeconds(30));

		when(userRepository.findByEmail("daniel@tstyle.com")).thenReturn(Optional.of(user));

		assertThrows(BusinessException.class, () -> userService
				.resendVerificationCode(new ResendVerificationRequest("daniel@tstyle.com"), "127.0.0.1"));

		verify(userRepository, never()).save(user);
		verify(emailCenter, never()).sendVerificationCode(any(String.class), any(String.class));
	}

	/**
	 * Crea un usuario pendiente de verificación para las pruebas.
	 *
	 * @return usuario de prueba
	 */
	private User createUnverifiedUser() {

		User user = new User("Daniel Zapata", "daniel@tstyle.com", "hashed-password", Role.CLIENT);

		user.setVerificationCodeHash("hashed-code");
		user.setVerificationExpiresAt(LocalDateTime.now().plusMinutes(10));
		user.setVerificationAttempts(0);
		user.setLastVerificationSentAt(LocalDateTime.now().minusMinutes(2));

		return user;
	}
}
