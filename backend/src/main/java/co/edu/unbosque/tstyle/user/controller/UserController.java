
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
import co.edu.unbosque.tstyle.user.service.UserService;

import jakarta.validation.Valid;

/**
 * Controlador encargado de gestionar las solicitudes HTTP de usuarios.
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
     * @param userService servicio encargado de la lógica de usuarios
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Registra un nuevo cliente en la plataforma.
     *
     * @param request información necesaria para registrar al cliente
     * @return respuesta con el resultado del registro
     */
    @PostMapping("/register")
    public ResponseEntity<MessageResponse> registerUser(
            @Valid @RequestBody RegisterRequest request) {

        MessageResponse response = userService.registerUser(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
