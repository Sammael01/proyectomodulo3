
package co.edu.unbosque.tstyle.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.unbosque.tstyle.exception.DuplicateResourceException;
import co.edu.unbosque.tstyle.security.dto.MessageResponse;
import co.edu.unbosque.tstyle.security.dto.RegisterRequest;
import co.edu.unbosque.tstyle.security.dto.ResendVerificationRequest;
import co.edu.unbosque.tstyle.security.dto.VerifyEmailRequest;
import co.edu.unbosque.tstyle.user.service.UserService;

/**
 * Pruebas HTTP para el controlador de usuarios.
 *
 * @author Equipo T Style
 */
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    /**
     * Comprueba el registro exitoso de un usuario.
     */
    @Test
    void testRegisterUserSuccessfully() throws Exception {

        when(userService.registerUser(any(RegisterRequest.class)))
            .thenReturn(new MessageResponse(
                "Usuario registrado. Revisa tu correo para verificar la cuenta"
            ));

        String json = """
            {
                "name": "Daniel Zapata",
                "email": "daniel@tstyle.com",
                "password": "Password123!"
            }
            """;

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.message")
                .value("Usuario registrado. Revisa tu correo para verificar la cuenta"));
    }

    /**
     * Comprueba que el controlador rechace los datos inválidos.
     */
    @Test
    void testRegisterUserWithInvalidData() throws Exception {

        String json = """
            {
                "name": "",
                "email": "correo-invalido",
                "password": "123"
            }
            """;

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    /**
     * Comprueba que el controlador responda con conflicto
     * cuando el correo ya está registrado.
     */
    @Test
    void testRegisterUserWithExistingEmail() throws Exception {

        when(userService.registerUser(any(RegisterRequest.class)))
            .thenThrow(new DuplicateResourceException(
                "El correo electrónico ya se encuentra registrado"
            ));

        String json = """
            {
                "name": "Daniel Zapata",
                "email": "daniel@tstyle.com",
                "password": "Password123!"
            }
            """;

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    /**
     * Comprueba la verificación exitosa del correo.
     */
    @Test
    void testVerifyEmailSuccessfully() throws Exception {

        when(userService.verifyEmail(any(VerifyEmailRequest.class)))
            .thenReturn(new MessageResponse("Correo verificado correctamente"));

        String json = """
            {
                "email": "daniel@tstyle.com",
                "code": "123456"
            }
            """;

        mockMvc.perform(post("/api/users/verify-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message")
                .value("Correo verificado correctamente"));
    }

    /**
     * Comprueba que se rechace un código con formato inválido.
     */
    @Test
    void testVerifyEmailWithInvalidCode() throws Exception {

        String json = """
            {
                "email": "daniel@tstyle.com",
                "code": "123"
            }
            """;

        mockMvc.perform(post("/api/users/verify-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    /**
     * Comprueba el reenvío exitoso del código.
     */
    @Test
    void testResendVerificationSuccessfully() throws Exception {

        when(userService.resendVerificationCode(
                any(ResendVerificationRequest.class)))
            .thenReturn(new MessageResponse(
                "Se envió un nuevo código de verificación"
            ));

        String json = """
            {
                "email": "daniel@tstyle.com"
            }
            """;

        mockMvc.perform(post("/api/users/resend-verification")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message")
                .value("Se envió un nuevo código de verificación"));
    }

    /**
     * Comprueba que el reenvío rechace un correo inválido.
     */
    @Test
    void testResendVerificationWithInvalidEmail() throws Exception {

        String json = """
            {
                "email": "correo-invalido"
            }
            """;

        mockMvc.perform(post("/api/users/resend-verification")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }
}
