package com.cartpilot.cart.api;

import java.math.BigDecimal;
import java.util.UUID;

import com.cartpilot.cart.application.CartItemResult;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "장바구니 상품")
public record CartItemResponse(
		@Schema(description = "상품 식별자") UUID productId,
		@Schema(description = "현재 상품명", example = "저소음 무선 키보드") String name,
		@Schema(description = "장바구니 수량", example = "2") int quantity,
		@Schema(description = "현재 단가", example = "79000.00") BigDecimal unitPriceAmount,
		@Schema(description = "현재 단가 기준 항목 합계", example = "158000.00") BigDecimal lineTotalAmount,
		@Schema(description = "통화 코드", example = "KRW") String currency) {

	static CartItemResponse from(CartItemResult item) {
		return new CartItemResponse(
				item.productId(),
				item.name(),
				item.quantity(),
				item.unitPriceAmount(),
				item.lineTotalAmount(),
				item.currency());
	}
}
