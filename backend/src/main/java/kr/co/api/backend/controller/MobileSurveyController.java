package kr.co.api.backend.controller;

import kr.co.api.backend.dto.survey.SurveyDetailResponseDTO;
import kr.co.api.backend.dto.survey.SurveyPrefillResponseDTO;
import kr.co.api.backend.dto.survey.SurveyRecommendationResponseDTO;
import kr.co.api.backend.dto.survey.SurveyResponseRequestDTO;
import kr.co.api.backend.service.SurveyRecommendationService;
import kr.co.api.backend.service.SurveyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mobile/surveys")
@RequiredArgsConstructor
public class MobileSurveyController {

    private final SurveyService surveyService;
    private final SurveyRecommendationService recommendationService;

    @GetMapping("/{surveyId}")
    public ResponseEntity<SurveyDetailResponseDTO> getSurvey(@PathVariable Long surveyId) {
        SurveyDetailResponseDTO detail = surveyService.getSurveyDetail(surveyId);
        if (detail == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(detail);
    }

    @PostMapping("/{surveyId}/responses")
    public ResponseEntity<Void> submitSurveyResponse(
            @PathVariable Long surveyId,
            @RequestBody SurveyResponseRequestDTO request
    ) {
        surveyService.submitSurveyResponse(surveyId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{surveyId}/recommendations")
    public ResponseEntity<SurveyRecommendationResponseDTO> getRecommendations(
            @PathVariable Long surveyId,
            @RequestParam String custCode
    ) {
        return ResponseEntity.ok(recommendationService.getRecommendations(custCode, surveyId));
    }

    @GetMapping("/{surveyId}/recommendations/{productId}/prefill")
    public ResponseEntity<SurveyPrefillResponseDTO> getPrefill(
            @PathVariable Long surveyId,
            @PathVariable String productId,
            @RequestParam String custCode
    ) {
        return ResponseEntity.ok(recommendationService.buildPrefill(custCode, surveyId, productId));
    }

    @PostMapping("/{surveyId}/responses/_debug")
    public ResponseEntity<Object> debug(
            @PathVariable Long surveyId,
            @RequestBody Map<String, Object> body
    ) {
        // 여기 찍히는 게 "Spring이 실제로 받은 JSON"이다
        System.out.println("==== RAW BODY ====");
        System.out.println(body);

        Object answers = body.get("answers");
        System.out.println("==== answers ====");
        System.out.println(answers);

        return ResponseEntity.ok(body);
    }

}
