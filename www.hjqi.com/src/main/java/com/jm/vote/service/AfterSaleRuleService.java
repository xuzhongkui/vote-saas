package com.jm.vote.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jm.vote.entity.AfterSaleRule;
import com.jm.vote.repository.AfterSaleRuleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AfterSaleRuleService {

    private final AfterSaleRuleMapper ruleMapper;

    /**
     * 创建售后规则
     */
    @Transactional
    public AfterSaleRule createRule(Long merchantId, Long productId, String ruleType, 
                                   String titleZh, String titleEn, String contentZh, String contentEn, Integer validDays) {
        AfterSaleRule rule = AfterSaleRule.builder()
                .merchantId(merchantId)
                .productId(productId)
                .ruleType(ruleType)
                .titleZh(titleZh)
                .titleEn(titleEn)
                .contentZh(contentZh)
                .contentEn(contentEn)
                .validDays(validDays != null ? validDays : 7)
                .status(1)
                .deleted(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        ruleMapper.insert(rule);
        return rule;
    }

    /**
     * 获取售后规则列表
     */
    public List<AfterSaleRule> getRules(Long merchantId, Long productId, String ruleType) {
        LambdaQueryWrapper<AfterSaleRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AfterSaleRule::getMerchantId, merchantId)
                .eq(AfterSaleRule::getDeleted, 0)
                .eq(AfterSaleRule::getStatus, 1);
        
        if (productId != null) {
            wrapper.and(w -> w.eq(AfterSaleRule::getProductId, productId).or().isNull(AfterSaleRule::getProductId));
        }
        if (ruleType != null && !ruleType.isEmpty()) {
            wrapper.eq(AfterSaleRule::getRuleType, ruleType);
        }
        
        wrapper.orderByDesc(AfterSaleRule::getCreatedAt);
        return ruleMapper.selectList(wrapper);
    }

    /**
     * 更新售后规则
     */
    @Transactional
    public AfterSaleRule updateRule(Long id, Long merchantId, String titleZh, String titleEn, 
                                   String contentZh, String contentEn, Integer validDays, Integer status) {
        AfterSaleRule rule = ruleMapper.selectById(id);
        if (rule == null || !rule.getMerchantId().equals(merchantId)) {
            throw new RuntimeException("规则不存在或无权限");
        }
        
        if (titleZh != null) rule.setTitleZh(titleZh);
        if (titleEn != null) rule.setTitleEn(titleEn);
        if (contentZh != null) rule.setContentZh(contentZh);
        if (contentEn != null) rule.setContentEn(contentEn);
        if (validDays != null) rule.setValidDays(validDays);
        if (status != null) rule.setStatus(status);
        
        rule.setUpdatedAt(LocalDateTime.now());
        ruleMapper.updateById(rule);
        return rule;
    }

    /**
     * 删除售后规则
     */
    @Transactional
    public void deleteRule(Long id, Long merchantId) {
        AfterSaleRule rule = ruleMapper.selectById(id);
        if (rule == null || !rule.getMerchantId().equals(merchantId)) {
            throw new RuntimeException("规则不存在或无权限");
        }
        
        rule.setDeleted(1);
        rule.setUpdatedAt(LocalDateTime.now());
        ruleMapper.updateById(rule);
    }
}

