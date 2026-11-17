package com.cartpilot.inventory.api;

import java.util.UUID;

import com.cartpilot.inventory.application.InventoryAvailabilityService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products/{productId}/availability")
@Tag(name = "Inventory", description = "상품 재고 확인")
@RequiredArgsConstructor
public class InventoryController {

	private final InventoryAvailabilityService inventoryAvailabilityService;

	@GetMapping
	@Operation(summary = "상품 재고 조회", description = "게시된 상품을 요청 수량만큼 구매할 수 있는지 확인합니다.")
	@ApiResponses({
			@ApiResponse(
					responseCode = "200",
					description = "재고 확인 성공",
					content = @Content(schema = @Schema(implementation = InventoryAvailabilityResponse.class))),
			@ApiResponse(responseCode = "400", description = "상품 ID 또는 수량이 유효하지 않음", content = @Content),
			@ApiResponse(responseCode = "404", description = "게시된 상품을 찾을 수 없음", content = @Content)
	})
	public InventoryAvailabilityResponse checkAvailability(
			@Parameter(description = "상품 UUID", required = true)
			@PathVariable UUID productId,
			@Parameter(description = "확인할 구매 수량", example = "1")
			@RequestParam(defaultValue = "1") @Min(1) @Max(100) int quantity) {
		return InventoryAvailabilityResponse.from(inventoryAvailabilityService.check(productId, quantity));
	}
}
