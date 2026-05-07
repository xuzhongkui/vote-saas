package com.jm.vote.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jm.vote.dto.CreateRiskControlRuleRequest;
import com.jm.vote.dto.RiskControlRuleDTO;
import com.jm.vote.entity.RiskControlRule;
import com.jm.vote.repository.RiskControlRuleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RiskControlService {

    private final RiskControlRuleMapper riskControlRuleMapper;

    /**
     * 获取风控规则列表
     */
    public Page<RiskControlRuleDTO> getRules(String ruleType, Integer enabled, int page, int size) {
        LambdaQueryWrapper<RiskControlRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RiskControlRule::getDeleted, 0);
        if (ruleType != null && !ruleType.isEmpty()) {
            wrapper.eq(RiskControlRule::getRuleType, ruleType);
        }
        if (enabled != null) {
            wrapper.eq(RiskControlRule::getEnabled, enabled);
        }
        wrapper.orderByDesc(RiskControlRule::getPriority).orderByDesc(RiskControlRule::getCreatedAt);
        
        Page<RiskControlRule> rulePage = new Page<>(page, size);
        Page<RiskControlRule> result = riskControlRuleMapper.selectPage(rulePage, wrapper);
        
        Page<RiskControlRuleDTO> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        dtoPage.setRecords(result.getRecords().stream()
                .map(this::toDTO)
                .collect(Collectors.toList()));
        return dtoPage;
    }

    /**
     * 获取规则详情
     */
    public RiskControlRuleDTO getRule(Long id) {
        RiskControlRule rule = riskControlRuleMapper.selectById(id);
        if (rule == null || rule.getDeleted() == 1) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "规则不存在");
        }
        return toDTO(rule);
    }

    /**
     * 创建规则
     */
    @Transactional
    public RiskControlRuleDTO createRule(CreateRiskControlRuleRequest request) {
        RiskControlRule rule = new RiskControlRule();
        rule.setRuleName(request.getRuleName());
        rule.setRuleType(request.getRuleType());
        rule.setRuleConfig(request.getRuleConfig());
        rule.setAction(request.getAction());
        rule.setEnabled(request.getEnabled() != null ? request.getEnabled() : 1);
        rule.setPriority(request.getPriority());
        rule.setDescription(request.getDescription());
        rule.setCreatedAt(LocalDateTime.now());
        rule.setUpdatedAt(LocalDateTime.now());
        rule.setDeleted(0);

        riskControlRuleMapper.insert(rule);
        return toDTO(rule);
    }

    /**
     * 更新规则
     */
    @Transactional
    public RiskControlRuleDTO updateRule(Long id, CreateRiskControlRuleRequest request) {
        RiskControlRule rule = riskControlRuleMapper.selectById(id);
        if (rule == null || rule.getDeleted() == 1) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "规则不存在");
        }

        if (request.getRuleName() != null) {
            rule.setRuleName(request.getRuleName());
        }
        if (request.getRuleType() != null) {
            rule.setRuleType(request.getRuleType());
        }
        if (request.getRuleConfig() != null) {
            rule.setRuleConfig(request.getRuleConfig());
        }
        if (request.getAction() != null) {
            rule.setAction(request.getAction());
        }
        if (request.getEnabled() != null) {
            rule.setEnabled(request.getEnabled());
        }
        if (request.getPriority() != null) {
            rule.setPriority(request.getPriority());
        }
        if (request.getDescription() != null) {
            rule.setDescription(request.getDescription());
        }
        rule.setUpdatedAt(LocalDateTime.now());

        riskControlRuleMapper.updateById(rule);
        return toDTO(rule);
    }

    /**
     * 删除规则
     */
    @Transactional
    public void deleteRule(Long id) {
        RiskControlRule rule = riskControlRuleMapper.selectById(id);
        if (rule == null || rule.getDeleted() == 1) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "规则不存在");
        }
        rule.setDeleted(1);
        rule.setUpdatedAt(LocalDateTime.now());
        riskControlRuleMapper.updateById(rule);
    }

    /**
     * 启用/禁用规则
     */
    @Transactional
    public RiskControlRuleDTO toggleRule(Long id, Integer enabled) {
        RiskControlRule rule = riskControlRuleMapper.selectById(id);
        if (rule == null || rule.getDeleted() == 1) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "规则不存在");
        }
        rule.setEnabled(enabled);
        rule.setUpdatedAt(LocalDateTime.now());
        riskControlRuleMapper.updateById(rule);
        return toDTO(rule);
    }

    private RiskControlRuleDTO toDTO(RiskControlRule rule) {
        RiskControlRuleDTO dto = new RiskControlRuleDTO();
        dto.setId(rule.getId());
        dto.setRuleName(rule.getRuleName());
        dto.setRuleType(rule.getRuleType());
        dto.setRuleConfig(rule.getRuleConfig());
        dto.setAction(rule.getAction());
        dto.setEnabled(rule.getEnabled());
        dto.setPriority(rule.getPriority());
        dto.setDescription(rule.getDescription());
        dto.setCreatedAt(rule.getCreatedAt());
        dto.setUpdatedAt(rule.getUpdatedAt());
        return dto;
    }
}

