package com.cartpilot.cart.infrastructure;

import java.util.Optional;
import java.util.UUID;

import com.cartpilot.cart.domain.ShoppingCart;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, UUID> {

	Optional<ShoppingCart> findByUserId(UUID userId);
}
