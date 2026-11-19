package com.cartpilot.agent.config;

import java.time.Duration;

import com.google.genai.Client;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.HttpRetryOptions;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.util.Assert;

@Configuration(proxyBeanMethods = false)
@Profile("local-ai")
public class GoogleGenAiClientConfiguration {

	@Bean
	Client googleGenAiClient(
			@Value("${spring.ai.google.genai.api-key}") String apiKey,
			@Value("${cartpilot.ai.google.timeout:15s}") Duration timeout,
			@Value("${cartpilot.ai.google.retry-attempts:1}") int retryAttempts) {
		return Client.builder()
				.apiKey(apiKey)
				.httpOptions(createHttpOptions(timeout, retryAttempts))
				.build();
	}

	static HttpOptions createHttpOptions(Duration timeout, int retryAttempts) {
		Assert.isTrue(!timeout.isNegative() && !timeout.isZero(), "Google GenAI timeout must be positive");
		Assert.isTrue(retryAttempts >= 1, "Google GenAI retry attempts must be at least 1");

		long timeoutMillis = timeout.toMillis();
		Assert.isTrue(timeoutMillis <= Integer.MAX_VALUE, "Google GenAI timeout is too large");

		HttpRetryOptions retryOptions = HttpRetryOptions.builder()
				.attempts(retryAttempts)
				.build();
		return HttpOptions.builder()
				.timeout(Math.toIntExact(timeoutMillis))
				.retryOptions(retryOptions)
				.build();
	}
}
