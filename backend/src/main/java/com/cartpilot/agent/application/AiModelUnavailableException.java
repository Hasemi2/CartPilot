package com.cartpilot.agent.application;

public class AiModelUnavailableException extends RuntimeException {

	public AiModelUnavailableException(Throwable cause) {
		super("AI 모델을 일시적으로 사용할 수 없습니다.", cause);
	}
}
