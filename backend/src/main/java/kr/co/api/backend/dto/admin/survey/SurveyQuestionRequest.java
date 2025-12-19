package kr.co.api.backend.dto.admin.survey;

import lombok.Data;

import java.util.List;

@Data
public class SurveyQuestionRequest {
    private String qText;
    private String qType;
    private boolean required = true;
    private Integer maxSelect;
    private boolean active = true;
    private List<SurveyOptionRequest> options;
}
