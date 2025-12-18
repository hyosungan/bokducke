package ssafy.bokduck.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import ssafy.bokduck.dto.LlmRequestDto;
import ssafy.bokduck.mapper.LlmMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Service
@Transactional
public class LlmService {

    private final LlmMapper llmMapper;
    private final RestTemplate restTemplate;

    @Value("${llm.api-key}")
    private String apiKey;

    @Value("${llm.model}")
    private String modelName;

    // SSAFY Proxy URL
    private static final String GEMINI_BASE_URL = "https://gms.ssafy.io/gmsapi/generativelanguage.googleapis.com/v1beta/models/";

    public LlmService(LlmMapper llmMapper) {
        this.llmMapper = llmMapper;
        this.restTemplate = new RestTemplate();
    }

    public String processRequest(LlmRequestDto requestDto) {
        String responseText = callGemini(requestDto.getPrompt());

        requestDto.setResponse(responseText);
        llmMapper.insertRequest(requestDto);

        return responseText;
    }

    private String callGemini(String prompt) {
        String url = GEMINI_BASE_URL + modelName + ":generateContent?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Note: Gemini usually uses key as query param, not Bearer token.
        // If it's a proxy that behaves like OpenAI, we would use Bearer.
        // Assuming standard Gemini API for now.

        // Request Body: { "contents": [{ "parts": [{ "text": "..." }] }] }
        Map<String, String> part = new HashMap<>();
        part.put("text", prompt);

        List<Map<String, String>> parts = new ArrayList<>();
        parts.add(part);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", parts);

        List<Map<String, Object>> contents = new ArrayList<>();
        contents.add(content);

        Map<String, Object> body = new HashMap<>();
        body.put("contents", contents);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            // Using Map<String, Object> to avoid raw type warnings
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null) {
                // Parse: candidates[0].content.parts[0].text
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> firstCandidate = candidates.get(0);
                    Map<String, Object> candidateContent = (Map<String, Object>) firstCandidate.get("content");
                    if (candidateContent != null) {
                        List<Map<String, Object>> resParts = (List<Map<String, Object>>) candidateContent.get("parts");
                        if (resParts != null && !resParts.isEmpty()) {
                            return (String) resParts.get(0).get("text");
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error calling Gemini: " + e.getMessage();
        }
        return "No response from Gemini";
    }
}
