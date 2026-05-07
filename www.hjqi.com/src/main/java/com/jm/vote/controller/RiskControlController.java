package com.jm.vote.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jm.vote.dto.CreateRiskControlRuleRequest;
import com.jm.vote.dto.RiskControlRuleDTO;
import com.jm.vote.service.RiskControlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/risk-control")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class RiskControlController {

    private final RiskControlService riskControlService;

    /**
     * 获取风控规则列表
     */
    @GetMapping("/rules")
    public ResponseEntity<Page<RiskControlRuleDTO>> getRules(
            @RequestParam(required = false) String ruleType,
            @RequestParam(required = false) Integer enabled,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(riskControlService.getRules(ruleType, enabled, page, size));
    }

    /**
     * 获取规则详情
     */
    @GetMapping("/rules/{id}")
    public ResponseEntity<RiskControlRuleDTO> getRule(@PathVariable Long id) {
        return ResponseEntity.ok(riskControlService.getRule(id));
    }

    /**
     * 创建规则
     */
    @PostMapping("/rules")
    public ResponseEntity<RiskControlRuleDTO> createRule(@Valid @RequestBody CreateRiskControlRuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(riskControlService.createRule(request));
    }

    /**
     * 更新规则
     */
    @PutMapping("/rules/{id}")
    public ResponseEntity<RiskControlRuleDTO> updateRule(
            @PathVariable Long id,
            @Valid @RequestBody CreateRiskControlRuleRequest request) {
        return ResponseEntity.ok(riskControlService.updateRule(id, request));
    }

    /**
     * 删除规则
     */
    @DeleteMapping("/rules/{id}")
    public ResponseEntity<Map<String, String>> deleteRule(@PathVariable Long id) {
        riskControlService.deleteRule(id);
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }

    /**
     * 启用/禁用规则
     */
    @PutMapping("/rules/{id}/toggle")
    public ResponseEntity<RiskControlRuleDTO> toggleRule(
            @PathVariable Long id,
            @RequestParam Integer enabled) {
        return ResponseEntity.ok(riskControlService.toggleRule(id, enabled));
    }
}

