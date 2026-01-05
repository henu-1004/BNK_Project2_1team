package kr.co.api.backend.dto.survey;

import lombok.Data;

@Data
public class RecoProductDTO {
    private Integer rankNo;
    private String dpstId;
    private String dpstName;
    private String dpstInfo;
    private String dpstCurrency;
    private String tag;
}
