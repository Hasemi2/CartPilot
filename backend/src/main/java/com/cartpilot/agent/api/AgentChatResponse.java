package com.cartpilot.agent.api;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI 에이전트 대화 응답")
public record AgentChatResponse(
		@Schema(description = "ChatClient가 반환한 최종 답변") String answer) {
}
