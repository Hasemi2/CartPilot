package com.cartpilot.order.application;

import java.math.BigDecimal;
import java.util.List;

public record OrderQuoteResult(
		int totalQuantity,
		BigDecimal totalAmount,
		String currency,
		boolean orderable,
		List<OrderQuoteItemResult> items) {

	public static OrderQuoteResult empty() {
		return new OrderQuoteResult(0, new BigDecimal("0.00"), null, false, List.of());
	}
}
