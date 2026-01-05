package kr.co.api.backend.dto.survey;

import lombok.Data;

@Data
public class SurveyPrefillResponseDTO {
    private String productId;
    private String currency;
    private Integer amount;
    private Integer periodMonths;
    private String withdrawType;
    private String accountPreference;
}
