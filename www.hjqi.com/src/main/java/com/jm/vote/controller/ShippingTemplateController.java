package com.jm.vote.controller;

import com.jm.vote.entity.ShippingTemplate;
import com.jm.vote.entity.ShippingTemplateRule;
import com.jm.vote.service.ShippingTemplateService;
import com.jm.vote.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/merchant/shipping-templates")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MERCHANT')")
public class ShippingTemplateController {

    private final ShippingTemplateService shippingTemplateService;
    private final SecurityUtil securityUtil;

    /**
     * 获取运费模板列表
     */
    @GetMapping
    public ResponseEntity<List<ShippingTemplate>> getTemplates() {
        Long merchantId = securityUtil.getCurrentMerchantId();
        List<ShippingTemplate> templates = shippingTemplateService.getTemplates(merchantId);
        return ResponseEntity.ok(templates);
    }

    /**
     * 获取运费模板详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ShippingTemplate> getTemplate(@PathVariable Long id) {
        Long merchantId = securityUtil.getCurrentMerchantId();
        ShippingTemplate template = shippingTemplateService.getTemplate(id, merchantId);
        return ResponseEntity.ok(template);
    }

    /**
     * 创建运费模板
     */
    @PostMapping
    public ResponseEntity<ShippingTemplate> createTemplate(@RequestBody CreateShippingTemplateRequest request) {
        Long merchantId = securityUtil.getCurrentMerchantId();
        ShippingTemplate template = shippingTemplateService.createTemplate(
                merchantId,
                request.getName(),
                request.getType(),
                request.getFreeShippingAmount(),
                request.getFreeShippingCondition(),
                request.getDefaultFee()
        );
        
        // 如果设置为默认模板，先取消其他默认模板
        if (request.getIsDefault() != null && request.getIsDefault() == 1) {
            shippingTemplateService.setDefaultTemplate(template.getId(), merchantId);
        }
        
        return ResponseEntity.ok(template);
    }

    /**
     * 更新运费模板
     */
    @PutMapping("/{id}")
    public ResponseEntity<ShippingTemplate> updateTemplate(
            @PathVariable Long id,
            @RequestBody UpdateShippingTemplateRequest request) {
        Long merchantId = securityUtil.getCurrentMerchantId();
        ShippingTemplate template = shippingTemplateService.updateTemplate(
                id,
                merchantId,
                request.getName(),
                request.getType(),
                request.getFreeShippingAmount(),
                request.getFreeShippingCondition(),
                request.getDefaultFee(),
                request.getStatus(),
                request.getIsDefault()
        );
        return ResponseEntity.ok(template);
    }

    /**
     * 删除运费模板
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteTemplate(@PathVariable Long id) {
        Long merchantId = securityUtil.getCurrentMerchantId();
        shippingTemplateService.deleteTemplate(id, merchantId);
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }

    /**
     * 获取运费模板规则列表
     */
    @GetMapping("/{id}/rules")
    public ResponseEntity<List<ShippingTemplateRule>> getTemplateRules(@PathVariable Long id) {
        Long merchantId = securityUtil.getCurrentMerchantId();
        List<ShippingTemplateRule> rules = shippingTemplateService.getTemplateRules(id, merchantId);
        return ResponseEntity.ok(rules);
    }

    /**
     * 添加运费模板规则
     */
    @PostMapping("/{id}/rules")
    public ResponseEntity<ShippingTemplateRule> addRule(
            @PathVariable Long id,
            @RequestBody CreateShippingTemplateRuleRequest request) {
        Long merchantId = securityUtil.getCurrentMerchantId();
        ShippingTemplateRule rule = shippingTemplateService.addRule(
                id,
                request.getRegionCodes(),
                request.getRegionNames(),
                request.getFirstWeight(),
                request.getFirstFee(),
                request.getContinueWeight(),
                request.getContinueFee(),
                request.getFirstCount(),
                request.getFirstCountFee(),
                request.getContinueCount(),
                request.getContinueCountFee()
        );
        return ResponseEntity.ok(rule);
    }

    /**
     * 删除运费模板规则
     */
    @DeleteMapping("/rules/{ruleId}")
    public ResponseEntity<Map<String, String>> deleteRule(@PathVariable Long ruleId) {
        Long merchantId = securityUtil.getCurrentMerchantId();
        shippingTemplateService.deleteRule(ruleId, merchantId);
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }

    // 内部请求类
    @lombok.Data
    public static class CreateShippingTemplateRequest {
        private String name;
        private String type;
        private BigDecimal freeShippingAmount;
        private String freeShippingCondition;
        private BigDecimal defaultFee;
        private Integer isDefault;
    }

    @lombok.Data
    public static class UpdateShippingTemplateRequest {
        private String name;
        private String type;
        private BigDecimal freeShippingAmount;
        private String freeShippingCondition;
        private BigDecimal defaultFee;
        private Integer status;
        private Integer isDefault;
    }

    @lombok.Data
    public static class CreateShippingTemplateRuleRequest {
        private String regionCodes;
        private String regionNames;
        private Integer firstWeight;
        private BigDecimal firstFee;
        private Integer continueWeight;
        private BigDecimal continueFee;
        private Integer firstCount;
        private BigDecimal firstCountFee;
        private Integer continueCount;
        private BigDecimal continueCountFee;
    }
}

