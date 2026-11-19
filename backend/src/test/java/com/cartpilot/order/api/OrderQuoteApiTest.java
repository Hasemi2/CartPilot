package com.cartpilot.order.api;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import com.cartpilot.cart.infrastructure.CartItemRepository;
import com.cartpilot.cart.infrastructure.ShoppingCartRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("local-lite")
@SpringBootTest(properties =
		"spring.datasource.url=jdbc:h2:mem:cartpilot-order-quote-test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
class OrderQuoteApiTest {

	private static final String KEYBOARD_ID = "10000000-0000-0000-0000-000000000001";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CartItemRepository cartItemRepository;

	@Autowired
	private ShoppingCartRepository shoppingCartRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	void resetCartAndStock() {
		cartItemRepository.deleteAll();
		shoppingCartRepository.deleteAll();
		jdbcTemplate.update(
				"update inventory_stocks set on_hand_quantity = 10 where product_id = ?",
				UUID.fromString(KEYBOARD_ID));
	}

	@Test
	void returnsNonOrderableZeroQuoteForEmptyCart() throws Exception {
		mockMvc.perform(post("/api/order-quotes"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalQuantity").value(0))
				.andExpect(jsonPath("$.totalAmount").value(0.00))
				.andExpect(jsonPath("$.currency").value(nullValue()))
				.andExpect(jsonPath("$.orderable").value(false))
				.andExpect(jsonPath("$.items", hasSize(0)));
	}

	@Test
	void calculatesQuoteFromCurrentCatalogPriceAndInventory() throws Exception {
		setKeyboardQuantity(2);

		mockMvc.perform(post("/api/order-quotes"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalQuantity").value(2))
				.andExpect(jsonPath("$.totalAmount").value(158000.00))
				.andExpect(jsonPath("$.currency").value("KRW"))
				.andExpect(jsonPath("$.orderable").value(true))
				.andExpect(jsonPath("$.items", hasSize(1)))
				.andExpect(jsonPath("$.items[0].productId").value(KEYBOARD_ID))
				.andExpect(jsonPath("$.items[0].quantity").value(2))
				.andExpect(jsonPath("$.items[0].unitPriceAmount").value(79000.00))
				.andExpect(jsonPath("$.items[0].lineTotalAmount").value(158000.00))
				.andExpect(jsonPath("$.items[0].available").value(true));
	}

	@Test
	void marksQuoteAsNonOrderableWhenStockChangedAfterCartUpdate() throws Exception {
		setKeyboardQuantity(2);
		jdbcTemplate.update(
				"update inventory_stocks set on_hand_quantity = 0 where product_id = ?",
				UUID.fromString(KEYBOARD_ID));

		mockMvc.perform(post("/api/order-quotes"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalAmount").value(158000.00))
				.andExpect(jsonPath("$.orderable").value(false))
				.andExpect(jsonPath("$.items[0].available").value(false));
	}

	private void setKeyboardQuantity(int quantity) throws Exception {
		mockMvc.perform(put("/api/carts/me/items/{productId}", KEYBOARD_ID)
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"quantity\": " + quantity + "}"))
				.andExpect(status().isOk());
	}
}
