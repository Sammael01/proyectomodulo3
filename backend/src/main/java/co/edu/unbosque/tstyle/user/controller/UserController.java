
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
import jakarta.validation.Valid;

/**
 * Controlador encargado de gestionar las operaciones
 * relacionadas con los usuarios de T Style.
 *
 * @author Equipo T Style
 */
@RestController
@RequestMapping("/api/users")
@Validated
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
     * @param request datos del registro
     * @return respuesta de confirmación
     */
    @PostMapping("/register")
    public ResponseEntity<MessageResponse> registerUser(
            @Valid @RequestBody RegisterRequest request) {

        MessageResponse response = userService.registerUser(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Verifica el correo electrónico de un usuario.
     *
     * @param request correo y código de verificación
     * @return respuesta de confirmación
     */
    @PostMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request) {

        MessageResponse response = userService.verifyEmail(request);

        return ResponseEntity.ok(response);
    }

    /**
     * Solicita el envío de un nuevo código de verificación.
     *
     * @param request correo electrónico del usuario
     * @return respuesta de confirmación
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<MessageResponse> resendVerificationCode(
            @Valid @RequestBody ResendVerificationRequest request) {

        MessageResponse response =
            userService.resendVerificationCode(request);

        return ResponseEntity.ok(response);
    }
}
