package ssafy.bokduck.dto;

import java.time.LocalDateTime;

public class UserInterestDto {
    private Long interestId;
    private Long userId;
    private String interestType; // REGION / KEYWORD / PRICE_RANGE
    private String value;
    private LocalDateTime createdAt;

    public UserInterestDto() {
    }

    public Long getInterestId() {
        return interestId;
    }

    public void setInterestId(Long interestId) {
        this.interestId = interestId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getInterestType() {
        return interestType;
    }

    public void setInterestType(String interestType) {
        this.interestType = interestType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "UserInterestDto{" +
                "interestId=" + interestId +
                ", userId=" + userId +
                ", interestType='" + interestType + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
