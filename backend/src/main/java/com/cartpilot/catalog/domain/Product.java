package com.cartpilot.catalog.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

	@Id
	private UUID id;

	@Column(nullable = false, length = 200)
	private String name;

	@Column(nullable = false, length = 2000)
	private String description;

	@Column(name = "price_amount", nullable = false, precision = 19, scale = 2)
	private BigDecimal priceAmount;

	@Column(nullable = false, length = 3)
	private String currency;

	@Column(nullable = false)
	private boolean published;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

}
