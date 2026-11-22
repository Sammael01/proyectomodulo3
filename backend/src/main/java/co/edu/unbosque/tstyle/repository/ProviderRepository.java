package co.edu.unbosque.tstyle.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unbosque.tstyle.model.Provider;

/**
 * 
 * Acceso a datos de los proveedores 
 * 
 * @author Sebastián
 */
public interface ProviderRepository extends JpaRepository<Provider, Long>{
	
	Optional<Provider> findByName(String name);
}
