package kr.co.api.backend.service.admin;

import kr.co.api.backend.dto.admin.survey.SurveySaveDTO;
import kr.co.api.backend.dto.admin.survey.SurveySummaryDTO;
import kr.co.api.backend.mapper.admin.SurveyAdminMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SurveyAdminService {

    private final SurveyAdminMapper surveyAdminMapper;

    public List<SurveySummaryDTO> getSurveys() {
        return surveyAdminMapper.selectSurveyList();
    }

    public SurveySummaryDTO getSurveyById(Long surveyId) {
        return surveyAdminMapper.selectSurveyById(surveyId);
    }

    public SurveySummaryDTO createSurvey(SurveySaveDTO dto) {
        surveyAdminMapper.insertSurvey(dto);
        return surveyAdminMapper.selectSurveyById(dto.getSurveyId());
    }

    public SurveySummaryDTO updateSurvey(SurveySaveDTO dto) {
        surveyAdminMapper.updateSurvey(dto);
        return surveyAdminMapper.selectSurveyById(dto.getSurveyId());
    }
}
