package co.edu.unbosque.tstyle.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.tstyle.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
	Optional<Product> findByProviderIdAndExternalId(Long providerId, String externalId);

	@EntityGraph(attributePaths = { "provider", "category" })
	@Query("""
			SELECT p FROM Product p
			WHERE p.provider.active = true
			  AND p.active = true
			  AND (:categoryId IS NULL OR p.category.id = :categoryId)
			  AND (:providerId IS NULL OR p.provider.id = :providerId)
			  AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))
			""")
	Page<Product> search(@Param("categoryId") Long categoryId,
			@Param("providerId") Long providerId,
			@Param("search") String search,
			Pageable pageable);

	@Modifying
	@Transactional
	@Query("update Product p set p.stock = p.stock - :quantity "
			+ "where p.id = :id and p.active = true and p.stock >= :quantity")
	int decreaseStock(@Param("id") Long id, @Param("quantity") int quantity);

	@Modifying
	@Transactional
	@Query("update Product p set p.stock = p.stock + :quantity where p.id = :id")
	int increaseStock(@Param("id") Long id, @Param("quantity") int quantity);
}
