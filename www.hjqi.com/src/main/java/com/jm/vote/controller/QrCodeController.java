package com.jm.vote.controller;

import com.jm.vote.service.QrCodeService;
import com.jm.vote.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/qrcode")
@RequiredArgsConstructor
public class QrCodeController {

    private final QrCodeService qrCodeService;
    private final SecurityUtil securityUtil;

    /**
     * 生成商家邀请码二维码（Base64）
     */
    @GetMapping("/merchant/invite")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Map<String, Object>> generateMerchantInviteQr(HttpServletRequest request) {
        Long merchantId = securityUtil.getCurrentMerchantId();
        if (merchantId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        String qrCode = qrCodeService.generateMerchantInviteCodeQr(merchantId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("qrCode", qrCode);
        return ResponseEntity.ok(response);
    }

    /**
     * 生成并保存商家邀请码二维码（返回URL）
     */
    @PostMapping("/merchant/invite/generate")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Map<String, Object>> generateAndSaveMerchantInviteQr() {
        Long merchantId = securityUtil.getCurrentMerchantId();
        if (merchantId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        String qrCodeUrl = qrCodeService.generateAndSaveMerchantInviteQrCode(merchantId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("qrCodeUrl", qrCodeUrl);
        return ResponseEntity.ok(response);
    }

    /**
     * 生成商品分享二维码
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<Map<String, Object>> generateProductShareQr(@PathVariable Long productId) {
        String qrCode = qrCodeService.generateProductShareQr(productId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("qrCode", qrCode);
        return ResponseEntity.ok(response);
    }

    /**
     * 生成店铺二维码
     */
    @GetMapping("/shop/{merchantId}")
    public ResponseEntity<Map<String, Object>> generateShopQr(@PathVariable Long merchantId) {
        String qrCode = qrCodeService.generateShopQr(merchantId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("qrCode", qrCode);
        return ResponseEntity.ok(response);
    }
}

