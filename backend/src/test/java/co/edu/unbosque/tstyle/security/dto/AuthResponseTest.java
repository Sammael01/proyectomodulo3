package co.edu.unbosque.tstyle.security.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias para AuthResponse.
 *
 * @author Equipo T Style
 */
class AuthResponseTest {

    @Test
    void testCreateAuthResponseWithEmptyConstructor() {

        AuthResponse response = new AuthResponse();

        assertNull(response.getToken());
        assertNull(response.getUserId());
        assertNull(response.getName());
        assertNull(response.getEmail());
        assertNull(response.getRole());
    }

    @Test
    void testCreateAuthResponseWithAllArguments() {

        AuthResponse response = new AuthResponse(
                "token-example",
                1L,
                "Daniel Zapata",
                "daniel@example.com",
                "CLIENT"
        );

        assertEquals("token-example", response.getToken());
        assertEquals(1L, response.getUserId());
        assertEquals("Daniel Zapata", response.getName());
        assertEquals("daniel@example.com", response.getEmail());
        assertEquals("CLIENT", response.getRole());
    }

    @Test
    void testAuthResponseSetters() {

        AuthResponse response = new AuthResponse();

        response.setToken("new-token");
        response.setUserId(2L);
        response.setName("Daniel");
        response.setEmail("daniel@example.com");
        response.setRole("ADMIN");

        assertEquals("new-token", response.getToken());
        assertEquals(2L, response.getUserId());
        assertEquals("Daniel", response.getName());
        assertEquals("daniel@example.com", response.getEmail());
        assertEquals("ADMIN", response.getRole());
    }
}