package com.cartpilot.cart.api;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
		"spring.datasource.url=jdbc:h2:mem:cartpilot-cart-test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
class CartApiTest {

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
	void returnsEmptyCartInitially() throws Exception {
		mockMvc.perform(get("/api/carts/me"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalQuantity").value(0))
				.andExpect(jsonPath("$.items", hasSize(0)));
	}

	@Test
	void addsProductWithCurrentPrice() throws Exception {
		mockMvc.perform(put("/api/carts/me/items/{productId}", KEYBOARD_ID)
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{"quantity": 2}
							"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalQuantity").value(2))
				.andExpect(jsonPath("$.items", hasSize(1)))
				.andExpect(jsonPath("$.items[0].productId").value(KEYBOARD_ID))
				.andExpect(jsonPath("$.items[0].quantity").value(2))
				.andExpect(jsonPath("$.items[0].unitPriceAmount").value(79000.00))
				.andExpect(jsonPath("$.items[0].lineTotalAmount").value(158000.00))
				.andExpect(jsonPath("$.items[0].currency").value("KRW"));
	}

	@Test
	void replacesQuantityWithoutDuplicatingItem() throws Exception {
		setKeyboardQuantity(2);

		mockMvc.perform(put("/api/carts/me/items/{productId}", KEYBOARD_ID)
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{"quantity": 3}
							"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalQuantity").value(3))
				.andExpect(jsonPath("$.items", hasSize(1)))
				.andExpect(jsonPath("$.items[0].quantity").value(3));
	}

	@Test
	void rejectsQuantityAboveCurrentStock() throws Exception {
		mockMvc.perform(put("/api/carts/me/items/{productId}", KEYBOARD_ID)
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{"quantity": 11}
							"""))
				.andExpect(status().isConflict());
	}

	@Test
	void rejectsQuantityOutsideAllowedRange() throws Exception {
		mockMvc.perform(put("/api/carts/me/items/{productId}", KEYBOARD_ID)
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{"quantity": 0}
							"""))
				.andExpect(status().isBadRequest());
	}

	@Test
	void removesItemIdempotently() throws Exception {
		setKeyboardQuantity(2);

		mockMvc.perform(delete("/api/carts/me/items/{productId}", KEYBOARD_ID))
				.andExpect(status().isNoContent());

		mockMvc.perform(delete("/api/carts/me/items/{productId}", KEYBOARD_ID))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/carts/me"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items", hasSize(0)));
	}

	private void setKeyboardQuantity(int quantity) throws Exception {
		mockMvc.perform(put("/api/carts/me/items/{productId}", KEYBOARD_ID)
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"quantity\": " + quantity + "}"))
				.andExpect(status().isOk());
	}
}
