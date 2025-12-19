package kr.co.api.backend.dto.admin.survey;

import lombok.Data;

@Data
public class SurveyOptionRecord {
    private Long optId;
    private Long qId;
    private String optCode;
    private String optText;
    private String optValue;
    private Integer optOrder;
    private String isActive;
    private String createdBy;
}
