package co.edu.unbosque.tstyle.security.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias para MessageResponse.
 *
 * @author Equipo T Style
 */
class MessageResponseTest {

    @Test
    void testCreateMessageResponseWithEmptyConstructor() {

        MessageResponse response = new MessageResponse();

        assertNull(response.getMessage());
    }

    @Test
    void testCreateMessageResponseWithMessage() {

        MessageResponse response = new MessageResponse(
                "Usuario registrado correctamente"
        );

        assertEquals(
                "Usuario registrado correctamente",
                response.getMessage()
        );
    }

    @Test
    void testMessageResponseSetter() {

        MessageResponse response = new MessageResponse();

        response.setMessage("Correo enviado correctamente");

        assertEquals(
                "Correo enviado correctamente",
                response.getMessage()
        );
    }
}