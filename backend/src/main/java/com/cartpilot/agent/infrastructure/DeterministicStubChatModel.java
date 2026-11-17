package com.cartpilot.agent.infrastructure;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.util.JsonHelper;

/**
 * API 키 없이 Tool Calling 흐름을 재현하기 위한 결정적 ChatModel이다.
 * 자연어를 이해하는 모델이 아니며, 지원하는 고정 명령 형식만 Tool 호출로 변환한다.
 */
public class DeterministicStubChatModel implements ChatModel {

	private static final JsonHelper JSON_HELPER = new JsonHelper();

	private static final String SEARCH_PREFIX = "상품 검색:";
	private static final String DETAIL_PREFIX = "상품 상세:";
	private static final String STOCK_PREFIX = "재고 확인:";
	private static final String SET_CART_PREFIX = "장바구니 수량 설정:";
	private static final String GET_CART_COMMAND = "장바구니 조회";

	@Override
	public ChatResponse call(Prompt prompt) {
		Optional<ToolResponseMessage> toolResponse = lastToolResponse(prompt);
		if (toolResponse.isPresent()) {
			return textResponse(formatToolResult(toolResponse.get()));
		}

		String userText = prompt.getUserMessage().getText().trim();
		return selectTool(userText)
				.map(this::toolCallResponse)
				.orElseGet(() -> textResponse("지원하지 않는 Stub 명령입니다. " + supportedCommands()));
	}

	@Override
	public ChatOptions getOptions() {
		return ToolCallingChatOptions.builder().build();
	}

	private Optional<ToolRequest> selectTool(String userText) {
		if (userText.equals(GET_CART_COMMAND)) {
			return Optional.of(new ToolRequest("getCart", Map.of()));
		}
		if (userText.startsWith(SEARCH_PREFIX)) {
			return Optional.of(new ToolRequest("searchProducts", Map.of(
					"query", valueAfterPrefix(userText, SEARCH_PREFIX))));
		}
		if (userText.startsWith(DETAIL_PREFIX)) {
			return Optional.of(new ToolRequest("getProductDetails", Map.of(
					"productId", valueAfterPrefix(userText, DETAIL_PREFIX))));
		}
		if (userText.startsWith(STOCK_PREFIX)) {
			return Optional.of(productAndQuantityRequest(
					"checkStock", valueAfterPrefix(userText, STOCK_PREFIX)));
		}
		if (userText.startsWith(SET_CART_PREFIX)) {
			return Optional.of(productAndQuantityRequest(
					"setCartItemQuantity", valueAfterPrefix(userText, SET_CART_PREFIX)));
		}
		return Optional.empty();
	}

	private ToolRequest productAndQuantityRequest(String toolName, String arguments) {
		String[] values = Arrays.stream(arguments.split(","))
				.map(String::trim)
				.toArray(String[]::new);
		if (values.length != 2) {
			throw new IllegalArgumentException("상품 ID와 수량은 쉼표로 구분해야 합니다.");
		}

		return new ToolRequest(toolName, Map.of(
				"productId", values[0],
				"quantity", Integer.parseInt(values[1])));
	}

	private String valueAfterPrefix(String userText, String prefix) {
		String value = userText.substring(prefix.length()).trim();
		if (value.isEmpty()) {
			throw new IllegalArgumentException("Stub 명령에 필요한 값이 없습니다.");
		}
		return value;
	}

	private ChatResponse toolCallResponse(ToolRequest request) {
		AssistantMessage.ToolCall toolCall = new AssistantMessage.ToolCall(
				"stub-call-" + request.name(),
				"function",
				request.name(),
				JSON_HELPER.toJson(request.arguments()));
		AssistantMessage message = AssistantMessage.builder()
				.content("")
				.toolCalls(List.of(toolCall))
				.build();
		return new ChatResponse(List.of(new Generation(message)));
	}

	private ChatResponse textResponse(String content) {
		return new ChatResponse(List.of(new Generation(new AssistantMessage(content))));
	}

	private Optional<ToolResponseMessage> lastToolResponse(Prompt prompt) {
		return prompt.getInstructions().stream()
				.filter(ToolResponseMessage.class::isInstance)
				.map(ToolResponseMessage.class::cast)
				.reduce((first, second) -> second);
	}

	private String formatToolResult(ToolResponseMessage message) {
		return message.getResponses().stream()
				.map(ToolResponseMessage.ToolResponse::responseData)
				.reduce((first, second) -> first + System.lineSeparator() + second)
				.map(result -> "Stub Tool 실행 결과: " + result)
				.orElse("Stub Tool 실행 결과가 없습니다.");
	}

	private String supportedCommands() {
		return "상품 검색: 검색어 | 상품 상세: 상품ID | 재고 확인: 상품ID, 수량 | "
				+ "장바구니 수량 설정: 상품ID, 수량 | 장바구니 조회";
	}

	private record ToolRequest(String name, Map<String, Object> arguments) {
	}
}
