package co.edu.unbosque.tstyle.security.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias para RegisterRequest.
 *
 * @author Equipo T Style
 */
class RegisterRequestTest {

    @Test
    void testCreateRegisterRequestWithEmptyConstructor() {

        RegisterRequest request = new RegisterRequest();

        assertNull(request.getName());
        assertNull(request.getEmail());
        assertNull(request.getPassword());
    }

    @Test
    void testCreateRegisterRequestWithAllArguments() {

        RegisterRequest request = new RegisterRequest(
                "Daniel Zapata",
                "daniel@example.com",
                "Password123"
        );

        assertEquals("Daniel Zapata", request.getName());
        assertEquals("daniel@example.com", request.getEmail());
        assertEquals("Password123", request.getPassword());
    }

    @Test
    void testRegisterRequestSetters() {

        RegisterRequest request = new RegisterRequest();

        request.setName("Daniel");
        request.setEmail("daniel@example.com");
        request.setPassword("Password123");

        assertEquals("Daniel", request.getName());
        assertEquals("daniel@example.com", request.getEmail());
        assertEquals("Password123", request.getPassword());
    }
}