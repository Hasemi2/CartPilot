package com.cartpilot.catalog.api;

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
class ProductDetailApiTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void getsPublishedProductDetails() throws Exception {
		mockMvc.perform(get("/api/products/10000000-0000-0000-0000-000000000001"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value("10000000-0000-0000-0000-000000000001"))
				.andExpect(jsonPath("$.name").value("저소음 무선 키보드"))
				.andExpect(jsonPath("$.description").isNotEmpty())
				.andExpect(jsonPath("$.priceAmount").value(79000.00))
				.andExpect(jsonPath("$.currency").value("KRW"));
	}

	@Test
	void hidesUnpublishedProductAsNotFound() throws Exception {
		mockMvc.perform(get("/api/products/10000000-0000-0000-0000-000000000004"))
				.andExpect(status().isNotFound());
	}

	@Test
	void returnsNotFoundForUnknownProduct() throws Exception {
		mockMvc.perform(get("/api/products/99999999-9999-9999-9999-999999999999"))
				.andExpect(status().isNotFound());
	}

	@Test
	void rejectsMalformedProductId() throws Exception {
		mockMvc.perform(get("/api/products/not-a-uuid"))
				.andExpect(status().isBadRequest());
	}
}
