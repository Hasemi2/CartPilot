package com.cartpilot.catalog.api;

import java.math.BigDecimal;
import java.util.UUID;

import com.cartpilot.catalog.application.ProductDetailService;
import com.cartpilot.catalog.application.ProductSearchService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Catalog", description = "게시 상품 탐색")
@RequiredArgsConstructor
public class ProductController {

	private final ProductDetailService productDetailService;
	private final ProductSearchService productSearchService;

	@GetMapping
	@Operation(
			summary = "상품 검색",
			description = "게시된 상품의 이름과 설명을 검색하고 선택적으로 최대 가격을 적용합니다. 최대 20개를 반환합니다.")
	@ApiResponses({
			@ApiResponse(
					responseCode = "200",
					description = "상품 검색 성공",
					content = @Content(schema = @Schema(implementation = ProductSearchResponse.class))),
			@ApiResponse(responseCode = "400", description = "검색 조건이 유효하지 않음", content = @Content)
	})
	public ProductSearchResponse search(
			@Parameter(description = "상품 이름 또는 설명 검색어", example = "키보드")
			@RequestParam(defaultValue = "") @Size(max = 100) String query,
			@Parameter(description = "허용할 최대 상품 가격", example = "100000")
			@RequestParam(required = false) @DecimalMin("0.00") BigDecimal maxPrice) {
		return new ProductSearchResponse(
				productSearchService.search(query, maxPrice).stream()
						.map(ProductSummaryResponse::from)
						.toList());
	}

	@GetMapping("/{productId}")
	@Operation(summary = "상품 상세 조회", description = "상품 ID로 게시된 상품의 상세 정보와 현재 가격을 조회합니다.")
	@ApiResponses({
			@ApiResponse(
					responseCode = "200",
					description = "상품 상세 조회 성공",
					content = @Content(schema = @Schema(implementation = ProductDetailResponse.class))),
			@ApiResponse(responseCode = "400", description = "상품 ID 형식이 유효하지 않음", content = @Content),
			@ApiResponse(responseCode = "404", description = "게시된 상품을 찾을 수 없음", content = @Content)
	})
	public ProductDetailResponse getProduct(
			@Parameter(description = "상품 UUID", required = true)
			@PathVariable UUID productId) {
		return ProductDetailResponse.from(productDetailService.getPublishedProduct(productId));
	}
}
