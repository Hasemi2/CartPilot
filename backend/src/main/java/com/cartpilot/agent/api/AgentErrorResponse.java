package com.cartpilot.agent.api;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI 에이전트 오류 응답")
public record AgentErrorResponse(
		@Schema(description = "오류 코드", example = "AI_MODEL_UNAVAILABLE") String code,
		@Schema(description = "사용자 안내 메시지", example = "AI 모델이 일시적으로 혼잡합니다. 잠시 후 다시 시도해주세요.") String message) {
}
