
package co.edu.unbosque.tstyle.config;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Base64;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.unbosque.tstyle.security.service.AuthService;
import co.edu.unbosque.tstyle.security.service.JwtService;
import co.edu.unbosque.tstyle.user.enums.Role;
import co.edu.unbosque.tstyle.user.model.User;
import co.edu.unbosque.tstyle.user.repository.UserRepository;
import co.edu.unbosque.tstyle.user.service.UserService;

/**
 * Pruebas de configuración de seguridad para la aplicación T Style.
 *
 * Comprueba el acceso a las rutas públicas, privadas y administrativas según la
 * autenticación y los roles de los usuarios mediante tokens JWT.
 *
 * @author Equipo T Style
 */
@WebMvcTest
@AutoConfigureMockMvc
@Import({ SecurityConfig.class, SecurityConfigTest.TestConfig.class })
class SecurityConfigTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private UserRepository userRepository;

	/**
	 * Comprueba que las rutas privadas rechacen solicitudes sin autenticación.
	 */
	@Test
	void testPrivateRouteRequiresAuthentication() throws Exception {
		mockMvc.perform(get("/api/private/test")).andExpect(status().isUnauthorized());
	}

	/**
	 * Comprueba que las rutas administrativas rechacen solicitudes sin
	 * autenticación.
	 */
	@Test
	void testAdminRouteRequiresAuthentication() throws Exception {
		mockMvc.perform(get("/api/admin/test")).andExpect(status().isUnauthorized());
	}

	/**
	 * Comprueba que un usuario CLIENT autenticado no pueda acceder a rutas
	 * administrativas.
	 */
	@Test
	void testClientCannotAccessAdminRoute() throws Exception {
		User user = createUser("client@tstyle.test", Role.CLIENT);

		when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

		String token = jwtService.generateToken(user);

		mockMvc.perform(get("/api/admin/test").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isForbidden());
	}

	/**
	 * Comprueba que un usuario ADMIN autenticado pueda superar la autorización
	 * administrativa.
	 */
	@Test
	void testAdminCanAccessAdminRoute() throws Exception {
		User user = createUser("admin@tstyle.test", Role.ADMIN);

		when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

		String token = jwtService.generateToken(user);

		mockMvc.perform(get("/api/admin/test").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isNotFound());
	}

	/**
	 * Comprueba que el endpoint de inicio de sesión sea público.
	 */
	@Test
	void testLoginEndpointIsPublic() throws Exception {
		mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isBadRequest());
	}

	/**
	 * Comprueba que el endpoint de registro sea público.
	 */
	@Test
	void testRegisterEndpointIsPublic() throws Exception {
		mockMvc.perform(post("/api/users/register").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isBadRequest());
	}

	/**
	 * Comprueba que el endpoint de recuperación de contraseña sea público.
	 */
	@Test
	void testPasswordRecoveryEndpointIsPublic() throws Exception {
		mockMvc.perform(post("/api/auth/forgot-password").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isBadRequest());
	}

	/**
	 * Crea un usuario habilitado y con correo verificado para las pruebas JWT.
	 *
	 * @param email correo electrónico del usuario
	 * @param role  rol asignado al usuario
	 * @return usuario preparado para generar un token JWT
	 */
	private User createUser(String email, Role role) {
		User user = new User();
		user.setId(1L);
		user.setEmail(email);
		user.setRole(role);
		user.setEnabled(true);
		user.setEmailVerified(true);
		user.setTokenVersion(0);

		return user;
	}

	/**
	 * Configuración auxiliar de dependencias para las pruebas.
	 */
	@TestConfiguration
	@EnableWebSecurity
	static class TestConfig {

		@Bean
		JwtService jwtService() {
			return new JwtService(Base64.getEncoder().encodeToString(new byte[64]), 60000);
		}

		@Bean
		UserRepository userRepository() {
			return org.mockito.Mockito.mock(UserRepository.class);
		}

		@Bean
		AuthService authService() {
			return org.mockito.Mockito.mock(AuthService.class);
		}

		@Bean
		UserService userService() {
			return org.mockito.Mockito.mock(UserService.class);
		}
	}
}
