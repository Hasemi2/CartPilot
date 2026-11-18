package com.cartpilot.agent.config;

import com.cartpilot.agent.infrastructure.DeterministicStubChatModel;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration(proxyBeanMethods = false)
@Profile("local-lite")
public class StubAiConfiguration {

	@Bean
	ChatModel stubChatModel() {
		return new DeterministicStubChatModel();
	}
}
