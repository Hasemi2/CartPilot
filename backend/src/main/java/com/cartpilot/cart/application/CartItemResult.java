package com.cartpilot.cart.application;

import java.math.BigDecimal;
import java.util.UUID;

public record CartItemResult(
		UUID productId,
		String name,
		int quantity,
		BigDecimal unitPriceAmount,
		BigDecimal lineTotalAmount,
		String currency) {
}
