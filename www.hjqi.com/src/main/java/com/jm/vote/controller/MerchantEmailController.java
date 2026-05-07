package com.jm.vote.controller;

import com.jm.vote.entity.MerchantEmail;
import com.jm.vote.service.MerchantEmailService;
import com.jm.vote.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/merchant/emails")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MERCHANT')")
public class MerchantEmailController {

    private final MerchantEmailService merchantEmailService;
    private final SecurityUtil securityUtil;

    /**
     * 获取邮箱列表
     */
    @GetMapping
    public ResponseEntity<List<MerchantEmail>> getEmails() {
        Long merchantId = securityUtil.getCurrentMerchantId();
        List<MerchantEmail> emails = merchantEmailService.getEmails(merchantId);
        return ResponseEntity.ok(emails);
    }

    /**
     * 创建邮箱
     */
    @PostMapping
    public ResponseEntity<MerchantEmail> createEmail(@RequestBody CreateMerchantEmailRequest request) {
        Long merchantId = securityUtil.getCurrentMerchantId();
        MerchantEmail email = merchantEmailService.createEmail(
                merchantId,
                request.getEmail(),
                request.getName(),
                request.getPurpose(),
                request.getIsDefault()
        );
        return ResponseEntity.ok(email);
    }

    /**
     * 更新邮箱
     */
    @PutMapping("/{id}")
    public ResponseEntity<MerchantEmail> updateEmail(
            @PathVariable Long id,
            @RequestBody UpdateMerchantEmailRequest request) {
        Long merchantId = securityUtil.getCurrentMerchantId();
        MerchantEmail email = merchantEmailService.updateEmail(
                id,
                merchantId,
                request.getEmail(),
                request.getName(),
                request.getPurpose(),
                request.getIsDefault(),
                request.getStatus()
        );
        return ResponseEntity.ok(email);
    }

    /**
     * 删除邮箱
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteEmail(@PathVariable Long id) {
        Long merchantId = securityUtil.getCurrentMerchantId();
        merchantEmailService.deleteEmail(id, merchantId);
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }

    // 内部请求类
    @lombok.Data
    public static class CreateMerchantEmailRequest {
        private String email;
        private String name;
        private String purpose;
        private Integer isDefault;
    }

    @lombok.Data
    public static class UpdateMerchantEmailRequest {
        private String email;
        private String name;
        private String purpose;
        private Integer isDefault;
        private Integer status;
    }
}

