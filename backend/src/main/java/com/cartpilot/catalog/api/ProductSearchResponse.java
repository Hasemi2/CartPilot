package com.cartpilot.catalog.api;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품 검색 결과")
public record ProductSearchResponse(
		@Schema(description = "검색 조건에 맞는 게시 상품 목록") List<ProductSummaryResponse> products) {
}
