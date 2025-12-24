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
        int requestedLimit = 10; // Default

        // 0. Fetch History for Context
        List<Message> history = chatMemory.get(conversationId, 10);

        // Trigger search
        if (message.contains("찾아줘") || message.contains("검색해줘") || message.contains("알려줘") || message.contains("구")
                || message.contains("아파트") || message.contains("매물") || message.contains("더")
                || message.contains("보여줘")) {
            Map<String, Object> extractionResult = extractSearchKeyword(message, history);
            if (extractionResult != null && extractionResult.get("location") != null) {
                String location = (String) extractionResult.get("location");
                int limit = 10;
                if (extractionResult.get("limit") != null) {
                    if (extractionResult.get("limit") instanceof Integer) {
                        limit = (Integer) extractionResult.get("limit");
                    } else if (extractionResult.get("limit") instanceof String) {
                        try {
                            limit = Integer.parseInt((String) extractionResult.get("limit"));
                        } catch (NumberFormatException e) {
                            limit = 10;
                        }
                    }
                }
                requestedLimit = limit; // Update requestedLimit for system instruction

                if (!"null".equalsIgnoreCase(location)) {
                    // HYBRID SEARCH STRATEGY:
                    // Fetch a larger pool candidates (max 50) - 100 was causing 502 Bad Gateway
                    int candidateLimit = Math.max(limit, 50);
                    toolResult = realEstateTools.searchProperties(location, candidateLimit);
                }
            }
        }

        // 1. Prepare Context and System Prompt
        systemInstruction = """
                You are a helpful real estate assistant.

                ROLE AND RESPONSE FORMAT:
                You MUST output your response in strict JSON format.
                The JSON must have the following structure:
                {
                    "message": "Your natural language response here. Summarize the listings if available, or answer the user's question.",
                    "listings": [ ... ] // Array of house info objects.
                }

                CONTEXT DATA:
                The following is the raw data found in the database for the user's query.
                [DATA START]
                %s
                [DATA END]

                CRITICAL INSTRUCTION:
                1. The user may have specific qualitative criteria (e.g. "near elementary school", "quiet area", "good view").
                2. Use your **INTERNAL KNOWLEDGE** about the apt names and locations provided in [DATA] to select the best matches.
                3. **FILTERING**: Even if [DATA] has many items, ONLY return the ones that best match the user's request.
                4. **LIMIT**: The user requested approximately %d items (implied or default). Try to return close to this number of best matches.

                **CRITICAL HANDLING OF EMPTY MATCHES:**
                5. **CASE A: NO DATA IN DB** (Context [DATA] is "[]" or debug info says listings=0):
                   - If [DATA] is empty, it means the requested region is NOT supported in our database.
                   - Return `listings: []` (Empty Array).
                   - In "message", explain: "죄송하지만 요청하신 지역의 데이터가 저희 DB에 없습니다. (지원: 서울, 부산 등)"
                   - **HALLUCINATION BLOCK**: Do NOT mention "Seogwipo", "Jeju", or any other region unless the user explicitly asked for it. Just say the requested data is missing.

                6. **CASE B: DATA EXISTS BUT FILTERED** (Context [DATA] has items, but none match criteria):
                   - If [DATA] is NOT empty, but no item matches specific criteria (e.g. price < 100M), you MUST **RELAX THE CONSTRAINTS**.
                   - Select the closest available options from [DATA].
                   - **NEVER** return an empty `listings` array in this case.
                   - **NEVER** use invalid keys like "note" in the `listings` objects. Use standard fields (`aptNm`, `latestDealAmount`, etc.).
                """
                .formatted(toolResult.isEmpty() ? "[]" : toolResult, requestedLimit); // Use requestedLimit here

        // 2. Build Messages List
        List<Map<String, Object>> messages = new java.util.ArrayList<>();

        // Add System Message
        if (systemInstruction != null && !systemInstruction.isEmpty()) {
            Map<String, Object> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemInstruction);
            messages.add(systemMsg);
        }

        // Add History to Messages
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
        // few shot용
        Map<String, Object> exampleUser1 = new HashMap<>();
        exampleUser1.put("role", "user");
        exampleUser1.put("content", "부산광역시 해운대구의 아파트 2개 추천해줘");
        messages.add(exampleUser1);
        Map<String, Object> exampleAssistant1 = new HashMap<>();
        exampleAssistant1.put("role", "assistant");

        String exampleJson = """
                {
                    "message": "부산광역시 해운대구의 대표적인 아파트 2곳을 추천해 드립니다.",
                    "listings": [
                {
                    "aptSeq": "26350-99",
                    "sggCd": "26350",
                    "umdCd": "10600",
                    "umdNm": "중동",
                    "jibun": "1763",
                    "roadNmSggCd": "26350",
                    "roadNm": "해운대해변로",
                    "roadNmBonbun": "394",
                    "roadNmBubun": "0",
                    "aptNm": "해운대동일",
                    "buildYear": 1998,
                    "latitude": "35.1652743714169",
                    "longitude": "129.171178199358",
                    "latestDealAmount": "46,000",
                    "latestDealYear": 2025,
                    "latestDealMonth": 2,
                    "latestDealDay": 14,
                    "latestDealFloor": "18",
                    "latestDealArea": 84.9
                },
                {
                    "aptSeq": "26350-9",
                    "sggCd": "26350",
                    "umdCd": "10500",
                    "umdNm": "우동",
                    "jibun": "938",
                    "roadNmSggCd": "26350",
                    "roadNm": "해운대로483번길",
                    "roadNmBonbun": "10",
                    "roadNmBubun": "0",
                    "aptNm": "롯데",
                    "buildYear": 1993,
                    "latitude": "35.1634441587193",
                    "longitude": "129.147619699842",
                    "latestDealAmount": "57,300",
                    "latestDealYear": 2025,
                    "latestDealMonth": 2,
                    "latestDealDay": 11,
                    "latestDealFloor": "2",
                    "latestDealArea": 84.84
                }
                ]
                }
                """;
        exampleAssistant1.put("content", exampleJson);
        messages.add(exampleAssistant1);

        Map<String, Object> exampleUser2 = new HashMap<>();
        exampleUser2.put("role", "user");
        exampleUser2.put("content", "서울특별시 종로구 청운동 아파트 1개 보여줘");
        messages.add(exampleUser2);

        Map<String, Object> exampleAssistant2 = new HashMap<>();
        exampleAssistant2.put("role", "assistant");
        String exampleJson2 = """
                {
                    "message": "서울특별시 종로구 청운동의 아파트 한 곳을 추천 드릴게요. 자하문로33길 43에 위치한 청운현대 apt 입니다.",
                    "listings": [
                        {
                            "aptSeq": "11110-4",
                            "sggCd": "11110",
                            "umdCd": "10100",
                            "umdNm": "청운동",
                            "jibun": "56-45",
                            "roadNmSggCd": "11110",
                            "roadNm": "자하문로33길",
                            "roadNmBonbun": "43",
                            "roadNmBubun": "0",
                            "aptNm": "청운현대",
                            "buildYear": 2000,
                            "latitude": "37.5861486417138",
                            "longitude": "126.966930414705",
                            "latestDealAmount": "57,300",
                            "latestDealYear": 2023,
                            "latestDealMonth": 8,
                            "latestDealDay": 29,
                            "latestDealFloor": "4",
                            "latestDealArea": 103.67
                        }
                    ]
                }
                """;
        exampleAssistant2.put("content", exampleJson2);
        messages.add(exampleAssistant2);

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
            requestMap.put("model", "gpt-5.2");
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

    private Map<String, Object> extractSearchKeyword(String message, List<Message> history) {
        try {
            // Build history string for context
            StringBuilder historyContext = new StringBuilder();
            if (history != null && !history.isEmpty()) {
                historyContext.append("\nPREVIOUS CONVERSATION:\n");
                for (Message msg : history) {
                    historyContext.append(msg.getMessageType().getValue()).append(": ").append(msg.getText())
                            .append("\n");
                }
            }

            String prompt = """
                    Analyze the user's sentence to determine the target real estate location in South Korea and the requested number of items.

                    RULES:
                    1. If the user mentions a specific administrative name (Dong, Gu, Si), extract it. (Priority: Dong > Gu > Si).
                    2. If the user describes a location (e.g., "near the ocean in Busan"), INFER the most representative administrative district name.
                       - **LANDMARK INFERENCE**: If the user mentions a specific building or landmark (e.g. "Multicampus", "COEX"), USE YOUR KNOWLEDGE to find its administrative district (Dong/Gu) and output THAT (e.g. "Multicampus" -> "Yeoksam-dong").
                    3. Do NOT output composite names like "Busan Haeundae-gu". Output ONLY the most specific part (e.g., "Haeundae-gu").
                       - **Normalization**: If the location is a district (Gu), MUST append "-gu" (e.g., "Gangnam" -> "Gangnam-gu").
                       - **POI REMOVAL**: After inferring the location, output ONLY the administrative name from the landmark. (e.g., "Yeoksam-dong Multicampus" -> "Yeoksam-dong").
                    4. Extract the numeric quantity requested. If not specified, default to 10.
                    5. Return a strict JSON object: {"location": "...", "limit": 5}
                    6. **CONTEXT AWARENESS**: Check 'PREVIOUS CONVERSATION'. If the user says "more", "next", "continue", or similar WITHOUT specifying a location, REUSE the location from the previous turn.
                    7. If absolutely no location can be inferred (neither from current nor history), set location to null.

                    Examples:
                    "강남 아파트 3개" -> {"location": "강남구", "limit": 3}
                    "더 보여줘" (History: User asked for Gangnam) -> {"location": "강남구", "limit": 10}
                    "부산 바다랑 가까운 곳" -> {"location": "해운대구", "limit": 10}
                    "낙성대역 근처" -> {"location": "낙성대동", "limit": 10}
                    "울산시 전체" -> {"location": "울산광역시", "limit": 10}
                    "설악산 근처 펜션형 아파트" -> {"location": "속초시", "limit": 10}
                    "역삼동 멀티캠퍼스 주변" -> {"location": "역삼동", "limit": 10}
                    "멀티캠퍼스" -> {"location": "역삼동", "limit": 10}
                    "롯데월드타워" -> {"location": "잠실동", "limit": 10}

                    %s

                    User Sentence: "%s"
                    """
                    .formatted(historyContext.toString(), message);

            String requestBody = """
                    {
                        "model": "gpt-5.2",
                        "messages": [
                            {
                                "role": "user",
                                "content": "%s"
                            }
                        ],
                        "temperature": 0.0,
                        "stream": false,
                         "response_format": { "type": "json_object" }
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
                    String content = (String) messageObj.get("content");

                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    return mapper.readValue(content, Map.class);
                }
            }
        } catch (Exception e) {
            // System.out.println("Extraction failed: " + e.getMessage());
        }
        return null;
    }
}
