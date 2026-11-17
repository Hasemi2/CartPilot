package com.cartpilot.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

	@Bean
	OpenAPI cartPilotOpenApi() {
		return new OpenAPI()
				.info(new Info()
						.title("CartPilot API")
						.version("v1")
						.description("상품 탐색과 커머스 기능을 제공하는 CartPilot API"));
	}
}
