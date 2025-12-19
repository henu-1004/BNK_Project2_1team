package kr.co.api.backend.dto.admin.survey;

import lombok.Data;

import java.util.List;

@Data
public class SurveyCreateRequest {
    private String title;
    private String description;
    private boolean active = true;
    private String createdBy;
    private String updatedBy;
    private List<SurveyQuestionRequest> questions;
}
