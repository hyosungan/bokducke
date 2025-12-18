package ssafy.bokduck.dto;

import java.time.LocalDateTime;

public class PropertyScrapDto {
    private Long userId;
    private String aptSeq;
    private LocalDateTime createdAt;

    public PropertyScrapDto() {
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "PropertyScrapDto{" +
                "userId=" + userId +
                ", aptSeq='" + aptSeq + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
