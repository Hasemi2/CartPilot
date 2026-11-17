package com.cartpilot.catalog.infrastructure;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.cartpilot.catalog.domain.Product;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, UUID> {

	Optional<Product> findByIdAndPublishedTrue(UUID id);

	@Query("""
			select p
			from Product p
			where p.published = true
			  and (
			    :query = ''
			    or lower(p.name) like lower(concat('%', :query, '%'))
			    or lower(p.description) like lower(concat('%', :query, '%'))
			  )
			  and (:maxPrice is null or p.priceAmount <= :maxPrice)
			order by p.name asc, p.id asc
			""")
	List<Product> searchPublished(
			@Param("query") String query,
			@Param("maxPrice") BigDecimal maxPrice,
			Pageable pageable);
}
