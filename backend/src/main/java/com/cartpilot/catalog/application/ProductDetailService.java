package com.cartpilot.catalog.application;

import java.util.UUID;

import com.cartpilot.catalog.infrastructure.ProductRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductDetailService {

	private final ProductRepository productRepository;

	@Transactional(readOnly = true)
	public ProductDetailResult getPublishedProduct(UUID productId) {
		return productRepository.findByIdAndPublishedTrue(productId)
				.map(ProductDetailResult::from)
				.orElseThrow(() -> new ProductNotFoundException(productId));
	}
}
