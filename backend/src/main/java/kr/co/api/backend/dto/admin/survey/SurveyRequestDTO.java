package kr.co.api.backend.dto.admin.survey;

import lombok.Data;

@Data
public class SurveyRequestDTO {
    private String title;
    private String description;
    private String isActive;
    private String createdBy;
    private String updatedBy;
}
