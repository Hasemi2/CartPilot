package com.cartpilot.inventory.application;

import java.util.UUID;

public record InventoryAvailabilityResult(
		UUID productId,
		int requestedQuantity,
		boolean available) {
}
