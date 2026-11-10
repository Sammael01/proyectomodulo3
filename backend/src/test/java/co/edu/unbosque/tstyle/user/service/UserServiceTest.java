
package co.edu.unbosque.tstyle.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import co.edu.unbosque.tstyle.exception.DuplicateResourceException;
import co.edu.unbosque.tstyle.security.dto.MessageResponse;
import co.edu.unbosque.tstyle.security.dto.RegisterRequest;
import co.edu.unbosque.tstyle.user.enums.Role;
import co.edu.unbosque.tstyle.user.model.User;
import co.edu.unbosque.tstyle.user.repository.UserRepository;

/**
 * Pruebas unitarias para UserService.
 *
 * @author Equipo T Style
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private RegisterRequest registerRequest;

    /**
     * Prepara los datos necesarios para cada prueba.
     */
    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest(
            "Daniel Zapata",
            "daniel@tstyle.com",
            "Password123!"
        );
    }

    /**
     * Comprueba que un usuario pueda registrarse correctamente.
     */
    @Test
    void testRegisterUserSuccessfully() {

        when(userRepository.existsByEmail("daniel@tstyle.com"))
            .thenReturn(false);

        when(passwordEncoder.encode("Password123!"))
            .thenReturn("$2a$10$hashedPasswordExample");

        MessageResponse response = userService.registerUser(registerRequest);

        assertEquals("Usuario registrado correctamente", response.getMessage());

        verify(userRepository).save(any(User.class));
    }

    /**
     * Comprueba que la contraseña se almacene mediante un hash.
     */
    @Test
    void testRegisterUserEncryptsPassword() {

        when(userRepository.existsByEmail("daniel@tstyle.com"))
            .thenReturn(false);

        when(passwordEncoder.encode("Password123!"))
            .thenReturn("$2a$10$hashedPasswordExample");

        userService.registerUser(registerRequest);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertNotEquals("Password123!", savedUser.getPassword());
        assertEquals("$2a$10$hashedPasswordExample", savedUser.getPassword());
    }

    /**
     * Comprueba que el registro asigne automáticamente el rol CLIENT.
     */
    @Test
    void testRegisterUserAssignsClientRole() {

        when(userRepository.existsByEmail("daniel@tstyle.com"))
            .thenReturn(false);

        when(passwordEncoder.encode("Password123!"))
            .thenReturn("$2a$10$hashedPasswordExample");

        userService.registerUser(registerRequest);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());

        assertEquals(Role.CLIENT, userCaptor.getValue().getRole());
    }

    /**
     * Comprueba que se normalice el correo electrónico.
     */
    @Test
    void testRegisterUserNormalizesEmail() {

        registerRequest.setEmail("  DANIEL@TSTYLE.COM  ");

        when(userRepository.existsByEmail("daniel@tstyle.com"))
            .thenReturn(false);

        when(passwordEncoder.encode("Password123!"))
            .thenReturn("$2a$10$hashedPasswordExample");

        userService.registerUser(registerRequest);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());

        assertEquals("daniel@tstyle.com", userCaptor.getValue().getEmail());
    }

    /**
     * Comprueba que no se permita registrar un correo duplicado.
     */
    @Test
    void testRegisterUserWithExistingEmail() {

        when(userRepository.existsByEmail("daniel@tstyle.com"))
            .thenReturn(true);

        assertThrows(
            DuplicateResourceException.class,
            () -> userService.registerUser(registerRequest)
        );

        verify(userRepository, never()).save(any(User.class));
    }
}
