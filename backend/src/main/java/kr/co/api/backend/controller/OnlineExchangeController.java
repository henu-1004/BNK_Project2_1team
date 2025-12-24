package kr.co.api.backend.controller;

import kr.co.api.backend.dto.FrgnExchOnlineDTO;
import kr.co.api.backend.dto.RateDTO;
import kr.co.api.backend.jwt.CustomUserDetails;
import kr.co.api.backend.service.OnlineExchangeService;
import kr.co.api.backend.service.RateQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mobile/exchange")
@RequiredArgsConstructor
public class OnlineExchangeController {

    private final OnlineExchangeService onlineExchangeService;
    private final RateQueryService rateQueryService;

    /**
     * 환율 목록 (통화별 최신 1건)
     */
    @GetMapping("/rates")
    public List<RateDTO> getRates() {
        return rateQueryService.getLatestRates();
    }

    /**
     * 특정 통화 환율 히스토리
     */
    @GetMapping("/rates/{currency}")
    public List<RateDTO> getRateHistory(
            @PathVariable String currency
    ) {
        return rateQueryService.getRateHistory(currency);
    }

    /**
     * 온라인 환전 요청
     */
    @PostMapping("/online")
    public ResponseEntity<?> onlineExchange(
            @RequestBody FrgnExchOnlineDTO dto,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        String userId = authentication.getName(); // 여기 하나로 끝
        onlineExchangeService.processOnlineExchange(dto, userId);

        return ResponseEntity.ok("온라인 환전이 정상적으로 처리되었습니다.");
    }





    @GetMapping("/accounts")
    public ResponseEntity<?> getMyExchangeAccounts(
            @RequestParam String currency,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        Object principal = authentication.getPrincipal();

        String userId;
        if (principal instanceof CustomUserDetails user) {
            userId = user.getUsername();
        } else if (principal instanceof String s) {
            // principal이 String으로 들어오는 케이스 대응
            userId = s;
            if ("anonymousUser".equals(userId)) {
                return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
            }
        } else {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        Map<String, Object> result = onlineExchangeService.getMyExchangeAccounts(userId, currency);
        return ResponseEntity.ok(result);
    }






}
