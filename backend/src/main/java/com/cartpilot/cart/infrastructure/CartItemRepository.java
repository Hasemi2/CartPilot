package com.cartpilot.cart.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.cartpilot.cart.domain.CartItem;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

	List<CartItem> findAllByCartIdOrderByCreatedAtAsc(UUID cartId);

	Optional<CartItem> findByCartIdAndProductId(UUID cartId, UUID productId);

	long deleteByCartIdAndProductId(UUID cartId, UUID productId);
}
