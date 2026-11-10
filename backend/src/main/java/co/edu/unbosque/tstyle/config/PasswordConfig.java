
package co.edu.unbosque.tstyle.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configura el codificador de contraseñas de T Style.
 *
 * @author Equipo T Style
 */
@Configuration
public class PasswordConfig {

	/**
	 * Crea el componente encargado de codificar y verificar contraseñas.
	 *
	 * @return codificador de contraseñas BCrypt
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
		