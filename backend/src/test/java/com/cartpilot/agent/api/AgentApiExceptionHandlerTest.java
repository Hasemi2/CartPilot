package com.cartpilot.agent.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cartpilot.agent.application.AgentChatService;
import com.cartpilot.agent.application.AiModelUnavailableException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AgentChatController.class)
@Import(AgentApiExceptionHandler.class)
class AgentApiExceptionHandlerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AgentChatService agentChatService;

	@Test
	void returnsServiceUnavailableWhenAiModelCannotRespond() throws Exception {
		when(agentChatService.chat("상품을 찾아줘"))
				.thenThrow(new AiModelUnavailableException(new RuntimeException("upstream unavailable")));

		mockMvc.perform(post("/api/agent/chat")
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{"message":"상품을 찾아줘"}
							"""))
				.andExpect(status().isServiceUnavailable())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.code").value("AI_MODEL_UNAVAILABLE"))
				.andExpect(jsonPath("$.message")
						.value("AI 모델이 일시적으로 혼잡합니다. 잠시 후 다시 시도해주세요."));
	}
}
