package kr.co.api.backend.controller.admin;

import kr.co.api.backend.dto.admin.survey.SurveyCreateRequest;
import kr.co.api.backend.dto.admin.survey.SurveySummaryDto;
import kr.co.api.backend.service.admin.SurveyAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/api/surveys")
public class SurveyAdminController {

    private final SurveyAdminService surveyAdminService;

    @GetMapping
    public List<SurveySummaryDto> getSurveys() {
        return surveyAdminService.getSurveySummaries();
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createSurvey(@RequestBody SurveyCreateRequest request) {
        Long surveyId = surveyAdminService.createSurvey(request);
        Map<String, Object> response = new HashMap<>();
        response.put("surveyId", surveyId);
        response.put("message", "설문이 저장되었습니다.");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(IllegalArgumentException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }
}
