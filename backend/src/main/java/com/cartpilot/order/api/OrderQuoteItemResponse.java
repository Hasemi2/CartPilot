package com.cartpilot.order.api;

import java.math.BigDecimal;
import java.util.UUID;

import com.cartpilot.order.application.OrderQuoteItemResult;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주문 견적 상품")
public record OrderQuoteItemResponse(
		@Schema(description = "상품 식별자") UUID productId,
		@Schema(description = "현재 상품명", example = "저소음 무선 키보드") String name,
		@Schema(description = "주문 예정 수량", example = "2") int quantity,
		@Schema(description = "현재 단가", example = "79000.00") BigDecimal unitPriceAmount,
		@Schema(description = "상품별 합계", example = "158000.00") BigDecimal lineTotalAmount,
		@Schema(description = "통화 코드", example = "KRW") String currency,
		@Schema(description = "현재 재고로 주문 가능한지 여부", example = "true") boolean available) {

	static OrderQuoteItemResponse from(OrderQuoteItemResult item) {
		return new OrderQuoteItemResponse(
				item.productId(),
				item.name(),
				item.quantity(),
				item.unitPriceAmount(),
				item.lineTotalAmount(),
				item.currency(),
				item.available());
	}
}
