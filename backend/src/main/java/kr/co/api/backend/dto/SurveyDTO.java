package kr.co.api.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SurveyDTO {

    private Long surveyId;
    private String title;
    private String description;
    private String isActive;
    private String createdBy;
    private String updatedBy;
}
