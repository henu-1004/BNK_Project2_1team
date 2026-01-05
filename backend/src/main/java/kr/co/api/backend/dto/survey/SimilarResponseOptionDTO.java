package kr.co.api.backend.dto.survey;

import lombok.Data;

@Data
public class SimilarResponseOptionDTO {
    private Long respId;
    private String custCode;
    private Long optId;
}
