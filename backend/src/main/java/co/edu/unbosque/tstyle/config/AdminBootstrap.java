
package co.edu.unbosque.tstyle.config;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.tstyle.user.enums.Role;
import co.edu.unbosque.tstyle.user.model.User;
import co.edu.unbosque.tstyle.user.repository.UserRepository;

/**
 * Comprueba y crea la cuenta administradora al iniciar T Style.
 *
 * La cuenta solamente se crea cuando no existe ningún usuario con rol ADMIN.
 * Las credenciales se reciben mediante configuración privada.
 *
 * @author Equipo T Style
 */
@Component
public class AdminBootstrap implements ApplicationRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(AdminBootstrap.class);

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final String adminName;
	private final String adminEmail;
	private final String adminPassword;

	/**
	 * Constructor del inicializador de la cuenta administradora.
	 *
	 * @param userRepository  repositorio de usuarios
	 * @param passwordEncoder codificador de contraseñas
	 * @param adminName       nombre inicial del administrador
	 * @param adminEmail      correo inicial del administrador
	 * @param adminPassword   contraseña inicial del administrador
	 */
	public AdminBootstrap(UserRepository userRepository, PasswordEncoder passwordEncoder,
			@Value("${ADMIN_BOOTSTRAP_NAME:}") String adminName, @Value("${ADMIN_BOOTSTRAP_EMAIL:}") String adminEmail,
			@Value("${ADMIN_BOOTSTRAP_PASSWORD:}") String adminPassword) {

		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.adminName = adminName;
		this.adminEmail = adminEmail;
		this.adminPassword = adminPassword;
	}

	/**
	 * Comprueba si existe un administrador y lo crea cuando sea necesario.
	 *
	 * @param args argumentos de inicio de la aplicación
	 */
	@Override
	@Transactional
	public void run(ApplicationArguments args) {

		if (userRepository.existsByRole(Role.ADMIN)) {
			LOGGER.info("Ya existe una cuenta administradora.");
			return;
		}

		if (adminName == null || adminName.isBlank() || adminEmail == null || adminEmail.isBlank()
				|| adminPassword == null || adminPassword.length() < 12) {

			throw new IllegalStateException(
					"No se puede crear el administrador. Configure " + "ADMIN_BOOTSTRAP_NAME, ADMIN_BOOTSTRAP_EMAIL "
							+ "y ADMIN_BOOTSTRAP_PASSWORD. " + "La contraseña debe tener al menos 12 caracteres.");
		}

		String email = adminEmail.trim().toLowerCase(Locale.ROOT);

		if (userRepository.existsByEmail(email)) {
			throw new IllegalStateException(
					"El correo configurado para ADMIN ya pertenece " + "a otro usuario. Utilice un correo diferente.");
		}

		User admin = new User(adminName.trim(), email, passwordEncoder.encode(adminPassword), Role.ADMIN);

		admin.setEmailVerified(true);
		admin.setEnabled(true);

		userRepository.save(admin);

		LOGGER.info("La cuenta administradora fue creada correctamente.");
	}
}
