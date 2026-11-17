package com.cartpilot.cart.application;

import java.util.List;

public record CartResult(
		int totalQuantity,
		List<CartItemResult> items) {

	public static CartResult empty() {
		return new CartResult(0, List.of());
	}
}
