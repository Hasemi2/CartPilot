package com.cartpilot.agent.application;

import java.net.SocketTimeoutException;

import com.google.genai.errors.GenAiIOException;
import com.google.genai.errors.ServerException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgentChatService {

	private final ChatClient chatClient;

	public String chat(String message) {
		try {
			return chatClient.prompt()
					.user(message.trim())
					.call()
					.content();
		}
		catch (RuntimeException exception) {
			ModelFailureReason failureReason = classifyModelFailure(exception);
			if (failureReason != null) {
				log.warn("AI model call failed: reason={}", failureReason);
				throw new AiModelUnavailableException(exception);
			}
			throw exception;
		}
	}

	private ModelFailureReason classifyModelFailure(Throwable exception) {
		Throwable current = exception;
		boolean connectionError = false;
		while (current != null) {
			if (current instanceof ServerException serverException && serverException.code() == 503) {
				return ModelFailureReason.GOOGLE_503;
			}
			if (current instanceof SocketTimeoutException) {
				return ModelFailureReason.TIMEOUT;
			}
			if (current instanceof GenAiIOException) {
				connectionError = true;
			}
			current = current.getCause();
		}
		return connectionError ? ModelFailureReason.CONNECTION_ERROR : null;
	}

	private enum ModelFailureReason {
		GOOGLE_503,
		TIMEOUT,
		CONNECTION_ERROR
	}
}
