package com.cartpilot.inventory.api;

import java.util.UUID;

import com.cartpilot.inventory.application.InventoryAvailabilityResult;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품 재고 가용성")
public record InventoryAvailabilityResponse(
		@Schema(description = "상품 식별자", example = "10000000-0000-0000-0000-000000000001") UUID productId,
		@Schema(description = "확인한 구매 수량", example = "2", minimum = "1", maximum = "100") int requestedQuantity,
		@Schema(description = "요청 수량 구매 가능 여부", example = "true") boolean available) {

	static InventoryAvailabilityResponse from(InventoryAvailabilityResult result) {
		return new InventoryAvailabilityResponse(
				result.productId(),
				result.requestedQuantity(),
				result.available());
	}
}
