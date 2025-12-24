package kr.co.api.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SurveyOptionRequestDTO {

    private String optCode;
    private String optText;
    private String optValue;
    private Integer optOrder;
}
