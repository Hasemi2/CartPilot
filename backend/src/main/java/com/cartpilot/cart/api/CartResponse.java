package com.cartpilot.cart.api;

import java.util.List;

import com.cartpilot.cart.application.CartResult;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "현재 사용자의 장바구니")
public record CartResponse(
		@Schema(description = "장바구니 전체 상품 수량", example = "2") int totalQuantity,
		@Schema(description = "장바구니 상품 목록") List<CartItemResponse> items) {

	static CartResponse from(CartResult cart) {
		return new CartResponse(
				cart.totalQuantity(),
				cart.items().stream().map(CartItemResponse::from).toList());
	}
}
