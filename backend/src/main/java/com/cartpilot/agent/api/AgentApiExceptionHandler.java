package com.cartpilot.agent.api;

import com.cartpilot.agent.application.AiModelUnavailableException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = AgentChatController.class)
public class AgentApiExceptionHandler {

	private static final AgentErrorResponse MODEL_UNAVAILABLE_RESPONSE = new AgentErrorResponse(
			"AI_MODEL_UNAVAILABLE",
			"AI 모델이 일시적으로 혼잡합니다. 잠시 후 다시 시도해주세요.");

	@ExceptionHandler(AiModelUnavailableException.class)
	ResponseEntity<AgentErrorResponse> handleModelUnavailable() {
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
				.body(MODEL_UNAVAILABLE_RESPONSE);
	}
}
