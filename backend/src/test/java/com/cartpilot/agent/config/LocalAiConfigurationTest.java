package com.cartpilot.agent.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import com.cartpilot.agent.infrastructure.DeterministicStubChatModel;
import com.google.genai.Client;
import com.google.genai.types.HttpOptions;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("local-ai")
@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.NONE,
		properties = {
				"spring.datasource.url=jdbc:h2:mem:cartpilot-local-ai-test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
				"spring.ai.google.genai.api-key=test-api-key"
		})
class LocalAiConfigurationTest {

	@Autowired
	private Environment environment;

	@Autowired
	private ChatModel chatModel;

	@Autowired
	private ChatClient chatClient;

	@Autowired
	private Client googleGenAiClient;

	@Test
	void configuresGeminiFlashWithoutCallingTheExternalApi() {
		assertThat(environment.acceptsProfiles(Profiles.of("local-ai"))).isTrue();
		assertThat(chatModel)
				.isInstanceOf(GoogleGenAiChatModel.class)
				.isNotInstanceOf(DeterministicStubChatModel.class);
		assertThat(chatModel.getOptions())
				.isInstanceOfSatisfying(GoogleGenAiChatOptions.class,
						options -> assertThat(options.getModel()).isEqualTo("gemini-3.5-flash-lite"));
		assertThat(chatClient).isNotNull();
		assertThat(googleGenAiClient.apiKey()).isEqualTo("test-api-key");
	}

	@Test
	void configuresGoogleSdkTimeoutAndRetryAttempts() {
		HttpOptions options = GoogleGenAiClientConfiguration.createHttpOptions(Duration.ofSeconds(15), 1);

		assertThat(options.timeout()).contains(15_000);
		assertThat(options.retryOptions()).isPresent();
		assertThat(options.retryOptions().orElseThrow().attempts()).contains(1);
	}
}
