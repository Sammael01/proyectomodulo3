
package co.edu.unbosque.tstyle.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import co.edu.unbosque.tstyle.user.enums.Role;
import co.edu.unbosque.tstyle.user.model.User;
import co.edu.unbosque.tstyle.user.repository.UserRepository;

/**
 * Prueba el comportamiento de inicialización del administrador.
 *
 * @author Equipo T Style
 */
@ExtendWith(MockitoExtension.class)
class AdminBootstrapTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private AdminBootstrap adminBootstrap;

	@BeforeEach
	void setUp() {
		adminBootstrap = new AdminBootstrap(userRepository, passwordEncoder, "Administrador T Style",
				"ADMIN@TSTYLE.COM", "PasswordSegura123");
	}

	/**
	 * Comprueba que no se cree otro administrador si ya existe uno.
	 */
	@Test
	void shouldNotCreateAdminWhenOneAlreadyExists() {

		when(userRepository.existsByRole(Role.ADMIN)).thenReturn(true);

		adminBootstrap.run(null);

		verify(userRepository, never()).save(any(User.class));
	}

	/**
	 * Comprueba que se cree correctamente el administrador cuando no existe.
	 */
	@Test
	void shouldCreateAdminWhenNoneExists() {

		when(userRepository.existsByRole(Role.ADMIN)).thenReturn(false);
		when(userRepository.existsByEmail("admin@tstyle.com")).thenReturn(false);
		when(passwordEncoder.encode("PasswordSegura123")).thenReturn("password-hashed");

		adminBootstrap.run(null);

		ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

		verify(userRepository).save(captor.capture());

		User admin = captor.getValue();

		assertEquals("Administrador T Style", admin.getName());
		assertEquals("admin@tstyle.com", admin.getEmail());
		assertEquals("password-hashed", admin.getPassword());
		assertEquals(Role.ADMIN, admin.getRole());
		assertTrue(admin.isEmailVerified());
		assertTrue(admin.isEnabled());
	}

	/**
	 * Comprueba que no se cree el administrador si el correo ya está ocupado.
	 */
	@Test
	void shouldRejectAdminWhenEmailAlreadyExists() {

		when(userRepository.existsByRole(Role.ADMIN)).thenReturn(false);
		when(userRepository.existsByEmail("admin@tstyle.com")).thenReturn(true);

		assertThrows(IllegalStateException.class, () -> adminBootstrap.run(null));

		verify(userRepository, never()).save(any(User.class));
	}

	/**
	 * Comprueba que se rechace la creación si faltan las credenciales.
	 */
	@Test
	void shouldRejectCreationWhenCredentialsAreMissing() {

		AdminBootstrap bootstrapWithoutCredentials = new AdminBootstrap(userRepository, passwordEncoder, "", "", "");

		when(userRepository.existsByRole(Role.ADMIN)).thenReturn(false);

		assertThrows(IllegalStateException.class, () -> bootstrapWithoutCredentials.run(null));

		verify(userRepository, never()).save(any(User.class));
	}
}
