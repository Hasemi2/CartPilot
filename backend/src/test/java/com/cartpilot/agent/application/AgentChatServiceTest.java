package com.cartpilot.agent.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.SocketTimeoutException;

import com.google.genai.errors.GenAiIOException;
import com.google.genai.errors.ServerException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

class AgentChatServiceTest {

	private ChatClient.CallResponseSpec callResponseSpec;
	private AgentChatService agentChatService;

	@BeforeEach
	void setUp() {
		ChatClient chatClient = mock(ChatClient.class);
		ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
		callResponseSpec = mock(ChatClient.CallResponseSpec.class);

		when(chatClient.prompt()).thenReturn(requestSpec);
		when(requestSpec.user("상품을 찾아줘")).thenReturn(requestSpec);
		when(requestSpec.call()).thenReturn(callResponseSpec);
		agentChatService = new AgentChatService(chatClient);
	}

	@Test
	void convertsGoogleOverloadIntoModelUnavailableException() {
		RuntimeException failure = new RuntimeException(
				"Failed to generate content",
				new ServerException(503, "UNAVAILABLE", "The model is experiencing high demand."));
		when(callResponseSpec.content()).thenThrow(failure);

		assertThatThrownBy(() -> agentChatService.chat(" 상품을 찾아줘 "))
				.isInstanceOf(AiModelUnavailableException.class)
				.hasCause(failure);
	}

	@Test
	void convertsGoogleTimeoutIntoModelUnavailableException() {
		RuntimeException failure = new GenAiIOException(
				"Failed to execute HTTP request.",
				new SocketTimeoutException("timeout"));
		when(callResponseSpec.content()).thenThrow(failure);

		assertThatThrownBy(() -> agentChatService.chat("상품을 찾아줘"))
				.isInstanceOf(AiModelUnavailableException.class)
				.hasCause(failure);
	}
}
