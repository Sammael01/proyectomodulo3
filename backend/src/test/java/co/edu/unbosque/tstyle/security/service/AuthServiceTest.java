
package co.edu.unbosque.tstyle.security.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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
import co.edu.unbosque.tstyle.user.enums.Role;
import co.edu.unbosque.tstyle.user.model.User;
import co.edu.unbosque.tstyle.user.repository.UserRepository;

/**
 * Pruebas unitarias para el servicio de autenticación.
 *
 * Verifica el inicio de sesión, consulta del perfil y recuperación de
 * contraseña de los usuarios de T Style.
 *
 * @author Equipo T Style
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtService jwtService;

	@Mock
	private EmailCenter emailCenter;

	@Mock
	private AuditService auditService;

	@InjectMocks
	private AuthService authService;

	private User user;

	private final String ipAddress = "127.0.0.1";

	@BeforeEach
	void setUp() {
		user = new User("Daniel Zapata", "daniel@tstyle.com", "hashed-password", Role.CLIENT);
		user.setId(1L);
		user.setEmailVerified(true);
		user.setEnabled(true);
		user.setTokenVersion(0);
	}

	@Test
	void testLoginSuccessfully() {
		when(userRepository.findByEmail("daniel@tstyle.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("Password123!", "hashed-password")).thenReturn(true);
		when(jwtService.generateToken(user)).thenReturn("jwt-token");

		AuthResponse response = authService.login(new LoginRequest("daniel@tstyle.com", "Password123!"), ipAddress);

		assertEquals("jwt-token", response.getToken());
		assertEquals(1L, response.getUserId());
		assertEquals("Daniel Zapata", response.getName());
		assertEquals("daniel@tstyle.com", response.getEmail());
		assertEquals("CLIENT", response.getRole());

		verify(jwtService).generateToken(user);
		verify(auditService).registerEvent(eq(AuditEventType.LOGIN_SUCCESS), eq(1L), eq("daniel@tstyle.com"),
				eq(ipAddress), isNull(), isNull());
	}

	@Test
	void testLoginWithNonexistentEmail() {
		when(userRepository.findByEmail("daniel@tstyle.com")).thenReturn(Optional.empty());

		assertThrows(BusinessException.class,
				() -> authService.login(new LoginRequest("daniel@tstyle.com", "Password123!"), ipAddress));

		verify(jwtService, never()).generateToken(any(User.class));
	}

	@Test
	void testLoginWithIncorrectPassword() {
		when(userRepository.findByEmail("daniel@tstyle.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("WrongPassword123!", "hashed-password")).thenReturn(false);

		assertThrows(BusinessException.class,
				() -> authService.login(new LoginRequest("daniel@tstyle.com", "WrongPassword123!"), ipAddress));

		verify(jwtService, never()).generateToken(any(User.class));
		verify(auditService).registerEvent(eq(AuditEventType.LOGIN_FAILURE), eq(1L), eq("daniel@tstyle.com"),
				eq(ipAddress), isNull(), isNull());
	}

	@Test
	void testLoginWithUnverifiedEmail() {
		user.setEmailVerified(false);

		when(userRepository.findByEmail("daniel@tstyle.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("Password123!", "hashed-password")).thenReturn(true);

		assertThrows(BusinessException.class,
				() -> authService.login(new LoginRequest("daniel@tstyle.com", "Password123!"), ipAddress));

		verify(jwtService, never()).generateToken(any(User.class));
	}

	@Test
	void testLoginWithDisabledAccount() {
		user.setEnabled(false);

		when(userRepository.findByEmail("daniel@tstyle.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("Password123!", "hashed-password")).thenReturn(true);

		assertThrows(BusinessException.class,
				() -> authService.login(new LoginRequest("daniel@tstyle.com", "Password123!"), ipAddress));

		verify(jwtService, never()).generateToken(any(User.class));
	}

	@Test
	void testLoginNormalizesEmail() {
		when(userRepository.findByEmail("daniel@tstyle.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("Password123!", "hashed-password")).thenReturn(true);
		when(jwtService.generateToken(user)).thenReturn("jwt-token");

		authService.login(new LoginRequest(" DANIEL@TSTYLE.COM ", "Password123!"), ipAddress);

		verify(userRepository).findByEmail("daniel@tstyle.com");
	}

	@Test
	void testGetAuthenticatedUserSuccessfully() {
		when(userRepository.findByEmail("daniel@tstyle.com")).thenReturn(Optional.of(user));

		UserProfileResponse response = authService.getAuthenticatedUser("daniel@tstyle.com");

		assertEquals(1L, response.getUserId());
		assertEquals("Daniel Zapata", response.getName());
		assertEquals("daniel@tstyle.com", response.getEmail());
		assertEquals("CLIENT", response.getRole());
	}

	@Test
	void testGetAuthenticatedUserNotFound() {
		when(userRepository.findByEmail("daniel@tstyle.com")).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> authService.getAuthenticatedUser("daniel@tstyle.com"));
	}

	@Test
	void testForgotPasswordSuccessfully() {
		when(userRepository.findByEmail("daniel@tstyle.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.encode(any(String.class))).thenReturn("hashed-reset-code");

		MessageResponse response = authService.forgotPassword(new ForgotPasswordRequest("daniel@tstyle.com"),
				ipAddress);

		assertEquals(
				"Si el correo está registrado y la cuenta está habilitada, " + "recibirás un código de recuperación",
				response.getMessage());

		assertEquals("hashed-reset-code", user.getResetCodeHash());
		assertNotNull(user.getResetExpiresAt());
		assertEquals(0, user.getResetAttempts());
		assertNotNull(user.getLastResetSentAt());

		verify(userRepository).save(user);
		verify(emailCenter).sendPasswordResetCode(eq("daniel@tstyle.com"), any(String.class));
		verify(auditService).registerEvent(eq(AuditEventType.PASSWORD_RESET_REQUEST), eq(1L), eq("daniel@tstyle.com"),
				eq(ipAddress), isNull(), isNull());
	}

	@Test
	void testForgotPasswordWithNonexistentEmail() {
		when(userRepository.findByEmail("daniel@tstyle.com")).thenReturn(Optional.empty());

		MessageResponse response = authService.forgotPassword(new ForgotPasswordRequest("daniel@tstyle.com"),
				ipAddress);

		assertTrue(response.getMessage().contains("Si el correo está registrado"));

		verify(userRepository, never()).save(any(User.class));
		verify(emailCenter, never()).sendPasswordResetCode(any(String.class), any(String.class));
	}

	@Test
	void testForgotPasswordRespectsCooldown() {
		user.setLastResetSentAt(LocalDateTime.now().minusSeconds(30));

		when(userRepository.findByEmail("daniel@tstyle.com")).thenReturn(Optional.of(user));

		authService.forgotPassword(new ForgotPasswordRequest("daniel@tstyle.com"), ipAddress);

		verify(userRepository, never()).save(user);
		verify(emailCenter, never()).sendPasswordResetCode(any(String.class), any(String.class));
	}

	@Test
	void testResetPasswordWithIncorrectCode() {
		user.setResetCodeHash("hashed-reset-code");
		user.setResetExpiresAt(LocalDateTime.now().plusMinutes(10));
		user.setResetAttempts(0);

		when(userRepository.findByEmail("daniel@tstyle.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("999999", "hashed-reset-code")).thenReturn(false);

		assertThrows(BusinessException.class, () -> authService
				.resetPassword(new ResetPasswordRequest("daniel@tstyle.com", "999999", "NewPassword123!"), ipAddress));

		assertEquals(1, user.getResetAttempts());
		verify(userRepository, never()).save(user);
	}

	@Test
	void testResetPasswordWithExpiredCode() {
		user.setResetCodeHash("hashed-reset-code");
		user.setResetExpiresAt(LocalDateTime.now().minusMinutes(1));

		when(userRepository.findByEmail("daniel@tstyle.com")).thenReturn(Optional.of(user));

		assertThrows(BusinessException.class, () -> authService
				.resetPassword(new ResetPasswordRequest("daniel@tstyle.com", "123456", "NewPassword123!"), ipAddress));

		assertEquals(null, user.getResetCodeHash());
		assertEquals(null, user.getResetExpiresAt());
		assertEquals(0, user.getResetAttempts());
	}

	@Test
	void testResetPasswordWithMaximumAttempts() {
		user.setResetCodeHash("hashed-reset-code");
		user.setResetExpiresAt(LocalDateTime.now().plusMinutes(10));
		user.setResetAttempts(5);

		when(userRepository.findByEmail("daniel@tstyle.com")).thenReturn(Optional.of(user));

		assertThrows(BusinessException.class, () -> authService
				.resetPassword(new ResetPasswordRequest("daniel@tstyle.com", "123456", "NewPassword123!"), ipAddress));

		assertEquals(5, user.getResetAttempts());

		verify(passwordEncoder, never()).matches(any(String.class), any(String.class));
	}

	@Test
	void testResetPasswordSuccessfully() {
		user.setResetCodeHash("hashed-reset-code");
		user.setResetExpiresAt(LocalDateTime.now().plusMinutes(10));
		user.setResetAttempts(0);
		user.setTokenVersion(2);

		when(userRepository.findByEmail("daniel@tstyle.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("123456", "hashed-reset-code")).thenReturn(true);
		when(passwordEncoder.encode("NewPassword123!")).thenReturn("new-hashed-password");

		MessageResponse response = authService
				.resetPassword(new ResetPasswordRequest("daniel@tstyle.com", "123456", "NewPassword123!"), ipAddress);

		assertEquals("La contraseña se actualizó correctamente", response.getMessage());

		assertEquals("new-hashed-password", user.getPassword());
		assertEquals(3, user.getTokenVersion());
		assertNotNull(user.getPasswordChangedAt());

		assertEquals(null, user.getResetCodeHash());
		assertEquals(null, user.getResetExpiresAt());
		assertEquals(0, user.getResetAttempts());

		verify(userRepository).save(user);
		verify(auditService).registerEvent(eq(AuditEventType.PASSWORD_RESET_SUCCESS), eq(1L), eq("daniel@tstyle.com"),
				eq(ipAddress), isNull(), isNull());
	}
}
