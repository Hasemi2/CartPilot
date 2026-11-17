package com.cartpilot.catalog.application;

import java.math.BigDecimal;
import java.util.List;

import com.cartpilot.catalog.infrastructure.ProductRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ProductSearchService {

	private static final int MAX_RESULTS = 20;
	private static final int MAX_QUERY_LENGTH = 100;

	private final ProductRepository productRepository;

	@Transactional(readOnly = true)
	public List<ProductSearchResult> search(String query, BigDecimal maxPrice) {
		String normalizedQuery = query == null ? "" : query.trim();
		validate(normalizedQuery, maxPrice);

		return productRepository.searchPublished(normalizedQuery, maxPrice, PageRequest.of(0, MAX_RESULTS))
				.stream()
				.map(ProductSearchResult::from)
				.toList();
	}

	private void validate(String query, BigDecimal maxPrice) {
		if (query.length() > MAX_QUERY_LENGTH) {
			throw new IllegalArgumentException("검색어는 100자 이하여야 합니다.");
		}
		if (maxPrice != null && maxPrice.signum() < 0) {
			throw new IllegalArgumentException("최대 가격은 0 이상이어야 합니다.");
		}
	}
}
