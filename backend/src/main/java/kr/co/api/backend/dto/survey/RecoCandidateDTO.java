package kr.co.api.backend.dto.survey;

import lombok.Data;

@Data
public class RecoCandidateDTO {
    private String dpstId;
    private String dpstName;
    private String dpstInfo;
    private String dpstDescript;
    private String dpstCurrency;
    private Integer dpstType;
    private Integer dpstRateType;
    private String dpstPartWdrwYn;
    private String dpstAddPayYn;
    private String dpstAutoRenewYn;
    private Integer dpstPeriodType;
}
