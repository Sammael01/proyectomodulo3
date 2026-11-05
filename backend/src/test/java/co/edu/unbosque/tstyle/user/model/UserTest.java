
package co.edu.unbosque.tstyle.user.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias para la clase User.
 *
 * @author Equipo T Style
 */
class UserTest {

    /**
     * Comprueba la creación de un usuario mediante el constructor vacío.
     */
    @Test
    void testCreateUserWithEmptyConstructor() {

        User user = new User();

        user.setId(1L);
        user.setName("Daniel");
        user.setEmail("daniel@tstyle.com");
        user.setPassword("hashedPassword");
        user.setRole("CLIENT");
        user.setEmailVerified(false);

        assertEquals(1L, user.getId());
        assertEquals("Daniel", user.getName());
        assertEquals("daniel@tstyle.com", user.getEmail());
        assertEquals("hashedPassword", user.getPassword());
        assertEquals("CLIENT", user.getRole());
        assertFalse(user.getEmailVerified());
    }

    /**
     * Comprueba la creación de un usuario mediante el constructor completo.
     */
    @Test
    void testCreateUserWithAllArgumentsConstructor() {

        User user = new User(
            2L,
            "Carlos",
            "carlos@tstyle.com",
            "anotherHashedPassword",
            "CLIENT",
            true
        );

        assertEquals(2L, user.getId());
        assertEquals("Carlos", user.getName());
        assertEquals("carlos@tstyle.com", user.getEmail());
        assertEquals("anotherHashedPassword", user.getPassword());
        assertEquals("CLIENT", user.getRole());
        assertEquals(true, user.getEmailVerified());
    }
}
