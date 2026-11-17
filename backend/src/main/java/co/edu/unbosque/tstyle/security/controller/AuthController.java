
package co.edu.unbosque.tstyle.security.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import co.edu.unbosque.tstyle.security.dto.AuthResponse;
import co.edu.unbosque.tstyle.security.dto.ForgotPasswordRequest;
import co.edu.unbosque.tstyle.security.dto.LoginRequest;
import co.edu.unbosque.tstyle.security.dto.MessageResponse;
import co.edu.unbosque.tstyle.security.dto.ResetPasswordRequest;
import co.edu.unbosque.tstyle.security.dto.UserProfileResponse;
import co.edu.unbosque.tstyle.security.service.AuthService;

/**
 * Controlador encargado de los procesos de autenticación.
 *
 * Permite iniciar sesión, consultar el perfil del usuario autenticado y
 * gestionar la recuperación de contraseñas dentro de T Style.
 *
 * @author Equipo T Style
 * @version 1.0
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	/**
	 * Constructor del controlador de autenticación.
	 *
	 * @param authService servicio de autenticación
	 */
	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	/**
	 * Permite iniciar sesión en la plataforma.
	 *
	 * @param request correo y contraseña del usuario
	 * @return información del usuario y token JWT
	 */
	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {

		AuthResponse response = authService.login(request);

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	/**
	 * Obtiene la información del usuario que inició sesión.
	 *
	 * @param authentication información del usuario autenticado
	 * @return perfil del usuario autenticado
	 */
	@GetMapping("/me")
	public ResponseEntity<UserProfileResponse> getAuthenticatedUser(Authentication authentication) {

		UserProfileResponse response = authService.getAuthenticatedUser(authentication.getName());

		return ResponseEntity.ok(response);
	}

	/**
	 * Solicita el envío de un código para recuperar la contraseña.
	 *
	 * @param request correo electrónico asociado a la cuenta
	 * @return mensaje de confirmación
	 */
	@PostMapping("/forgot-password")
	public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {

		MessageResponse response = authService.forgotPassword(request);

		return ResponseEntity.ok(response);
	}

	/**
	 * Valida el código de recuperación y establece una nueva contraseña.
	 *
	 * @param request correo, código y nueva contraseña
	 * @return mensaje de confirmación
	 */
	@PostMapping("/reset-password")
	public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {

		MessageResponse response = authService.resetPassword(request);

		return ResponseEntity.ok(response);
	}
}
