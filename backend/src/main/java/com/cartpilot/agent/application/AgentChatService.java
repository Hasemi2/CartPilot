package com.cartpilot.agent.application;

import lombok.RequiredArgsConstructor;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AgentChatService {

	private final ChatClient chatClient;

	public String chat(String message) {
		return chatClient.prompt()
				.user(message.trim())
				.call()
				.content();
	}
}
