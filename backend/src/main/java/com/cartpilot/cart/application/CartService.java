package com.cartpilot.cart.application;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.cartpilot.cart.domain.CartItem;
import com.cartpilot.cart.domain.ShoppingCart;
import com.cartpilot.cart.infrastructure.CartItemRepository;
import com.cartpilot.cart.infrastructure.ShoppingCartRepository;
import com.cartpilot.catalog.application.ProductDetailResult;
import com.cartpilot.catalog.application.ProductDetailService;
import com.cartpilot.common.security.CurrentUserProvider;
import com.cartpilot.inventory.application.InventoryAvailabilityService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartService {

	private static final int MIN_QUANTITY = 1;
	private static final int MAX_QUANTITY = 100;

	private final CurrentUserProvider currentUserProvider;
	private final ShoppingCartRepository shoppingCartRepository;
	private final CartItemRepository cartItemRepository;
	private final ProductDetailService productDetailService;
	private final InventoryAvailabilityService inventoryAvailabilityService;

	@Transactional(readOnly = true)
	public CartResult getMyCart() {
		return shoppingCartRepository.findByUserId(currentUserProvider.currentUserId())
				.map(this::toResult)
				.orElseGet(CartResult::empty);
	}

	@Transactional
	public CartResult setItemQuantity(UUID productId, int quantity) {
		validateQuantity(quantity);

		if (!inventoryAvailabilityService.check(productId, quantity).available()) {
			throw new CartQuantityUnavailableException(productId, quantity);
		}

		ShoppingCart cart = shoppingCartRepository.findByUserId(currentUserProvider.currentUserId())
				.orElseGet(() -> shoppingCartRepository.save(
						ShoppingCart.create(currentUserProvider.currentUserId())));

		CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
				.map(existingItem -> {
					existingItem.changeQuantity(quantity);
					return existingItem;
				})
				.orElseGet(() -> CartItem.create(cart.getId(), productId, quantity));

		cartItemRepository.save(item);
		cart.touch();
		return toResult(cart);
	}

	@Transactional
	public void removeItem(UUID productId) {
		shoppingCartRepository.findByUserId(currentUserProvider.currentUserId())
				.ifPresent(cart -> {
					cartItemRepository.deleteByCartIdAndProductId(cart.getId(), productId);
					cart.touch();
				});
	}

	private CartResult toResult(ShoppingCart cart) {
		List<CartItemResult> items = cartItemRepository.findAllByCartIdOrderByCreatedAtAsc(cart.getId())
				.stream()
				.map(this::toItemResult)
				.toList();
		int totalQuantity = items.stream().mapToInt(CartItemResult::quantity).sum();
		return new CartResult(totalQuantity, items);
	}

	private CartItemResult toItemResult(CartItem item) {
		ProductDetailResult product = productDetailService.getPublishedProduct(item.getProductId());
		BigDecimal lineTotal = product.priceAmount().multiply(BigDecimal.valueOf(item.getQuantity()));
		return new CartItemResult(
				product.id(),
				product.name(),
				item.getQuantity(),
				product.priceAmount(),
				lineTotal,
				product.currency());
	}

	private void validateQuantity(int quantity) {
		if (quantity < MIN_QUANTITY || quantity > MAX_QUANTITY) {
			throw new IllegalArgumentException("수량은 1개 이상 100개 이하여야 합니다.");
		}
	}
}
