package kr.co.api.backend.mapper.admin;

import kr.co.api.backend.dto.SurveyDTO;
import kr.co.api.backend.dto.SurveyOptionDTO;
import kr.co.api.backend.dto.SurveyQuestionDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SurveyMapper {

    int insertSurvey(SurveyDTO survey);

    int insertSurveyQuestion(SurveyQuestionDTO question);

    int insertSurveyOption(SurveyOptionDTO option);
}
