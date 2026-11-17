package com.cartpilot.agent.tool;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.cartpilot.cart.application.CartResult;
import com.cartpilot.cart.application.CartService;
import com.cartpilot.catalog.application.ProductDetailResult;
import com.cartpilot.catalog.application.ProductDetailService;
import com.cartpilot.catalog.application.ProductSearchResult;
import com.cartpilot.catalog.application.ProductSearchService;
import com.cartpilot.inventory.application.InventoryAvailabilityResult;
import com.cartpilot.inventory.application.InventoryAvailabilityService;

import lombok.RequiredArgsConstructor;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommerceTools {

	private final ProductSearchService productSearchService;
	private final ProductDetailService productDetailService;
	private final InventoryAvailabilityService inventoryAvailabilityService;
	private final CartService cartService;

	@Tool(name = "searchProducts", description = "판매 중인 상품을 이름과 설명으로 검색한다. 최대 가격은 선택 사항이다.")
	public List<ProductSearchResult> searchProducts(
			@ToolParam(description = "상품 검색어. 전체 상품을 조회하려면 빈 문자열을 사용한다.") String query,
			@ToolParam(required = false, description = "조회할 상품의 최대 가격. 제한하지 않으면 생략한다.") BigDecimal maxPrice) {
		return productSearchService.search(query, maxPrice);
	}

	@Tool(name = "getProductDetails", description = "판매 중인 상품 한 개의 상세 정보와 현재 가격을 조회한다.")
	public ProductDetailResult getProductDetails(
			@ToolParam(description = "조회할 상품 ID") UUID productId) {
		return productDetailService.getPublishedProduct(productId);
	}

	@Tool(name = "checkStock", description = "상품의 요청 수량을 현재 구매할 수 있는지 확인한다. 재고를 차감하지 않는다.")
	public InventoryAvailabilityResult checkStock(
			@ToolParam(description = "재고를 확인할 상품 ID") UUID productId,
			@ToolParam(description = "확인할 수량. 1 이상 100 이하") int quantity) {
		return inventoryAvailabilityService.check(productId, quantity);
	}

	@Tool(name = "setCartItemQuantity", description = "현재 사용자의 장바구니에서 상품 수량을 설정한다. 기존 수량에 더하지 않고 요청 수량으로 교체한다.")
	public CartResult setCartItemQuantity(
			@ToolParam(description = "장바구니에 담을 상품 ID") UUID productId,
			@ToolParam(description = "설정할 수량. 1 이상 100 이하") int quantity) {
		return cartService.setItemQuantity(productId, quantity);
	}

	@Tool(name = "getCart", description = "현재 사용자의 장바구니 상품과 수량 및 현재 가격을 조회한다.")
	public CartResult getCart() {
		return cartService.getMyCart();
	}
}
