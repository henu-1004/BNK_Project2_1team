package kr.co.api.backend.service.admin;

import kr.co.api.backend.dto.SurveyCreateRequestDTO;
import kr.co.api.backend.dto.SurveyDTO;
import kr.co.api.backend.dto.SurveyOptionDTO;
import kr.co.api.backend.dto.SurveyOptionRequestDTO;
import kr.co.api.backend.dto.SurveyQuestionDTO;
import kr.co.api.backend.dto.SurveyQuestionRequestDTO;
import kr.co.api.backend.mapper.admin.SurveyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SurveyService {

    private static final String DEFAULT_ADMIN = "admin";
    private final SurveyMapper surveyMapper;

    @Transactional
    public Long createSurvey(SurveyCreateRequestDTO request) {
        SurveyDTO survey = new SurveyDTO();
        survey.setTitle(request.getTitle());
        survey.setDescription(request.getDescription());
        survey.setIsActive(resolveFlag(request.getIsActive(), "Y"));
        survey.setCreatedBy(DEFAULT_ADMIN);
        survey.setUpdatedBy(DEFAULT_ADMIN);
        surveyMapper.insertSurvey(survey);

        Long surveyId = survey.getSurveyId();
        List<SurveyQuestionRequestDTO> questions = request.getQuestions();
        if (questions == null || questions.isEmpty()) {
            return surveyId;
        }

        int qNo = 1;
        for (SurveyQuestionRequestDTO questionRequest : questions) {
            SurveyQuestionDTO question = new SurveyQuestionDTO();
            question.setSurveyId(surveyId);
            question.setQNo(qNo);
            question.setQKey("Q" + qNo);
            question.setQText(questionRequest.getText());
            question.setQType(questionRequest.getType());
            question.setIsRequired(resolveFlag(questionRequest.getIsRequired(), "Y"));
            question.setMaxSelect(resolveMaxSelect(questionRequest));
            question.setIsActive("Y");
            question.setCreatedBy(DEFAULT_ADMIN);
            question.setUpdatedBy(DEFAULT_ADMIN);
            surveyMapper.insertSurveyQuestion(question);

            Long qId = question.getQId();
            if (qId != null) {
                insertOptions(qId, questionRequest.getOptions());
            }
            qNo++;
        }
        return surveyId;
    }

    private Integer resolveMaxSelect(SurveyQuestionRequestDTO request) {
        if (!"MULTI".equalsIgnoreCase(request.getType())) {
            return null;
        }
        return request.getMaxSelect();
    }

    private void insertOptions(Long qId, List<SurveyOptionRequestDTO> options) {
        if (options == null || options.isEmpty()) {
            return;
        }
        for (SurveyOptionRequestDTO optionRequest : options) {
            SurveyOptionDTO option = new SurveyOptionDTO();
            option.setQId(qId);
            option.setOptCode(optionRequest.getOptCode());
            option.setOptText(optionRequest.getOptText());
            option.setOptValue(optionRequest.getOptValue());
            option.setOptOrder(optionRequest.getOptOrder());
            option.setIsActive("Y");
            option.setCreatedBy(DEFAULT_ADMIN);
            option.setUpdatedBy(DEFAULT_ADMIN);
            surveyMapper.insertSurveyOption(option);
        }
    }

    private String resolveFlag(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }
}
