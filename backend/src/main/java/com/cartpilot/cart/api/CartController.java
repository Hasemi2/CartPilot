package com.cartpilot.cart.api;

import java.util.UUID;

import com.cartpilot.cart.application.CartService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/carts/me")
@Tag(name = "Cart", description = "현재 사용자 장바구니")
@RequiredArgsConstructor
public class CartController {

	private final CartService cartService;

	@GetMapping
	@Operation(summary = "내 장바구니 조회")
	@ApiResponse(
			responseCode = "200",
			description = "장바구니 조회 성공",
			content = @Content(schema = @Schema(implementation = CartResponse.class)))
	public CartResponse getMyCart() {
		return CartResponse.from(cartService.getMyCart());
	}

	@PutMapping("/items/{productId}")
	@Operation(summary = "장바구니 상품 수량 설정", description = "상품을 추가하거나 기존 상품 수량을 교체합니다.")
	@ApiResponses({
			@ApiResponse(
					responseCode = "200",
					description = "수량 설정 성공",
					content = @Content(schema = @Schema(implementation = CartResponse.class))),
			@ApiResponse(responseCode = "400", description = "상품 ID 또는 수량이 유효하지 않음", content = @Content),
			@ApiResponse(responseCode = "404", description = "게시된 상품을 찾을 수 없음", content = @Content),
			@ApiResponse(responseCode = "409", description = "요청 수량의 재고가 부족함", content = @Content)
	})
	public CartResponse setItemQuantity(
			@Parameter(description = "상품 UUID", required = true) @PathVariable UUID productId,
			@Valid @RequestBody SetCartItemQuantityRequest request) {
		return CartResponse.from(cartService.setItemQuantity(productId, request.quantity()));
	}

	@DeleteMapping("/items/{productId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "장바구니 상품 제거")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "상품 제거 완료"),
			@ApiResponse(responseCode = "400", description = "상품 ID 형식이 유효하지 않음", content = @Content)
	})
	public void removeItem(
			@Parameter(description = "상품 UUID", required = true) @PathVariable UUID productId) {
		cartService.removeItem(productId);
	}
}
