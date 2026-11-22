
package co.edu.unbosque.tstyle.security.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Base64;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.edu.unbosque.tstyle.user.enums.Role;
import co.edu.unbosque.tstyle.user.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Pruebas unitarias para el servicio de generación y validación JWT.
 *
 * @author Equipo T Style
 */
class JwtServiceTest {

	private static final String SECRET = Base64.getEncoder().encodeToString(new byte[64]);

	private JwtService jwtService;
	private User user;

	@BeforeEach
	void setUp() {

		jwtService = new JwtService(SECRET, 60000);

		user = new User("Daniel Zapata", "daniel@tstyle.com", "hashed-password", Role.CLIENT);

		user.setId(1L);
		user.setEmailVerified(true);
		user.setEnabled(true);
		user.setTokenVersion(0);
	}

	/**
	 * Comprueba que se genere un token JWT.
	 */
	@Test
	void testGenerateTokenSuccessfully() {

		String token = jwtService.generateToken(user);

		assertNotNull(token);
		assertFalse(token.isBlank());
	}

	/**
	 * Comprueba que se pueda extraer el correo del token.
	 */
	@Test
	void testExtractEmailSuccessfully() {

		String token = jwtService.generateToken(user);

		String email = jwtService.extractEmail(token);

		assertEquals("daniel@tstyle.com", email);
	}

	/**
	 * Comprueba que un token vigente sea válido para su usuario.
	 */
	@Test
	void testTokenIsValid() {

		String token = jwtService.generateToken(user);

		assertTrue(jwtService.isTokenValid(token, user));
	}

	/**
	 * Comprueba que se rechace un token manipulado.
	 */
	@Test
	void testTokenWithInvalidSignature() {

		String token = jwtService.generateToken(user);
		String manipulatedToken = token.substring(0, token.length() - 2) + "ab";

		assertFalse(jwtService.isTokenValid(manipulatedToken, user));
	}

	/**
	 * Comprueba que un token de otro usuario sea rechazado.
	 */
	@Test
	void testTokenWithDifferentEmail() {

		String token = jwtService.generateToken(user);

		User anotherUser = new User("Otro Usuario", "otro@tstyle.com", "hashed-password", Role.CLIENT);

		anotherUser.setId(2L);
		anotherUser.setEmailVerified(true);
		anotherUser.setEnabled(true);

		assertFalse(jwtService.isTokenValid(token, anotherUser));
	}

	/**
	 * Comprueba que un token anterior sea inválido al cambiar la versión de
	 * autenticación del usuario.
	 */
	@Test
	void testTokenWithDifferentVersion() {

		String token = jwtService.generateToken(user);

		user.setTokenVersion(1);

		assertFalse(jwtService.isTokenValid(token, user));
	}

	/**
	 * Comprueba que un token vencido sea rechazado.
	 */
	@Test
	void testExpiredTokenIsInvalid() throws InterruptedException {

		JwtService shortExpirationService = new JwtService(SECRET, 1);

		String token = shortExpirationService.generateToken(user);

		Thread.sleep(1100);

		assertFalse(shortExpirationService.isTokenValid(token, user));
	}

	/**
	 * Comprueba que un token firmado con otra clave sea rechazado.
	 */
	@Test
	void testTokenWithDifferentSecret() {

		String token = jwtService.generateToken(user);

		byte[] differentKeyBytes = new byte[64];
		Arrays.fill(differentKeyBytes, (byte) 1);

		SecretKey anotherKey = Keys.hmacShaKeyFor(differentKeyBytes);

		String anotherSecretToken = Jwts.builder().subject("daniel@tstyle.com").claim("tokenVersion", 0)
				.signWith(anotherKey).compact();

		assertFalse(jwtService.isTokenValid(anotherSecretToken, user));
		assertTrue(jwtService.isTokenValid(token, user));
	}
}
