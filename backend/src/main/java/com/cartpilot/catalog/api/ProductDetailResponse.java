package com.cartpilot.catalog.api;

import java.math.BigDecimal;
import java.util.UUID;

import com.cartpilot.catalog.application.ProductDetailResult;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품 상세 정보")
public record ProductDetailResponse(
		@Schema(description = "상품 식별자", example = "10000000-0000-0000-0000-000000000001") UUID id,
		@Schema(description = "상품명", example = "저소음 무선 키보드") String name,
		@Schema(description = "상품 설명", example = "조용한 타건감과 멀티 페어링을 지원하는 무선 키보드") String description,
		@Schema(description = "현재 상품 가격", example = "79000.00", minimum = "0") BigDecimal priceAmount,
		@Schema(description = "ISO 4217 통화 코드", example = "KRW", minLength = 3, maxLength = 3) String currency) {

	static ProductDetailResponse from(ProductDetailResult product) {
		return new ProductDetailResponse(
				product.id(),
				product.name(),
				product.description(),
				product.priceAmount(),
				product.currency());
	}
}
