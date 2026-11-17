package com.cartpilot.catalog.application;

import java.math.BigDecimal;
import java.util.UUID;

import com.cartpilot.catalog.domain.Product;

public record ProductSearchResult(
		UUID id,
		String name,
		String description,
		BigDecimal priceAmount,
		String currency) {

	static ProductSearchResult from(Product product) {
		return new ProductSearchResult(
				product.getId(),
				product.getName(),
				product.getDescription(),
				product.getPriceAmount(),
				product.getCurrency());
	}
}
