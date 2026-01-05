package kr.co.api.backend.dto.survey;

import lombok.Data;

@Data
public class SimilarPurchaseDTO {
    private String custCode;
    private String productId;
    private Integer cnt;
}
