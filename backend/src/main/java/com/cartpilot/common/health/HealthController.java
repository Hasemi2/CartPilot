package com.cartpilot.common.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
@Tag(name = "Health", description = "애플리케이션 상태 확인")
public class HealthController {

	@GetMapping
	@Operation(summary = "애플리케이션 상태 조회", description = "애플리케이션이 요청을 처리할 수 있는지 확인합니다.")
	@ApiResponse(
			responseCode = "200",
			description = "정상 상태",
			content = @Content(schema = @Schema(implementation = HealthResponse.class)))
	public HealthResponse health() {
		return new HealthResponse("UP");
	}
}
