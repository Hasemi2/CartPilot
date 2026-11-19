package com.cartpilot.agent.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "AI 에이전트 대화 요청")
public record AgentChatRequest(
		@Schema(
			description = "사용자 메시지. local-lite는 문서화된 Stub 명령 형식, local-ai는 자연어를 사용합니다.",
			example = "상품 검색: 무선 키보드",
			maxLength = 500)
		@NotBlank @Size(max = 500) String message) {
}
