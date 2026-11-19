package com.cartpilot.order.application;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderQuoteItemResult(
		UUID productId,
		String name,
		int quantity,
		BigDecimal unitPriceAmount,
		BigDecimal lineTotalAmount,
		String currency,
		boolean available) {
}
