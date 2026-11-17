package com.cartpilot.catalog.application;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProductNotFoundException extends RuntimeException {

	public ProductNotFoundException(UUID productId) {
		super("상품을 찾을 수 없습니다: " + productId);
	}
}
