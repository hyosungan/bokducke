package ssafy.bokduck.service;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.MessageType;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ssafy.bokduck.tools.RealEstateTools;
import ssafy.bokduck.context.PropertySearchContext; // Ensure context is used
import org.springframework.ai.chat.memory.ChatMemory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LlmService {

    private final RestClient restClient;
    private final RealEstateTools realEstateTools;
    private final PropertySearchContext searchContext;
    private final ChatMemory chatMemory;

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;

    public LlmService(RestClient.Builder restClientBuilder, RealEstateTools realEstateTools,
            PropertySearchContext searchContext, ChatMemory chatMemory) {
        this.restClient = restClientBuilder.build();
        this.realEstateTools = realEstateTools;
        this.searchContext = searchContext;
        this.chatMemory = chatMemory;
    }

    public String chat(String message, String conversationId) {
        // 1. Tool / System Instruction Logic
        String systemInstruction;
        String toolResult = "";

        // Trigger search for specific keywords or if the user asks for it
        if (message.contains("찾아줘") || message.contains("검색해줘") || message.contains("알려줘") || message.contains("구")
                || message.contains("아파트") || message.contains("매물")) {
            String extractedKeyword = extractSearchKeyword(message);
            if (extractedKeyword != null && !extractedKeyword.isEmpty() && !extractedKeyword.equalsIgnoreCase("null")) {
                toolResult = realEstateTools.searchProperties(extractedKeyword);
            }
        }

        systemInstruction = """
                You are a helpful real estate assistant.

                ROLE AND RESPONSE FORMAT:
                You MUST output your response in strict JSON format.
                The JSON must have the following structure:
                {
                    "message": "Your natural language response here. Summarize the listings if available, or answer the user's question.",
                    "listings": [ ... ] // Array of house info objects. If listings are provided in the context below, COPY them here. If not, use your best knowledge or leave empty.
                }

                CONTEXT DATA:
                The following is the raw data found in the database for the user's query.
                [DATA START]
                %s
                [DATA END]

                CRITICAL INSTRUCTION FOR "listings":
                1. If [DATA] is NOT empty, you MUST use it to populate the 'listings' field.
                2. If [DATA] IS EMPTY "[]", but you are recommending specific apartments in your "message" (e.g. from your internal knowledge), you **MUST** construct JSON objects for them in the 'listings' array.
                   - Fill 'aptNm' with the name you recommended.
                   - Fill 'aptSeq' with a placeholder like "manual_1".
                   - Fill 'umdNm' or 'sggCd' with the location.
                   - Leave other fields null if unknown.
                3. The 'listings' array MUST NOT be empty if your message recommends specific properties.
                """
                .formatted(toolResult.isEmpty() ? "[]" : toolResult);

        // 2. Build Messages List
        List<Map<String, Object>> messages = new java.util.ArrayList<>();

        // Add System Message
        if (systemInstruction != null && !systemInstruction.isEmpty()) {
            Map<String, Object> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemInstruction);
            messages.add(systemMsg);
        }

        // Add History
        List<Message> history = chatMemory.get(conversationId, 10);
        for (Message msg : history) {
            String role = msg.getMessageType().getValue().toLowerCase();
            String content = msg.getText();

            if (content != null && !content.isEmpty()) {
                Map<String, Object> historyMsg = new HashMap<>();
                historyMsg.put("role", role);
                historyMsg.put("content", content);
                messages.add(historyMsg);
            }
        }

        // Add Current User Message
        Map<String, Object> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", message);
        messages.add(userMsg);

        // Build Final Request Body using ObjectMapper to handle escaping correctly
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        String requestBody;
        try {
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("model", "gpt-5");
            requestMap.put("messages", messages);
            requestMap.put("temperature", 1.0);
            requestMap.put("stream", false);

            requestBody = mapper.writeValueAsString(requestMap);
        } catch (Exception e) {
            throw new RuntimeException("Failed to construct JSON body", e);
        }

        String endpoint = baseUrl + "/v1/chat/completions";

        Map<String, Object> response = restClient.post()
                .uri(endpoint)
                .header("User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept", "*/*")
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        String aiResponse = "No response from AI";
        if (response != null && response.containsKey("choices")) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (!choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageObj = (Map<String, Object>) firstChoice.get("message");
                aiResponse = (String) messageObj.get("content");
            }
        }

        // 3. Save to Memory
        chatMemory.add(conversationId, new UserMessage(message));
        chatMemory.add(conversationId, new AssistantMessage(aiResponse));

        return aiResponse;
    }

    private String extractSearchKeyword(String message) {
        try {
            String prompt = """
                    Analyze the user's sentence and extract ONLY the South Korean administrative district name (e.g., 'Seoul', 'Gangnam-gu', 'Yeoksam-dong').
                    Ignore all other words like 'apartment', 'find', 'three', 'price', 'list', etc.
                    Return ONLY the location name in Korean.

                    Examples:
                    "강남 아파트 3개" -> "강남"
                    "서초구 매물 찾아줘" -> "서초구"
                    "역삼동 빌라" -> "역삼동"
                    "Find apartments in Daegu" -> "대구"

                    If no specific location is found, output 'null'.
                    User Sentence: "%s"
                    """
                    .formatted(message);

            String requestBody = """
                    {
                        "model": "gpt-5",
                        "messages": [
                            {
                                "role": "user",
                                "content": "%s"
                            }
                        ],
                        "temperature": 0.0,
                        "stream": false
                    }
                    """.formatted(prompt.replace("\n", "\\n").replace("\"", "\\\""));

            String endpoint = baseUrl + "/v1/chat/completions";
            Map<String, Object> response = restClient.post()
                    .uri(endpoint)
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> messageObj = (Map<String, Object>) choices.get(0).get("message");
                    String extracted = ((String) messageObj.get("content")).trim();
                    // Remove any trailing punctuation (quotes, periods)
                    return extracted.replaceAll("^['\"]+|['\"]+$", "").replace(".", "");
                }
            }
        } catch (Exception e) {
            // System.out.println("Extraction failed: " + e.getMessage());
        }
        return null; // Return null if extraction fails, so we don't search with garbage
    }
}
