package kr.co.api.backend.controller.admin;

import kr.co.api.backend.dto.SurveyCreateRequestDTO;
import kr.co.api.backend.service.admin.SurveyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin") // 이 컨트롤러의 기본 URL 경로
@RequiredArgsConstructor
public class AdminAiProductController {

    private final SurveyService surveyService;

    /**
     * AI 상품 추천 관리 메인 페이지
     * 요청 URL: /admin/ai-product
     */
    @GetMapping("/ai-product")
    public String aiProductList(Model model) {

        // 1. 사이드바의 'AI 상품추천' 탭을 활성화(active) 시키기 위한 식별자
        // (sidebar.html에서 ${menu} == 'ai-product' 조건을 체크함)
        model.addAttribute("menu", "ai-product");

        // 2. 페이지 상단 타이틀 전달 (admin_template.html에서 사용)
        model.addAttribute("pageTitle", "AI 상품 추천 관리");

        // 3. 실제 보여줄 HTML 파일 경로 (templates/admin/ai_product.html)
        return "admin/ai_product";
    }

    @PostMapping("/ai-product/surveys")
    @ResponseBody
    public Map<String, Object> createSurvey(@RequestBody SurveyCreateRequestDTO request) {
        Long surveyId = surveyService.createSurvey(request);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("surveyId", surveyId);
        return response;
    }
}
