package com.cartpilot.agent.api;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cartpilot.cart.infrastructure.CartItemRepository;
import com.cartpilot.cart.infrastructure.ShoppingCartRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("local-lite")
@SpringBootTest(properties =
		"spring.datasource.url=jdbc:h2:mem:cartpilot-agent-chat-api-test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
class AgentChatApiTest {

	private static final String KEYBOARD_ID = "10000000-0000-0000-0000-000000000001";

	@Autowired
	private MockMvc mockMvc;

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
	void searchesProductsThroughStubChatApi() throws Exception {
		mockMvc.perform(post("/api/agent/chat")
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{"message":"상품 검색: 무선 키보드"}
							"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.answer", containsString("Stub Tool 실행 결과")))
				.andExpect(jsonPath("$.answer", containsString(KEYBOARD_ID)))
				.andExpect(jsonPath("$.answer", containsString("무선 키보드")));
	}

	@Test
	void changesAndReadsCartThroughStubChatApi() throws Exception {
		mockMvc.perform(post("/api/agent/chat")
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{"message":"장바구니 수량 설정: %s, 2"}
							""".formatted(KEYBOARD_ID)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.answer", containsString("\"totalQuantity\":2")));

		mockMvc.perform(post("/api/agent/chat")
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{"message":"장바구니 조회"}
							"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.answer", containsString(KEYBOARD_ID)))
				.andExpect(jsonPath("$.answer", containsString("\"quantity\":2")));
	}

	@Test
	void rejectsBlankMessage() throws Exception {
		mockMvc.perform(post("/api/agent/chat")
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{"message":"  "}
							"""))
				.andExpect(status().isBadRequest());
	}

	@Test
	void documentsChatEndpointInOpenApi() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.paths['/api/agent/chat'].post.summary")
						.value("커머스 에이전트 대화"))
				.andExpect(jsonPath("$.components.schemas.AgentChatRequest").exists())
				.andExpect(jsonPath("$.components.schemas.AgentChatResponse").exists());
	}
}
