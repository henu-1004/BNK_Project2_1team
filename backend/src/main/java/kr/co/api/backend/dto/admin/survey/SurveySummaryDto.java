package kr.co.api.backend.dto.admin.survey;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SurveySummaryDto {
    private Long surveyId;
    private String title;
    private String description;
    private String isActive;
    private Integer questionCount;
    private LocalDateTime updatedAt;
}
