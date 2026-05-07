package com.jm.vote.controller;

import com.jm.vote.entity.AfterSaleRule;
import com.jm.vote.service.AfterSaleRuleService;
import com.jm.vote.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/merchant/after-sale-rules")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MERCHANT')")
public class AfterSaleRuleController {

    private final AfterSaleRuleService afterSaleRuleService;
    private final SecurityUtil securityUtil;

    /**
     * 获取售后规则列表
     */
    @GetMapping
    public ResponseEntity<List<AfterSaleRule>> getRules(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String ruleType) {
        Long merchantId = securityUtil.getCurrentMerchantId();
        List<AfterSaleRule> rules = afterSaleRuleService.getRules(merchantId, productId, ruleType);
        return ResponseEntity.ok(rules);
    }

    /**
     * 创建售后规则
     */
    @PostMapping
    public ResponseEntity<AfterSaleRule> createRule(@RequestBody CreateAfterSaleRuleRequest request) {
        Long merchantId = securityUtil.getCurrentMerchantId();
        AfterSaleRule rule = afterSaleRuleService.createRule(
                merchantId,
                request.getProductId(),
                request.getRuleType(),
                request.getTitleZh(),
                request.getTitleEn(),
                request.getContentZh(),
                request.getContentEn(),
                request.getValidDays()
        );
        return ResponseEntity.ok(rule);
    }

    /**
     * 更新售后规则
     */
    @PutMapping("/{id}")
    public ResponseEntity<AfterSaleRule> updateRule(
            @PathVariable Long id,
            @RequestBody UpdateAfterSaleRuleRequest request) {
        Long merchantId = securityUtil.getCurrentMerchantId();
        AfterSaleRule rule = afterSaleRuleService.updateRule(
                id,
                merchantId,
                request.getTitleZh(),
                request.getTitleEn(),
                request.getContentZh(),
                request.getContentEn(),
                request.getValidDays(),
                request.getStatus()
        );
        return ResponseEntity.ok(rule);
    }

    /**
     * 删除售后规则
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteRule(@PathVariable Long id) {
        Long merchantId = securityUtil.getCurrentMerchantId();
        afterSaleRuleService.deleteRule(id, merchantId);
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }

    // 内部请求类
    @lombok.Data
    public static class CreateAfterSaleRuleRequest {
        private Long productId;
        private String ruleType;
        private String titleZh;
        private String titleEn;
        private String contentZh;
        private String contentEn;
        private Integer validDays;
    }

    @lombok.Data
    public static class UpdateAfterSaleRuleRequest {
        private String titleZh;
        private String titleEn;
        private String contentZh;
        private String contentEn;
        private Integer validDays;
        private Integer status;
    }
}

