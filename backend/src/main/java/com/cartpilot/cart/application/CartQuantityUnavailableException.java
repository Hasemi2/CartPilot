package com.cartpilot.cart.application;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class CartQuantityUnavailableException extends RuntimeException {

	public CartQuantityUnavailableException(UUID productId, int quantity) {
		super("요청한 수량의 재고가 부족합니다: productId=" + productId + ", quantity=" + quantity);
	}
}
