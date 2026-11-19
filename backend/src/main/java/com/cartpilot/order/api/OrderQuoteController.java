package com.cartpilot.order.api;

import com.cartpilot.order.application.OrderQuoteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/order-quotes")
@Tag(name = "Order Quote", description = "현재 장바구니 기준 주문 가능 여부와 금액 계산")
@RequiredArgsConstructor
public class OrderQuoteController {

	private final OrderQuoteService orderQuoteService;

	@PostMapping
	@Operation(
			summary = "주문 견적 생성",
			description = "현재 상품 가격과 재고를 다시 확인해 견적을 계산합니다. 주문을 생성하거나 재고를 예약하지 않습니다.")
	@ApiResponse(
			responseCode = "200",
			description = "주문 견적 계산 성공",
			content = @Content(schema = @Schema(implementation = OrderQuoteResponse.class)))
	public OrderQuoteResponse createQuote() {
		return OrderQuoteResponse.from(orderQuoteService.createForCurrentUser());
	}
}
