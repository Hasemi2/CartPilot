package com.cartpilot.agent.api;

import com.cartpilot.agent.application.AgentChatService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agent/chat")
@Tag(name = "Agent", description = "Spring AI ChatClient와 Commerce Tool 대화")
@RequiredArgsConstructor
public class AgentChatController {

	private final AgentChatService agentChatService;

	@PostMapping
	@Operation(
		summary = "에이전트 대화",
		description = "local-lite에서는 '상품 검색: 검색어', '상품 상세: 상품ID', "
				+ "'재고 확인: 상품ID, 수량', '장바구니 수량 설정: 상품ID, 수량', "
				+ "'장바구니 조회' 형식을 지원합니다.")
	@ApiResponses({
		@ApiResponse(
				responseCode = "200",
				description = "대화 및 Tool 실행 성공",
				content = @Content(schema = @Schema(implementation = AgentChatResponse.class))),
		@ApiResponse(responseCode = "400", description = "메시지가 비어 있거나 허용 길이를 초과함", content = @Content)
	})
	public AgentChatResponse chat(@Valid @RequestBody AgentChatRequest request) {
		return new AgentChatResponse(agentChatService.chat(request.message()));
	}
}
