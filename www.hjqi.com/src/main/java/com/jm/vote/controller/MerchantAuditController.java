package com.jm.vote.controller;

import com.jm.vote.entity.Merchant;
import com.jm.vote.service.MerchantAuditService;
import com.jm.vote.util.SecurityUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 商家审核管理接口（管理员使用）
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/merchant-audit")
@RequiredArgsConstructor
public class MerchantAuditController {

    private final MerchantAuditService merchantAuditService;
    private final SecurityUtil securityUtil;

    /**
     * 获取待审核商家列表
     */
    @GetMapping("/pending")
    public ResponseEntity<List<Merchant>> getPendingMerchants() {
        // 验证管理员权限
        securityUtil.requireAdmin();
        
        List<Merchant> merchants = merchantAuditService.getPendingMerchants();
        return ResponseEntity.ok(merchants);
    }

    /**
     * 获取商家审核详情
     */
    @GetMapping("/{merchantId}")
    public ResponseEntity<Map<String, Object>> getMerchantAuditDetail(@PathVariable Long merchantId) {
        // 验证管理员权限
        securityUtil.requireAdmin();
        
        Map<String, Object> detail = merchantAuditService.getMerchantAuditDetail(merchantId);
        return ResponseEntity.ok(detail);
    }

    /**
     * 审核通过
     */
    @PostMapping("/{merchantId}/approve")
    public ResponseEntity<Map<String, Object>> approveMerchant(
            @PathVariable Long merchantId,
            @RequestBody(required = false) AuditRequest request) {
        // 验证管理员权限
        Long adminId = securityUtil.requireAdmin();
        
        String remark = request != null ? request.getRemark() : null;
        Map<String, Object> result = merchantAuditService.approve(merchantId, adminId, remark);
        
        return ResponseEntity.ok(result);
    }

    /**
     * 审核拒绝
     */
    @PostMapping("/{merchantId}/reject")
    public ResponseEntity<Map<String, Object>> rejectMerchant(
            @PathVariable Long merchantId,
            @RequestBody AuditRequest request) {
        // 验证管理员权限
        Long adminId = securityUtil.requireAdmin();
        
        if (request.getReason() == null || request.getReason().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "拒绝原因不能为空"));
        }
        
        Map<String, Object> result = merchantAuditService.reject(merchantId, adminId, request.getReason());
        
        return ResponseEntity.ok(result);
    }

    /**
     * 批量��核通过
     */
    @PostMapping("/batch/approve")
    public ResponseEntity<Map<String, Object>> batchApproveMerchants(@RequestBody BatchAuditRequest request) {
        // 验证管理员权限
        Long adminId = securityUtil.requireAdmin();
        
        if (request.getMerchantIds() == null || request.getMerchantIds().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "商家ID列表不能为空"));
        }
        
        Map<String, Object> result = merchantAuditService.batchApprove(request.getMerchantIds(), adminId, request.getRemark());
        
        return ResponseEntity.ok(result);
    }

    /**
     * 批量审核拒绝
     */
    @PostMapping("/batch/reject")
    public ResponseEntity<Map<String, Object>> batchRejectMerchants(@RequestBody BatchAuditRequest request) {
        // 验证管理员权限
        Long adminId = securityUtil.requireAdmin();
        
        if (request.getMerchantIds() == null || request.getMerchantIds().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "商家ID列表不能为空"));
        }
        
        if (request.getReason() == null || request.getReason().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "拒绝原因不能为空"));
        }
        
        Map<String, Object> result = merchantAuditService.batchReject(request.getMerchantIds(), adminId, request.getReason());
        
        return ResponseEntity.ok(result);
    }

    @Data
    public static class AuditRequest {
        private String remark;
        private String reason;
    }

    @Data
    public static class BatchAuditRequest {
        private List<Long> merchantIds;
        private String remark;
        private String reason;
    }
}

