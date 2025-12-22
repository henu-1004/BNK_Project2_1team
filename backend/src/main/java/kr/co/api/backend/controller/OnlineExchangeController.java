package kr.co.api.backend.controller;

import kr.co.api.backend.dto.FrgnExchOnlineDTO;
import kr.co.api.backend.jwt.CustomUserDetails;
import kr.co.api.backend.service.OnlineExchangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/exchange")
@RequiredArgsConstructor
public class OnlineExchangeController {

    private final OnlineExchangeService onlineExchangeService;

    /**
     * 온라인 환전 요청
     */
    @PostMapping("/online")
    public ResponseEntity<?> onlineExchange(
            @RequestBody FrgnExchOnlineDTO dto
    ) {
        CustomUserDetails user =
                (CustomUserDetails) SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal();

        String userId = user.getUsername();

        onlineExchangeService.processOnlineExchange(dto, userId);

        return ResponseEntity.ok("온라인 환전이 정상적으로 처리되었습니다.");
    }




    @GetMapping("/accounts")
    public ResponseEntity<?> getMyExchangeAccounts(
            @RequestParam String currency
    ) {
        CustomUserDetails user =
                (CustomUserDetails) SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal();

        String userId = user.getUsername();

        Map<String, Object> result =
                onlineExchangeService.getMyExchangeAccounts(userId, currency);

        return ResponseEntity.ok(result);
    }









}
