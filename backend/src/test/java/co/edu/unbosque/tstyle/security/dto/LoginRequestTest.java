package co.edu.unbosque.tstyle.security.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias para LoginRequest.
 *
 * @author Equipo T Style
 */
class LoginRequestTest {

    @Test
    void testCreateLoginRequestWithEmptyConstructor() {

        LoginRequest request = new LoginRequest();

        assertNull(request.getEmail());
        assertNull(request.getPassword());
    }

    @Test
    void testCreateLoginRequestWithAllArguments() {

        LoginRequest request = new LoginRequest(
                "daniel@example.com",
                "Password123"
        );

        assertEquals("daniel@example.com", request.getEmail());
        assertEquals("Password123", request.getPassword());
    }

    @Test
    void testLoginRequestSetters() {

        LoginRequest request = new LoginRequest();

        request.setEmail("daniel@example.com");
        request.setPassword("Password123");

        assertEquals("daniel@example.com", request.getEmail());
        assertEquals("Password123", request.getPassword());
    }
}