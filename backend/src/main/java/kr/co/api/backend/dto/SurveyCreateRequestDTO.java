package kr.co.api.backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SurveyCreateRequestDTO {

    private String title;
    private String description;
    private String isActive;
    private List<SurveyQuestionRequestDTO> questions;
}
