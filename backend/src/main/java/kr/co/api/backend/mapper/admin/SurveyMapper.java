package kr.co.api.backend.mapper.admin;

import kr.co.api.backend.dto.admin.survey.SurveyOptionRecord;
import kr.co.api.backend.dto.admin.survey.SurveyQuestionRecord;
import kr.co.api.backend.dto.admin.survey.SurveyRecord;
import kr.co.api.backend.dto.admin.survey.SurveySummaryDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SurveyMapper {

    void insertSurvey(SurveyRecord surveyRecord);

    void insertQuestion(SurveyQuestionRecord questionRecord);

    void insertOption(SurveyOptionRecord optionRecord);

    List<SurveySummaryDto> findSurveySummaries();
}
