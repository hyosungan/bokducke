package ssafy.bokduck.dto;

import java.time.LocalDateTime;

public class LlmRequestDto {
    private Long llmRequestId;
    private Long userId;
    private String aptSeq;
    private String type; // SCENERY / FIND_HOUSE
    private String prompt;
    private String response;
    private String conditionJson;
    private LocalDateTime createdAt;

    public LlmRequestDto() {
    }

    public Long getLlmRequestId() {
        return llmRequestId;
    }

    public void setLlmRequestId(Long llmRequestId) {
        this.llmRequestId = llmRequestId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAptSeq() {
        return aptSeq;
    }

    public void setAptSeq(String aptSeq) {
        this.aptSeq = aptSeq;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getConditionJson() {
        return conditionJson;
    }

    public void setConditionJson(String conditionJson) {
        this.conditionJson = conditionJson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "LlmRequestDto{" +
                "llmRequestId=" + llmRequestId +
                ", userId=" + userId +
                ", type='" + type + '\'' +
                '}';
    }
}
