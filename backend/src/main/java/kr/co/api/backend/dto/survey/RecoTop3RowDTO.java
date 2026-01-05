package kr.co.api.backend.dto.survey;

import lombok.Data;

@Data
public class RecoTop3RowDTO {
    private String custCode;
    private Long surveyId;
    private Integer rankNo;
    private String productId;
}
