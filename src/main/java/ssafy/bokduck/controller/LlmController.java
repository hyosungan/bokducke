package ssafy.bokduck.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ssafy.bokduck.dto.LlmRequestDto;
import ssafy.bokduck.service.LlmService;

@RestController
@RequestMapping("/api/v1/llm")
public class LlmController {

    private final LlmService llmService;

    public LlmController(LlmService llmService) {
        this.llmService = llmService;
    }

    @PostMapping
    public ResponseEntity<String> chat(@RequestBody LlmRequestDto requestDto) {
        try {
            String response = llmService.processRequest(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            e.printStackTrace(pw);
            return ResponseEntity.status(500).body("SERVER ERROR: " + e.getMessage() + "\n" + sw.toString());
        }
    }
}
