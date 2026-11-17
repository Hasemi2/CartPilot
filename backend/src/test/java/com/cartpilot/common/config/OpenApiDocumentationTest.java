package com.cartpilot.common.config;

import static org.hamcrest.Matchers.endsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
		"spring.datasource.url=jdbc:h2:mem:cartpilot-openapi-test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
class OpenApiDocumentationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void exposesCurrentApiSpecification() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.info.title").value("CartPilot API"))
				.andExpect(jsonPath("$.info.version").value("v1"))
				.andExpect(jsonPath("$.paths['/api/health'].get.summary")
						.value("애플리케이션 상태 조회"))
				.andExpect(jsonPath("$.paths['/api/products'].get.summary").value("상품 검색"))
				.andExpect(jsonPath("$.components.schemas.ProductSearchResponse").exists())
				.andExpect(jsonPath("$.components.schemas.ProductSummaryResponse").exists());
	}

	@Test
	void redirectsToSwaggerUi() throws Exception {
		mockMvc.perform(get("/swagger-ui.html"))
				.andExpect(status().is3xxRedirection())
				.andExpect(header().string("Location", endsWith("/swagger-ui/index.html")));
	}
}
