
package co.edu.unbosque.tstyle.security.service;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import co.edu.unbosque.tstyle.user.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Servicio encargado de generar y validar tokens JWT.
 *
 * Permite generar tokens de autenticación y comprobar que pertenezcan al
 * usuario correspondiente y que su versión coincida con la versión actual
 * almacenada en la base de datos.
 *
 * @author Equipo T Style
 */
@Service
public class JwtService {

	private final SecretKey signingKey;
	private final long expiration;

	/**
	 * Constructor del servicio JWT.
	 *
	 * @param secret     clave secreta codificada en Base64
	 * @param expiration tiempo de expiración en milisegundos
	 */
	public JwtService(@Value("${JWT_SECRET}") String secret, @Value("${jwt.expiration:86400000}") long expiration) {

		this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
		this.expiration = expiration;
	}

	/**
	 * Genera un token para un usuario autenticado.
	 *
	 * Incluye la versión de autenticación del usuario para permitir invalidar
	 * tokens anteriores cuando cambia su contraseña.
	 *
	 * @param user usuario autenticado
	 * @return token JWT firmado
	 */
	public String generateToken(User user) {

		Date issuedAt = new Date();
		Date expiresAt = new Date(issuedAt.getTime() + expiration);

		return Jwts.builder().subject(user.getEmail()).claim("userId", user.getId())
				.claim("role", user.getRole().name()).claim("tokenVersion", user.getTokenVersion()).issuedAt(issuedAt)
				.expiration(expiresAt).signWith(signingKey).compact();
	}

	/**
	 * Obtiene el correo electrónico almacenado en el token.
	 *
	 * @param token token JWT
	 * @return correo electrónico del usuario
	 */
	public String extractEmail(String token) {

		return extractClaims(token).getSubject();
	}

	/**
	 * Comprueba si el token pertenece al usuario y conserva la versión de
	 * autenticación vigente.
	 *
	 * @param token token JWT
	 * @param user  usuario que se desea validar
	 * @return true si el token es válido y su versión coincide
	 */
	public boolean isTokenValid(String token, User user) {

		try {
			Claims claims = extractClaims(token);

			String email = claims.getSubject();
			Object tokenVersionClaim = claims.get("tokenVersion");

			if (!(tokenVersionClaim instanceof Number)) {
				return false;
			}

			int tokenVersion = ((Number) tokenVersionClaim).intValue();

			return email.equals(user.getEmail()) && tokenVersion == user.getTokenVersion();

		} catch (JwtException | IllegalArgumentException exception) {
			return false;
		}
	}

	/**
	 * Obtiene la información contenida en un token firmado.
	 *
	 * @param token token JWT
	 * @return información del token
	 */
	private Claims extractClaims(String token) {

		return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
	}
}
