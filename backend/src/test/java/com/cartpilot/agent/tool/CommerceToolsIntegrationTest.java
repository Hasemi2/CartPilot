package com.cartpilot.agent.tool;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import com.cartpilot.cart.infrastructure.CartItemRepository;
import com.cartpilot.cart.infrastructure.ShoppingCartRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("local-lite")
@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.NONE,
		properties = "spring.datasource.url=jdbc:h2:mem:cartpilot-commerce-tools-test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
class CommerceToolsIntegrationTest {

	private static final String KEYBOARD_ID = "10000000-0000-0000-0000-000000000001";

	@Autowired
	private CommerceTools commerceTools;

	@Autowired
	private CartItemRepository cartItemRepository;

	@Autowired
	private ShoppingCartRepository shoppingCartRepository;

	private ToolCallback[] toolCallbacks;

	@BeforeEach
	void setUp() {
		cartItemRepository.deleteAll();
		shoppingCartRepository.deleteAll();
		toolCallbacks = ToolCallbacks.from(commerceTools);
	}

	@Test
	void exposesOnlyTheFiveCommerceTools() {
		assertThat(toolCallbacks)
				.extracting(callback -> callback.getToolDefinition().name())
				.containsExactlyInAnyOrder(
						"searchProducts",
						"getProductDetails",
						"checkStock",
						"setCartItemQuantity",
						"getCart");
	}

	@Test
	void executesToolsThroughApplicationServicesAndH2() {
		String searchResult = tool("searchProducts").call("""
				{"query":"무선 키보드"}
				""");
		assertThat(searchResult)
				.contains(KEYBOARD_ID)
				.contains("무선 키보드");

		String detailResult = tool("getProductDetails").call("""
				{"productId":"%s"}
				""".formatted(KEYBOARD_ID));
		assertThat(detailResult)
				.contains(KEYBOARD_ID)
				.contains("79000.00")
				.contains("KRW");

		String stockResult = tool("checkStock").call("""
				{"productId":"%s","quantity":2}
				""".formatted(KEYBOARD_ID));
		assertThat(stockResult)
				.contains(KEYBOARD_ID)
				.contains("\"available\":true");

		String updatedCart = tool("setCartItemQuantity").call("""
				{"productId":"%s","quantity":2}
				""".formatted(KEYBOARD_ID));
		assertThat(updatedCart)
				.contains(KEYBOARD_ID)
				.contains("\"totalQuantity\":2")
				.contains("\"quantity\":2");

		String currentCart = tool("getCart").call("{}");
		assertThat(currentCart)
				.contains(KEYBOARD_ID)
				.contains("\"totalQuantity\":2");
	}

	private ToolCallback tool(String name) {
		return Arrays.stream(toolCallbacks)
				.filter(callback -> callback.getToolDefinition().name().equals(name))
				.findFirst()
				.orElseThrow();
	}
}
