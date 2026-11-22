
package co.edu.unbosque.tstyle.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

<<<<<<< HEAD:src/main/java/co/edu/unbosque/tstyle/repository/UserRepository.java
import co.edu.unbosque.tstyle.model.User;
=======
import co.edu.unbosque.tstyle.user.enums.Role;
import co.edu.unbosque.tstyle.user.model.User;
>>>>>>> b3d96ed4654d7773c3d07c2555796c9f2776c929:backend/src/main/java/co/edu/unbosque/tstyle/user/repository/UserRepository.java

/**
 * Proporciona las operaciones de acceso a datos para los usuarios de T Style.
 *
 * Spring Data JPA genera automáticamente la implementación de esta interfaz.
 *
 * @author Equipo T Style
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	/**
	 * Busca un usuario mediante su correo electrónico.
	 *
	 * @param email correo electrónico del usuario
	 * @return usuario encontrado, o un Optional vacío si no existe
	 */
	Optional<User> findByEmail(String email);

	/**
	 * Comprueba si un correo electrónico ya está registrado.
	 *
	 * @param email correo electrónico que se desea comprobar
	 * @return true si el correo ya existe, false en caso contrario
	 */
	boolean existsByEmail(String email);

	/**
	 * Comprueba si existe un usuario con un rol determinado.
	 *
	 * @param role rol que se desea buscar
	 * @return true si existe un usuario con ese rol
	 */
	boolean existsByRole(Role role);
}
