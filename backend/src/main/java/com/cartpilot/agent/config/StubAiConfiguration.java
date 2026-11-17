package com.cartpilot.agent.config;

import com.cartpilot.agent.infrastructure.DeterministicStubChatModel;
import com.cartpilot.agent.tool.CommerceTools;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.ToolCallingAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
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

	@Bean
	ChatClient commerceChatClient(
			ChatClient.Builder chatClientBuilder,
			ToolCallingAdvisor.Builder<?> toolCallingAdvisorBuilder,
			CommerceTools commerceTools) {
		return chatClientBuilder
				.defaultSystem("CartPilot의 결정적 Stub ChatModel입니다. 등록된 Commerce Tool만 사용합니다.")
				.defaultAdvisors(toolCallingAdvisorBuilder.build())
				.defaultOptions(ToolCallingChatOptions.builder())
				.defaultTools(commerceTools)
				.build();
	}
}
