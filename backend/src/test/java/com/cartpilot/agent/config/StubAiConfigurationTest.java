package com.cartpilot.agent.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.cartpilot.agent.infrastructure.DeterministicStubChatModel;
import com.cartpilot.cart.infrastructure.CartItemRepository;
import com.cartpilot.cart.infrastructure.ShoppingCartRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("local-lite")
@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.NONE,
		properties = "spring.datasource.url=jdbc:h2:mem:cartpilot-stub-ai-test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
class StubAiConfigurationTest {

	private static final String KEYBOARD_ID = "10000000-0000-0000-0000-000000000001";

	@Autowired
	private ChatModel chatModel;

	@Autowired
	private ChatClient chatClient;

	@Autowired
	private CartItemRepository cartItemRepository;

	@Autowired
	private ShoppingCartRepository shoppingCartRepository;

	@BeforeEach
	void clearCart() {
		cartItemRepository.deleteAll();
		shoppingCartRepository.deleteAll();
	}

	@Test
	void configuresApiKeyFreeStubChatModel() {
		assertThat(chatModel).isInstanceOf(DeterministicStubChatModel.class);
	}

	@Test
	void executesSearchToolThroughChatClient() {
		String answer = chatClient.prompt()
				.user("상품 검색: 무선 키보드")
				.call()
				.content();

		assertThat(answer)
				.contains("Stub Tool 실행 결과")
				.contains(KEYBOARD_ID)
				.contains("무선 키보드");
	}

	@Test
	void executesProductDetailAndStockToolsThroughChatClient() {
		String detail = chatClient.prompt()
				.user("상품 상세: " + KEYBOARD_ID)
				.call()
				.content();

		assertThat(detail)
				.contains(KEYBOARD_ID)
				.contains("79000.00")
				.contains("KRW");

		String stock = chatClient.prompt()
				.user("재고 확인: " + KEYBOARD_ID + ", 2")
				.call()
				.content();

		assertThat(stock)
				.contains(KEYBOARD_ID)
				.contains("\"available\":true");
	}

	@Test
	void executesCartMutationAndQueryToolsThroughChatClient() {
		String updatedCart = chatClient.prompt()
				.user("장바구니 수량 설정: " + KEYBOARD_ID + ", 2")
				.call()
				.content();

		assertThat(updatedCart)
				.contains(KEYBOARD_ID)
				.contains("\"totalQuantity\":2");

		String currentCart = chatClient.prompt()
				.user("장바구니 조회")
				.call()
				.content();

		assertThat(currentCart)
				.contains(KEYBOARD_ID)
				.contains("\"quantity\":2");
	}
}
