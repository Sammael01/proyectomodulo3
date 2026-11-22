package co.edu.unbosque.tstyle.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unbosque.tstyle.model.Category;

/**
 *
 * Acceso a datos de las categorias
 *
 * @author Sebastián
 */
public interface CategoryRepository extends JpaRepository<Category, Long>{

	Optional<Category> findByName(String name);
}
