package kr.co.api.backend.dto.admin.survey;

import lombok.Data;

@Data
public class SurveyOptionRequest {
    private String optCode;
    private String optText;
    private String optValue;
    private Integer optOrder;
    private boolean active = true;
}
