
package co.edu.unbosque.tstyle.user.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import co.edu.unbosque.tstyle.user.enums.Role;

/**
 * Pruebas unitarias para la entidad User.
 *
 * @author Equipo T Style
 */
class UserTest {

    /**
     * Comprueba los valores iniciales del constructor vacío.
     */
    @Test
    void testCreateUserWithEmptyConstructor() {

        User user = new User();

        assertFalse(user.isEmailVerified());
        assertTrue(user.isEnabled());
    }

    /**
     * Comprueba la creación de un usuario mediante el constructor principal.
     */
    @Test
    void testCreateUserWithMainConstructor() {

        User user = new User(
            "Daniel",
            "daniel@tstyle.com",
            "hashedPassword",
            Role.CLIENT
        );

        assertEquals("Daniel", user.getName());
        assertEquals("daniel@tstyle.com", user.getEmail());
        assertEquals("hashedPassword", user.getPassword());
        assertEquals(Role.CLIENT, user.getRole());
        assertFalse(user.isEmailVerified());
        assertTrue(user.isEnabled());
    }

    /**
     * Comprueba la asignación y consulta de los atributos del usuario.
     */
    @Test
    void testSettersAndGetters() {

        User user = new User();

        LocalDateTime creationDate = LocalDateTime.now();

        user.setId(1L);
        user.setName("Carlos");
        user.setEmail("carlos@tstyle.com");
        user.setPassword("anotherHashedPassword");
        user.setRole(Role.ADMIN);
        user.setEmailVerified(true);
        user.setEnabled(false);
        user.setCreatedAt(creationDate);

        assertEquals(1L, user.getId());
        assertEquals("Carlos", user.getName());
        assertEquals("carlos@tstyle.com", user.getEmail());
        assertEquals("anotherHashedPassword", user.getPassword());
        assertEquals(Role.ADMIN, user.getRole());
        assertTrue(user.isEmailVerified());
        assertFalse(user.isEnabled());
        assertEquals(creationDate, user.getCreatedAt());
    }

    /**
     * Comprueba que se establezca la fecha de creación antes de persistir.
     */
    @Test
    void testOnCreateSetsCreationDate() {

        User user = new User();

        user.onCreate();

        assertNotNull(user.getCreatedAt());
    }

    /**
     * Comprueba que la fecha existente no sea reemplazada.
     */
    @Test
    void testOnCreateKeepsExistingCreationDate() {

        User user = new User();

        LocalDateTime creationDate = LocalDateTime.of(2026, 11, 10, 10, 0);

        user.setCreatedAt(creationDate);
        user.onCreate();

        assertEquals(creationDate, user.getCreatedAt());
    }
}
