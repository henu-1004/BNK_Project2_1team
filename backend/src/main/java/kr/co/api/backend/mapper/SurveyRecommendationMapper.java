package kr.co.api.backend.mapper;

import kr.co.api.backend.dto.survey.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SurveyRecommendationMapper {
    Long selectLatestRespId(@Param("surveyId") Long surveyId, @Param("custCode") String custCode);

    List<SurveyResponseDetailDTO> selectResponseDetails(@Param("respId") Long respId);

    List<RecoCandidateDTO> selectRecoCandidates();

    int deleteRecoTop3(@Param("custCode") String custCode, @Param("surveyId") Long surveyId);

    int insertRecoTop3(@Param("rows") List<RecoTop3RowDTO> rows);

    List<RecoProductDTO> selectRecoTop3(@Param("custCode") String custCode, @Param("surveyId") Long surveyId);

    List<SimilarResponseOptionDTO> selectOtherResponseOptions(
            @Param("surveyId") Long surveyId,
            @Param("custCode") String custCode
    );

    List<SimilarPurchaseDTO> selectSimilarCustomerPurchases(@Param("custCodes") List<String> custCodes);
}
