package kr.co.api.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SurveyOptionDTO {

    private Long optId;
    private Long qId;
    private String optCode;
    private String optText;
    private String optValue;
    private Integer optOrder;
    private String isActive;
    private String createdBy;
    private String updatedBy;
}
