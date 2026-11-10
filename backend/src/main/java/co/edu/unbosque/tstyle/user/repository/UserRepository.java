
package co.edu.unbosque.tstyle.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.tstyle.user.model.User;

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
}
