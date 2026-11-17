package com.cartpilot.inventory.application;

import java.util.UUID;

import com.cartpilot.catalog.application.ProductDetailService;
import com.cartpilot.inventory.infrastructure.InventoryStockRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryAvailabilityService {

	private static final int MIN_QUANTITY = 1;
	private static final int MAX_QUANTITY = 100;

	private final ProductDetailService productDetailService;
	private final InventoryStockRepository inventoryStockRepository;

	@Transactional(readOnly = true)
	public InventoryAvailabilityResult check(UUID productId, int requestedQuantity) {
		validateQuantity(requestedQuantity);
		productDetailService.getPublishedProduct(productId);

		boolean available = inventoryStockRepository.findById(productId)
				.map(stock -> stock.canFulfill(requestedQuantity))
				.orElse(false);

		return new InventoryAvailabilityResult(productId, requestedQuantity, available);
	}

	private void validateQuantity(int quantity) {
		if (quantity < MIN_QUANTITY || quantity > MAX_QUANTITY) {
			throw new IllegalArgumentException("수량은 1개 이상 100개 이하여야 합니다.");
		}
	}
}
