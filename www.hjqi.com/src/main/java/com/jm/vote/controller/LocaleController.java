package com.jm.vote.controller;

import com.jm.vote.service.PlatformConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 平台语言设置控制器
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LocaleController {

    private final PlatformConfigService platformConfigService;

    // ==================== 公开接口 ====================

    /**
     * 获取平台语言设置（公开接口，无需登录）
     */
    @GetMapping("/public/platform-locale")
    public ResponseEntity<Map<String, String>> getPlatformLocale() {
        String locale = platformConfigService.getPlatformLocale();
        return ResponseEntity.ok(Map.of("locale", locale));
    }

    // ==================== 管理员接口 ====================

    /**
     * 获取平台语言设置（管理员）
     */
    @GetMapping("/admin/platform-locale")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Map<String, String>> getAdminPlatformLocale() {
        String locale = platformConfigService.getPlatformLocale();
        return ResponseEntity.ok(Map.of("locale", locale));
    }

    /**
     * 设置平台语言（仅管理员可操作）
     */
    @PutMapping("/admin/platform-locale")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> setPlatformLocale(@RequestBody Map<String, String> request) {
        String locale = request.get("locale");
        if (locale == null || locale.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "语言代码不能为空"
            ));
        }
        
        platformConfigService.setPlatformLocale(locale);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "语言设置成功",
            "locale", locale
        ));
    }

    // ==================== 商家端接口 ====================

    /**
     * 获取平台语言设置（商家端）
     */
    @GetMapping("/merchant/platform-locale")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Map<String, String>> getMerchantPlatformLocale() {
        String locale = platformConfigService.getPlatformLocale();
        return ResponseEntity.ok(Map.of("locale", locale));
    }

    /**
     * 设置平台语言（商家端）
     */
    @PutMapping("/merchant/platform-locale")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Map<String, Object>> setMerchantPlatformLocale(@RequestBody Map<String, String> request) {
        String locale = request.get("locale");
        if (locale == null || locale.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "语言代码不能为空"
            ));
        }
        
        platformConfigService.setPlatformLocale(locale);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "语言设置成功",
            "locale", locale
        ));
    }

    // ==================== 用户端接口 ====================

    /**
     * 获取平台语言设置（用户端）
     */
    @GetMapping("/user/platform-locale")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Map<String, String>> getUserPlatformLocale() {
        String locale = platformConfigService.getPlatformLocale();
        return ResponseEntity.ok(Map.of("locale", locale));
    }

    /**
     * 设置平台语言（用户端）
     */
    @PutMapping("/user/platform-locale")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> setUserPlatformLocale(@RequestBody Map<String, String> request) {
        String locale = request.get("locale");
        if (locale == null || locale.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "语言代码不能为空"
            ));
        }
        
        platformConfigService.setPlatformLocale(locale);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "语言设置成功",
            "locale", locale
        ));
    }
}

