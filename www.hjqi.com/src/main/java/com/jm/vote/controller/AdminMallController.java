package com.jm.vote.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jm.vote.annotation.OperationLog;
import com.jm.vote.dto.*;
import com.jm.vote.service.AdminMallService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminMallController {

    private final AdminMallService adminMallService;

    // ==================== 商家管理 ====================

    @GetMapping("/merchants")
    public ResponseEntity<Page<MerchantListDTO>> getMerchants(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        return ResponseEntity.ok(adminMallService.getMerchants(page, size, status));
    }

    @GetMapping("/merchants/{id}")
    public ResponseEntity<MerchantDetailDTO> getMerchant(@PathVariable Long id) {
        return ResponseEntity.ok(adminMallService.getMerchant(id));
    }

    @PostMapping("/merchants/{id}/audit")
    @OperationLog(operation = "AUDIT", module = "MERCHANT", description = "审核商家")
    public ResponseEntity<MerchantDetailDTO> auditMerchant(@PathVariable Long id, @Valid @RequestBody AuditMerchantRequest request) {
        return ResponseEntity.ok(adminMallService.auditMerchant(id, request));
    }

    @PutMapping("/merchants/{id}/status")
    @OperationLog(operation = "UPDATE", module = "MERCHANT", description = "更新商家状态")
    public ResponseEntity<MerchantDetailDTO> updateMerchantStatus(@PathVariable Long id, @RequestParam Integer status) {
        return ResponseEntity.ok(adminMallService.updateMerchantStatus(id, status));
    }

    // ==================== 系统配置管理 ====================

    @GetMapping("/system-configs")
    public ResponseEntity<List<SystemConfigDTO>> getSystemConfigs(@RequestParam(required = false) String configGroup) {
        return ResponseEntity.ok(adminMallService.getSystemConfigs(configGroup));
    }

    @GetMapping("/system-configs/{id}")
    public ResponseEntity<SystemConfigDTO> getSystemConfig(@PathVariable Long id) {
        return ResponseEntity.ok(adminMallService.getSystemConfig(id));
    }

    @GetMapping("/system-configs/key/{configKey}")
    public ResponseEntity<SystemConfigDTO> getSystemConfigByKey(@PathVariable String configKey) {
        return ResponseEntity.ok(adminMallService.getSystemConfigByKey(configKey));
    }

    @PostMapping("/system-configs")
    @OperationLog(operation = "CREATE", module = "CONFIG", description = "创建系统配置")
    public ResponseEntity<SystemConfigDTO> createSystemConfig(@Valid @RequestBody CreateSystemConfigRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminMallService.createSystemConfig(request));
    }

    @PutMapping("/system-configs/{id}")
    @OperationLog(operation = "UPDATE", module = "CONFIG", description = "更新系统配置")
    public ResponseEntity<SystemConfigDTO> updateSystemConfig(@PathVariable Long id, @RequestBody UpdateSystemConfigRequest request) {
        return ResponseEntity.ok(adminMallService.updateSystemConfig(id, request));
    }

    @DeleteMapping("/system-configs/{id}")
    @OperationLog(operation = "DELETE", module = "CONFIG", description = "删除系统配置")
    public ResponseEntity<Map<String, String>> deleteSystemConfig(@PathVariable Long id) {
        adminMallService.deleteSystemConfig(id);
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }

    // ==================== 数据统计 ====================

    @GetMapping("/statistics")
    public ResponseEntity<AdminStatisticsDTO> getStatistics() {
        return ResponseEntity.ok(adminMallService.getStatistics());
    }

    // ==================== 提现管理 ====================

    /**
     * 提现申请列表
     */
    @GetMapping("/promotion/withdrawals")
    public ResponseEntity<Page<PromotionWithdrawalDTO>> getPromotionWithdrawals(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        return ResponseEntity.ok(adminMallService.getPromotionWithdrawals(page, size, status));
    }

    /**
     * 审核提现申请（通过/拒绝）
     */
    @PostMapping("/promotion/withdrawals/{id}/audit")
    @OperationLog(operation = "AUDIT", module = "WITHDRAWAL", description = "审核提现申请")
    public ResponseEntity<Map<String, Object>> auditPromotionWithdrawal(
            @PathVariable Long id,
            @RequestBody AuditWithdrawalRequest request) {
        Map<String, Object> result = adminMallService.auditPromotionWithdrawal(id, request.isApproved(), request.getRemark());
        return ResponseEntity.ok(result);
    }

    /**
     * 确认打款
     */
    @PostMapping("/promotion/withdrawals/{id}/transfer")
    @OperationLog(operation = "UPDATE", module = "WITHDRAWAL", description = "确认提现打款")
    public ResponseEntity<Map<String, Object>> confirmPromotionWithdrawalTransfer(
            @PathVariable Long id,
            @RequestBody TransferRequest request) {
        Map<String, Object> result = adminMallService.confirmPromotionWithdrawalTransfer(id, request.getTransferVoucher());
        return ResponseEntity.ok(result);
    }

    @lombok.Data
    public static class AuditWithdrawalRequest {
        private boolean approved;
        private String remark;
    }

    @lombok.Data
    public static class TransferRequest {
        private String transferVoucher;
    }
}

