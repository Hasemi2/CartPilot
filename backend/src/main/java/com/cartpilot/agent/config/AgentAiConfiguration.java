package com.cartpilot.agent.config;

import com.cartpilot.agent.tool.CommerceTools;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.ToolCallingAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration(proxyBeanMethods = false)
@Profile({"local-lite", "local-ai"})
public class AgentAiConfiguration {

	@Bean
	ChatClient commerceChatClient(
			ChatClient.Builder chatClientBuilder,
			ToolCallingAdvisor.Builder<?> toolCallingAdvisorBuilder,
			CommerceTools commerceTools) {
		return chatClientBuilder
				.defaultSystem("""
						CartPilot 커머스 에이전트입니다.
						등록된 Commerce Tool로 실제 상품, 재고, 장바구니 정보를 확인하십시오.
						Tool 결과에 없는 상품 ID, 가격, 재고를 만들지 마십시오.
						주문 확정이나 결제를 수행하지 마십시오.
						""")
				.defaultAdvisors(toolCallingAdvisorBuilder.build())
				.defaultTools(commerceTools)
				.build();
	}
}
