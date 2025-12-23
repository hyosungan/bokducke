package ssafy.bokduck.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ssafy.bokduck.dto.LlmRequestDto;
import ssafy.bokduck.service.LlmService;

@RestController
@RequestMapping("/api/v1/llm")
public class LlmController {

    private final LlmService llmService;
    private final ssafy.bokduck.context.PropertySearchContext searchContext;
    private final ssafy.bokduck.security.JwtTokenProvider tokenProvider;

    public LlmController(LlmService llmService,
            ssafy.bokduck.context.PropertySearchContext searchContext,
            ssafy.bokduck.security.JwtTokenProvider tokenProvider) {
        this.llmService = llmService;
        this.searchContext = searchContext;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping
    public ResponseEntity<java.util.Map<String, Object>> chat(@RequestBody LlmRequestDto requestDto,
            java.security.Principal principal) {
        try {
            Authentication authentication = (Authentication) principal;
            String token = (String) authentication.getCredentials();
            java.util.Date expiration = tokenProvider.getExpiration(token);

            // Generate conversationID based on UserID + Expiration Time
            // This ensures meaningful conversation continuity within the same session (JWT
            // validity period)
            // and creates a fresh conversation when a new token is issued (re-login).
            String conversationId = principal.getName() + "_" + expiration.getTime();

            String responseJson = llmService.chat(requestDto.getPrompt(), conversationId);

            // Parse the JSON response from LLM
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.Map<String, Object> response;
            try {
                response = mapper.readValue(responseJson, java.util.Map.class);
            } catch (Exception e) {
                // Fallback if LLM didn't return valid JSON
                response = new java.util.HashMap<>();
                response.put("message", responseJson);
                response.put("listings", java.util.Collections.emptyList());
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            java.util.Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("error", "SERVER ERROR: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
