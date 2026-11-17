package com.cartpilot.inventory.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inventory_stocks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventoryStock {

	@Id
	@Column(name = "product_id")
	private UUID productId;

	@Column(name = "on_hand_quantity", nullable = false)
	private int onHandQuantity;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	public boolean canFulfill(int requestedQuantity) {
		return onHandQuantity >= requestedQuantity;
	}
}
