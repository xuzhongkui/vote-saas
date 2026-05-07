package com.jm.vote.controller;

import com.jm.vote.dto.MerchantLoginRequest;
import com.jm.vote.dto.MerchantLoginResponse;
import com.jm.vote.dto.MerchantRegisterRequest;
import com.jm.vote.service.MerchantAuthService;
import com.jm.vote.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/merchant")
@RequiredArgsConstructor
public class MerchantAuthController {

    private final MerchantAuthService merchantAuthService;
    private final PromotionService promotionService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody MerchantRegisterRequest request) {
        Map<String, Object> result = merchantAuthService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody MerchantLoginRequest request) {
        try {
            MerchantLoginResponse resp = merchantAuthService.login(request);
            return ResponseEntity.ok(resp);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("message", e.getReason() != null ? e.getReason() : "登录失败"));
        }
    }

    /**
     * 验证推广码（公开接口，注册时使用）
     */
    @GetMapping("/promotion/verify/{promotionCode}")
    public ResponseEntity<Map<String, Object>> verifyPromotionCode(@PathVariable String promotionCode) {
        Map<String, Object> result = promotionService.verifyPromotionCode(promotionCode);
        return ResponseEntity.ok(result);
    }
}


