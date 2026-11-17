package com.cartpilot.cart.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(description = "장바구니 상품 수량 설정 요청")
public record SetCartItemQuantityRequest(
		@Schema(description = "설정할 수량", example = "2", minimum = "1", maximum = "100")
		@Min(1) @Max(100) int quantity) {
}
