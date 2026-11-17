package com.cartpilot.inventory.api;

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
		"spring.datasource.url=jdbc:h2:mem:cartpilot-inventory-test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
class InventoryAvailabilityApiTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void reportsRequestedQuantityIsAvailable() throws Exception {
		mockMvc.perform(get("/api/products/10000000-0000-0000-0000-000000000001/availability")
					.param("quantity", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.productId").value("10000000-0000-0000-0000-000000000001"))
				.andExpect(jsonPath("$.requestedQuantity").value(10))
				.andExpect(jsonPath("$.available").value(true));
	}

	@Test
	void reportsRequestedQuantityIsUnavailable() throws Exception {
		mockMvc.perform(get("/api/products/10000000-0000-0000-0000-000000000001/availability")
					.param("quantity", "11"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.requestedQuantity").value(11))
				.andExpect(jsonPath("$.available").value(false));
	}

	@Test
	void rejectsQuantityOutsideAllowedRange() throws Exception {
		mockMvc.perform(get("/api/products/10000000-0000-0000-0000-000000000001/availability")
					.param("quantity", "0"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void hidesUnpublishedProductAsNotFound() throws Exception {
		mockMvc.perform(get("/api/products/10000000-0000-0000-0000-000000000004/availability"))
				.andExpect(status().isNotFound());
	}
}
