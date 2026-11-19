package com.cartpilot.order.application;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import com.cartpilot.cart.application.CartItemResult;
import com.cartpilot.cart.application.CartResult;
import com.cartpilot.cart.application.CartService;
import com.cartpilot.inventory.application.InventoryAvailabilityService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderQuoteService {

	private static final int MONEY_SCALE = 2;
	private static final RoundingMode MONEY_ROUNDING = RoundingMode.HALF_UP;

	private final CartService cartService;
	private final InventoryAvailabilityService inventoryAvailabilityService;

	@Transactional(readOnly = true)
	public OrderQuoteResult createForCurrentUser() {
		CartResult cart = cartService.getMyCart();
		if (cart.items().isEmpty()) {
			return OrderQuoteResult.empty();
		}

		String currency = cart.items().getFirst().currency();
		validateSingleCurrency(cart.items(), currency);

		List<OrderQuoteItemResult> items = cart.items().stream()
				.map(this::createItemQuote)
				.toList();
		BigDecimal totalAmount = items.stream()
				.map(OrderQuoteItemResult::lineTotalAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add)
				.setScale(MONEY_SCALE, MONEY_ROUNDING);
		boolean orderable = items.stream().allMatch(OrderQuoteItemResult::available);

		return new OrderQuoteResult(
				cart.totalQuantity(),
				totalAmount,
				currency,
				orderable,
				items);
	}

	private OrderQuoteItemResult createItemQuote(CartItemResult item) {
		BigDecimal unitPrice = item.unitPriceAmount().setScale(MONEY_SCALE, MONEY_ROUNDING);
		BigDecimal lineTotal = unitPrice
				.multiply(BigDecimal.valueOf(item.quantity()))
				.setScale(MONEY_SCALE, MONEY_ROUNDING);
		boolean available = inventoryAvailabilityService.check(item.productId(), item.quantity()).available();

		return new OrderQuoteItemResult(
				item.productId(),
				item.name(),
				item.quantity(),
				unitPrice,
				lineTotal,
				item.currency(),
				available);
	}

	private void validateSingleCurrency(List<CartItemResult> items, String currency) {
		boolean hasDifferentCurrency = items.stream()
				.anyMatch(item -> !currency.equals(item.currency()));
		if (hasDifferentCurrency) {
			throw new IllegalStateException("하나의 주문 견적에는 동일한 통화의 상품만 포함할 수 있습니다.");
		}
	}
}
