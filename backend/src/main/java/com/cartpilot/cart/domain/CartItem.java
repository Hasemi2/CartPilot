package com.cartpilot.cart.domain;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cart_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CartItem {

	@Id
	private UUID id;

	@Column(name = "cart_id", nullable = false)
	private UUID cartId;

	@Column(name = "product_id", nullable = false)
	private UUID productId;

	@Column(nullable = false)
	private int quantity;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	public static CartItem create(UUID cartId, UUID productId, int quantity) {
		OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
		return new CartItem(UUID.randomUUID(), cartId, productId, quantity, now, now);
	}

	public void changeQuantity(int quantity) {
		this.quantity = quantity;
		updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
	}
}
