package com.cartpilot.catalog.api;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("local-lite")
@SpringBootTest(properties =
		"spring.datasource.url=jdbc:h2:mem:cartpilot-catalog-test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
class ProductSearchApiTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void searchesOnlyPublishedProductsWithinMaximumPrice() throws Exception {
		mockMvc.perform(get("/api/products")
					.param("query", "키보드")
					.param("maxPrice", "100000"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.products", hasSize(1)))
				.andExpect(jsonPath("$.products[0].id").value("10000000-0000-0000-0000-000000000001"))
				.andExpect(jsonPath("$.products[0].name").value("저소음 무선 키보드"))
				.andExpect(jsonPath("$.products[0].priceAmount").value(79000.00))
				.andExpect(jsonPath("$.products[0].currency").value("KRW"));
	}

	@Test
	void listsAllPublishedProductsWhenQueryIsEmpty() throws Exception {
		mockMvc.perform(get("/api/products"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.products", hasSize(3)));
	}

	@Test
	void rejectsNegativeMaximumPrice() throws Exception {
		mockMvc.perform(get("/api/products").param("maxPrice", "-1"))
				.andExpect(status().isBadRequest());
	}
}
