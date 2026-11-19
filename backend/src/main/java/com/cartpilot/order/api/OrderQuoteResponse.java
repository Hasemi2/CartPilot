package com.cartpilot.order.api;

import java.math.BigDecimal;
import java.util.List;

import com.cartpilot.order.application.OrderQuoteResult;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "현재 장바구니 기준 주문 견적")
public record OrderQuoteResponse(
		@Schema(description = "전체 상품 수량", example = "2") int totalQuantity,
		@Schema(description = "현재 가격 기준 총액", example = "158000.00") BigDecimal totalAmount,
		@Schema(description = "통화 코드. 빈 장바구니이면 null", example = "KRW", nullable = true) String currency,
		@Schema(description = "모든 상품을 현재 재고로 주문 가능한지 여부", example = "true") boolean orderable,
		@Schema(description = "견적 상품 목록") List<OrderQuoteItemResponse> items) {

	static OrderQuoteResponse from(OrderQuoteResult quote) {
		return new OrderQuoteResponse(
				quote.totalQuantity(),
				quote.totalAmount(),
				quote.currency(),
				quote.orderable(),
				quote.items().stream().map(OrderQuoteItemResponse::from).toList());
	}
}
