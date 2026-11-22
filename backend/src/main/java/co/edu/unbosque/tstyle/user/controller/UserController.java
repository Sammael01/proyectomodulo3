
package co.edu.unbosque.tstyle.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.tstyle.security.dto.MessageResponse;
import co.edu.unbosque.tstyle.security.dto.RegisterRequest;
import co.edu.unbosque.tstyle.security.dto.ResendVerificationRequest;
import co.edu.unbosque.tstyle.security.dto.VerifyEmailRequest;
import co.edu.unbosque.tstyle.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador encargado de gestionar las operaciones relacionadas con los
 * usuarios de T Style.
 *
 * @author Equipo T Style
 */
@RestController
@RequestMapping("/api/users")
@Validated
@Tag(name = "Usuarios", description = "Registro y verificación de cuentas de usuario")
public class UserController {

	private final UserService userService;

	/**
	 * Constructor del controlador de usuarios.
	 *
	 * @param userService servicio encargado de gestionar los usuarios
	 */
	public UserController(UserService userService) {
		this.userService = userService;
	}

	/**
	 * Registra un nuevo usuario.
	 *
	 * @param request     datos del registro
	 * @param httpRequest petición HTTP para obtener la IP
	 * @return respuesta de confirmación
	 */
	@PostMapping("/register")
	@Operation(summary = "Registrar usuario", description = "Crea una nueva cuenta de usuario en T Style.")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Usuario registrado correctamente"),
			@ApiResponse(responseCode = "400", description = "Datos de registro inválidos"),
			@ApiResponse(responseCode = "409", description = "El correo ya está registrado") })
	public ResponseEntity<MessageResponse> registerUser(@Valid @RequestBody RegisterRequest request,
			HttpServletRequest httpRequest) {

		MessageResponse response = userService.registerUser(request, httpRequest.getRemoteAddr());

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	/**
	 * Verifica el correo electrónico de un usuario.
	 *
	 * @param request     correo y código de verificación
	 * @param httpRequest petición HTTP para obtener la IP
	 * @return respuesta de confirmación
	 */
	@PostMapping("/verify-email")
	@Operation(summary = "Verificar correo electrónico", description = "Valida el código enviado al correo electrónico del usuario.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Correo verificado correctamente"),
			@ApiResponse(responseCode = "400", description = "Código o datos inválidos") })
	public ResponseEntity<MessageResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request,
			HttpServletRequest httpRequest) {

		MessageResponse response = userService.verifyEmail(request, httpRequest.getRemoteAddr());

		return ResponseEntity.ok(response);
	}

	/**
	 * Solicita el envío de un nuevo código de verificación.
	 *
	 * @param request     correo electrónico del usuario
	 * @param httpRequest petición HTTP para obtener la IP
	 * @return respuesta de confirmación
	 */
	@PostMapping("/resend-verification")
	@Operation(summary = "Reenviar código de verificación", description = "Solicita el envío de un nuevo código de verificación al correo del usuario.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Solicitud procesada"),
			@ApiResponse(responseCode = "400", description = "Correo o solicitud inválida") })
	public ResponseEntity<MessageResponse> resendVerificationCode(@Valid @RequestBody ResendVerificationRequest request,
			HttpServletRequest httpRequest) {

		MessageResponse response = userService.resendVerificationCode(request, httpRequest.getRemoteAddr());

		return ResponseEntity.ok(response);
	}
}
