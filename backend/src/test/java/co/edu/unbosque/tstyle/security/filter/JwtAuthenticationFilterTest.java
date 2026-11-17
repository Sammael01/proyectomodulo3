
package co.edu.unbosque.tstyle.security.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Base64;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.mock.web.MockFilterChain;

import co.edu.unbosque.tstyle.security.service.JwtService;
import co.edu.unbosque.tstyle.user.enums.Role;
import co.edu.unbosque.tstyle.user.model.User;
import co.edu.unbosque.tstyle.user.repository.UserRepository;

/**
 * Pruebas unitarias para el filtro de autenticación JWT.
 *
 * @author Equipo T Style
 */
class JwtAuthenticationFilterTest {

	private static final String SECRET = Base64.getEncoder().encodeToString(new byte[64]);

	private JwtService jwtService;
	private UserRepository userRepository;
	private JwtAuthenticationFilter filter;
	private User user;

	@BeforeEach
	void setUp() {

		jwtService = new JwtService(SECRET, 60000);
		userRepository = mock(UserRepository.class);

		filter = new JwtAuthenticationFilter(jwtService, userRepository);

		user = new User("Daniel Zapata", "daniel@tstyle.com", "hashed-password", Role.CLIENT);

		user.setId(1L);
		user.setEmailVerified(true);
		user.setEnabled(true);
		user.setTokenVersion(0);
	}

	@AfterEach
	void cleanSecurityContext() {

		SecurityContextHolder.clearContext();
	}

	/**
	 * Comprueba que una solicitud sin token continúe por el filtro.
	 */
	@Test
	void testRequestWithoutTokenContinues() throws Exception {

		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		filter.doFilter(request, response, chain);

		assertNotNull(chain.getRequest());
		assertNull(SecurityContextHolder.getContext().getAuthentication());

		verifyNoInteractions(userRepository);
	}

	/**
	 * Comprueba que una solicitud con un token válido sea autenticada.
	 */
	@Test
	void testValidTokenAuthenticatesUser() throws Exception {

		String token = jwtService.generateToken(user);

		when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);

		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		filter.doFilter(request, response, chain);

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		assertNotNull(authentication);
		assertEquals(user.getEmail(), authentication.getPrincipal());
		assertTrue(authentication.getAuthorities().stream()
				.anyMatch(authority -> authority.getAuthority().equals("ROLE_CLIENT")));

		assertNotNull(chain.getRequest());
		assertEquals(200, response.getStatus());

		verify(userRepository).findByEmail(user.getEmail());
	}

	/**
	 * Comprueba que un token inválido sea rechazado.
	 */
	@Test
	void testInvalidTokenIsRejected() throws Exception {

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token-invalido");

		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		filter.doFilter(request, response, chain);

		assertEquals(401, response.getStatus());
		assertTrue(response.getContentAsString().contains("UNAUTHORIZED"));
		assertNull(chain.getRequest());
		assertNull(SecurityContextHolder.getContext().getAuthentication());
	}

	/**
	 * Comprueba que un usuario inexistente sea rechazado.
	 */
	@Test
	void testUserNotFoundIsRejected() throws Exception {

		String token = jwtService.generateToken(user);

		when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);

		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		filter.doFilter(request, response, chain);

		assertEquals(401, response.getStatus());
		assertNull(chain.getRequest());
		assertNull(SecurityContextHolder.getContext().getAuthentication());
	}

	/**
	 * Comprueba que un usuario deshabilitado sea rechazado.
	 */
	@Test
	void testDisabledUserIsRejected() throws Exception {

		user.setEnabled(false);

		String token = jwtService.generateToken(user);

		when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);

		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		filter.doFilter(request, response, chain);

		assertEquals(401, response.getStatus());
		assertNull(chain.getRequest());
		assertNull(SecurityContextHolder.getContext().getAuthentication());
	}

	/**
	 * Comprueba que un usuario sin correo verificado sea rechazado.
	 */
	@Test
	void testUnverifiedUserIsRejected() throws Exception {

		user.setEmailVerified(false);

		String token = jwtService.generateToken(user);

		when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);

		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		filter.doFilter(request, response, chain);

		assertEquals(401, response.getStatus());
		assertNull(chain.getRequest());
		assertNull(SecurityContextHolder.getContext().getAuthentication());
	}

	/**
	 * Comprueba que un token con una versión anterior sea rechazado.
	 */
	@Test
	void testTokenWithOldVersionIsRejected() throws Exception {

		String token = jwtService.generateToken(user);

		user.setTokenVersion(1);

		when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);

		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		filter.doFilter(request, response, chain);

		assertEquals(401, response.getStatus());
		assertNull(chain.getRequest());
		assertNull(SecurityContextHolder.getContext().getAuthentication());
	}

	/**
	 * Comprueba que el filtro asigne correctamente el rol de administrador.
	 */
	@Test
	void testAdminRoleIsAssigned() throws Exception {

		user.setRole(Role.ADMIN);

		String token = jwtService.generateToken(user);

		when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);

		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		filter.doFilter(request, response, chain);

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		assertNotNull(authentication);
		assertTrue(authentication.getAuthorities().stream()
				.anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")));

		assertNotNull(chain.getRequest());
		assertEquals(200, response.getStatus());
	}
}
