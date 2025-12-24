package kr.co.api.backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SurveyQuestionRequestDTO {

    private String text;
    private String type;
    private String isRequired;
    private Integer maxSelect;
    private List<SurveyOptionRequestDTO> options;
}
