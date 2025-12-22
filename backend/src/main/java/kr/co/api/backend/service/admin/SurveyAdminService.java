package kr.co.api.backend.service.admin;

import kr.co.api.backend.dto.admin.survey.*;
import kr.co.api.backend.mapper.admin.SurveyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class SurveyAdminService {

    private final SurveyMapper surveyMapper;

    @Transactional
    public Long createSurvey(SurveyCreateRequest request) {
        validateRequest(request);

        SurveyRecord surveyRecord = new SurveyRecord();
        surveyRecord.setTitle(request.getTitle());
        surveyRecord.setDescription(request.getDescription());
        surveyRecord.setIsActive(request.isActive() ? "Y" : "N");
        surveyRecord.setCreatedBy(StringUtils.hasText(request.getCreatedBy()) ? request.getCreatedBy() : "ADMIN");
        surveyRecord.setUpdatedBy(StringUtils.hasText(request.getUpdatedBy()) ? request.getUpdatedBy() : "ADMIN");
        surveyMapper.insertSurvey(surveyRecord);

        Long surveyId = surveyRecord.getSurveyId();

        int qNo = 1;
        for (SurveyQuestionRequest question : request.getQuestions()) {
            SurveyQuestionRecord questionRecord = new SurveyQuestionRecord();
            questionRecord.setSurveyId(surveyId);
            questionRecord.setQNo(qNo);
            questionRecord.setQKey("Q" + qNo);
            questionRecord.setQText(question.getQText());
            questionRecord.setQType(question.getQType().toUpperCase(Locale.ROOT));
            questionRecord.setIsRequired(question.isRequired() ? "Y" : "N");
            questionRecord.setMaxSelect(
                    "MULTI".equalsIgnoreCase(question.getQType()) ? question.getMaxSelect() : null
            );
            questionRecord.setIsActive(question.isActive() ? "Y" : "N");
            questionRecord.setCreatedBy(surveyRecord.getCreatedBy());
            questionRecord.setUpdatedBy(surveyRecord.getUpdatedBy());
            surveyMapper.insertQuestion(questionRecord);

            if (!"TEXT".equalsIgnoreCase(question.getQType())) {
                insertOptions(question, questionRecord, surveyRecord.getCreatedBy());
            }
            qNo++;
        }

        return surveyId;
    }

    public List<SurveySummaryDto> getSurveySummaries() {
        return surveyMapper.findSurveySummaries();
    }

    private void insertOptions(SurveyQuestionRequest question, SurveyQuestionRecord questionRecord, String createdBy) {
        List<SurveyOptionRequest> options = question.getOptions() != null ? question.getOptions() : new ArrayList<>();
        int order = 1;
        for (SurveyOptionRequest option : options) {
            SurveyOptionRecord optionRecord = new SurveyOptionRecord();
            optionRecord.setQId(questionRecord.getQId());
            optionRecord.setOptCode(StringUtils.hasText(option.getOptCode()) ? option.getOptCode() : toOptionCode(order));
            optionRecord.setOptText(option.getOptText());
            optionRecord.setOptValue(option.getOptValue());
            optionRecord.setOptOrder(option.getOptOrder() != null ? option.getOptOrder() : order);
            optionRecord.setIsActive(option.isActive() ? "Y" : "N");
            optionRecord.setCreatedBy(createdBy);
            surveyMapper.insertOption(optionRecord);
            order++;
        }
    }

    private void validateRequest(SurveyCreateRequest request) {
        if (!StringUtils.hasText(request.getTitle())) {
            throw new IllegalArgumentException("설문 제목을 입력해 주세요.");
        }
        if (request.getQuestions() == null || request.getQuestions().isEmpty()) {
            throw new IllegalArgumentException("최소 1개 이상의 문항을 등록해 주세요.");
        }

        for (SurveyQuestionRequest question : request.getQuestions()) {
            if (!StringUtils.hasText(question.getQText())) {
                throw new IllegalArgumentException("모든 문항에 질문 내용을 입력해 주세요.");
            }
            if (!StringUtils.hasText(question.getQType())) {
                throw new IllegalArgumentException("문항 타입을 선택해 주세요.");
            }
            if ("MULTI".equalsIgnoreCase(question.getQType()) && (question.getMaxSelect() == null || question.getMaxSelect() < 1)) {
                throw new IllegalArgumentException("복수 선택 문항의 최대 선택 수를 입력해 주세요.");
            }
            if (!"TEXT".equalsIgnoreCase(question.getQType())) {
                if (question.getOptions() == null || question.getOptions().isEmpty()) {
                    throw new IllegalArgumentException("객관식 문항에는 선택지를 추가해 주세요.");
                }
                for (SurveyOptionRequest option : question.getOptions()) {
                    if (!StringUtils.hasText(option.getOptText())) {
                        throw new IllegalArgumentException("모든 선택지에 표시 텍스트를 입력해 주세요.");
                    }
                }
            }
        }
    }

    private String toOptionCode(int order) {
        int base = (order - 1) % 26;
        return String.valueOf((char) ('A' + base));
    }
}
