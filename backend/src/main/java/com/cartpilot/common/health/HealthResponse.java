package com.cartpilot.common.health;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "애플리케이션 상태 응답")
public record HealthResponse(
		@Schema(description = "애플리케이션 상태", example = "UP") String status) {
}
