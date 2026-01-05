package kr.co.api.backend.dto.survey;

import lombok.Data;

import java.util.List;

@Data
public class SurveyRecommendationResponseDTO {
    private Long surveyId;
    private String custCode;
    private List<RecoProductDTO> products;
}
