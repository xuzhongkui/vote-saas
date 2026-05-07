package com.jm.vote.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jm.vote.entity.ShippingTemplate;
import com.jm.vote.entity.ShippingTemplateRule;
import com.jm.vote.repository.ShippingTemplateMapper;
import com.jm.vote.repository.ShippingTemplateRuleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShippingTemplateService {

    private final ShippingTemplateMapper templateMapper;
    private final ShippingTemplateRuleMapper ruleMapper;

    /**
     * 创建运费模板
     */
    @Transactional
    public ShippingTemplate createTemplate(Long merchantId, String name, String type, BigDecimal freeShippingAmount, String freeShippingCondition, BigDecimal defaultFee) {
        ShippingTemplate template = ShippingTemplate.builder()
                .merchantId(merchantId)
                .name(name)
                .type(type != null ? type : "REGION")
                .freeShippingAmount(freeShippingAmount)
                .freeShippingCondition(freeShippingCondition)
                .defaultFee(defaultFee != null ? defaultFee : BigDecimal.ZERO)
                .status(1)
                .isDefault(0)
                .deleted(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        templateMapper.insert(template);
        return template;
    }

    /**
     * 添加运费规则
     */
    @Transactional
    public ShippingTemplateRule addRule(Long templateId, String regionCodes, String regionNames, 
                                       Integer firstWeight, BigDecimal firstFee, Integer continueWeight, BigDecimal continueFee,
                                       Integer firstCount, BigDecimal firstCountFee, Integer continueCount, BigDecimal continueCountFee) {
        ShippingTemplateRule rule = ShippingTemplateRule.builder()
                .templateId(templateId)
                .regionCodes(regionCodes)
                .regionNames(regionNames)
                .firstWeight(firstWeight)
                .firstFee(firstFee != null ? firstFee : BigDecimal.ZERO)
                .continueWeight(continueWeight)
                .continueFee(continueFee != null ? continueFee : BigDecimal.ZERO)
                .firstCount(firstCount)
                .firstCountFee(firstCountFee != null ? firstCountFee : BigDecimal.ZERO)
                .continueCount(continueCount)
                .continueCountFee(continueCountFee != null ? continueCountFee : BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        ruleMapper.insert(rule);
        return rule;
    }

    /**
     * 计算运费
     */
    public BigDecimal calculateShippingFee(Long merchantId, String regionCode, BigDecimal orderAmount, Integer weight, Integer count) {
        // 获取默认模板
        LambdaQueryWrapper<ShippingTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShippingTemplate::getMerchantId, merchantId)
                .eq(ShippingTemplate::getStatus, 1)
                .eq(ShippingTemplate::getDeleted, 0)
                .eq(ShippingTemplate::getIsDefault, 1)
                .last("LIMIT 1");
        
        ShippingTemplate template = templateMapper.selectOne(wrapper);
        if (template == null) {
            return BigDecimal.ZERO;
        }
        
        // 检查是否包邮
        if (template.getFreeShippingAmount() != null && orderAmount.compareTo(template.getFreeShippingAmount()) >= 0) {
            return BigDecimal.ZERO;
        }
        
        // 根据模板类型计算运费
        if ("REGION".equals(template.getType())) {
            return calculateRegionShipping(template.getId(), regionCode);
        } else if ("WEIGHT".equals(template.getType()) && weight != null) {
            return calculateWeightShipping(template.getId(), weight);
        } else if ("COUNT".equals(template.getType()) && count != null) {
            return calculateCountShipping(template.getId(), count);
        }
        
        return template.getDefaultFee();
    }

    private BigDecimal calculateRegionShipping(Long templateId, String regionCode) {
        // 查找匹配的规则
        List<ShippingTemplateRule> rules = ruleMapper.selectList(
                new LambdaQueryWrapper<ShippingTemplateRule>()
                        .eq(ShippingTemplateRule::getTemplateId, templateId)
        );
        
        for (ShippingTemplateRule rule : rules) {
            if (rule.getRegionCodes() != null && rule.getRegionCodes().contains(regionCode)) {
                return rule.getFirstFee();
            }
        }
        
        return BigDecimal.ZERO;
    }

    private BigDecimal calculateWeightShipping(Long templateId, Integer weight) {
        List<ShippingTemplateRule> rules = ruleMapper.selectList(
                new LambdaQueryWrapper<ShippingTemplateRule>()
                        .eq(ShippingTemplateRule::getTemplateId, templateId)
        );
        
        if (rules.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        ShippingTemplateRule rule = rules.get(0);
        BigDecimal fee = rule.getFirstFee();
        
        if (rule.getFirstWeight() != null && weight > rule.getFirstWeight()) {
            int continueWeight = weight - rule.getFirstWeight();
            int continueCount = (int) Math.ceil((double) continueWeight / rule.getContinueWeight());
            fee = fee.add(rule.getContinueFee().multiply(new BigDecimal(continueCount)));
        }
        
        return fee;
    }

    private BigDecimal calculateCountShipping(Long templateId, Integer count) {
        List<ShippingTemplateRule> rules = ruleMapper.selectList(
                new LambdaQueryWrapper<ShippingTemplateRule>()
                        .eq(ShippingTemplateRule::getTemplateId, templateId)
        );
        
        if (rules.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        ShippingTemplateRule rule = rules.get(0);
        BigDecimal fee = rule.getFirstCountFee();
        
        if (rule.getFirstCount() != null && count > rule.getFirstCount()) {
            int continueCount = count - rule.getFirstCount();
            int continueCountUnits = (int) Math.ceil((double) continueCount / rule.getContinueCount());
            fee = fee.add(rule.getContinueCountFee().multiply(new BigDecimal(continueCountUnits)));
        }
        
        return fee;
    }

    /**
     * 获取运费模板列表
     */
    public List<ShippingTemplate> getTemplates(Long merchantId) {
        LambdaQueryWrapper<ShippingTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShippingTemplate::getMerchantId, merchantId)
                .eq(ShippingTemplate::getDeleted, 0)
                .orderByDesc(ShippingTemplate::getIsDefault)
                .orderByDesc(ShippingTemplate::getCreatedAt);
        return templateMapper.selectList(wrapper);
    }

    /**
     * 获取运费模板详情
     */
    public ShippingTemplate getTemplate(Long id, Long merchantId) {
        ShippingTemplate template = templateMapper.selectOne(
                new LambdaQueryWrapper<ShippingTemplate>()
                        .eq(ShippingTemplate::getId, id)
                        .eq(ShippingTemplate::getMerchantId, merchantId)
                        .eq(ShippingTemplate::getDeleted, 0)
        );
        if (template == null) {
            throw new RuntimeException("运费模板不存在或无权限");
        }
        return template;
    }

    /**
     * 更新运费模板
     */
    @Transactional
    public ShippingTemplate updateTemplate(Long id, Long merchantId, String name, String type,
                                          BigDecimal freeShippingAmount, String freeShippingCondition,
                                          BigDecimal defaultFee, Integer status, Integer isDefault) {
        ShippingTemplate template = templateMapper.selectOne(
                new LambdaQueryWrapper<ShippingTemplate>()
                        .eq(ShippingTemplate::getId, id)
                        .eq(ShippingTemplate::getMerchantId, merchantId)
                        .eq(ShippingTemplate::getDeleted, 0)
        );
        if (template == null) {
            throw new RuntimeException("运费模板不存在或无权限");
        }

        if (name != null) template.setName(name);
        if (type != null) template.setType(type);
        if (freeShippingAmount != null) template.setFreeShippingAmount(freeShippingAmount);
        if (freeShippingCondition != null) template.setFreeShippingCondition(freeShippingCondition);
        if (defaultFee != null) template.setDefaultFee(defaultFee);
        if (status != null) template.setStatus(status);
        if (isDefault != null) {
            template.setIsDefault(isDefault);
            if (isDefault == 1) {
                setDefaultTemplate(id, merchantId);
            }
        }

        template.setUpdatedAt(LocalDateTime.now());
        templateMapper.updateById(template);
        return template;
    }

    /**
     * 设置默认模板
     */
    @Transactional
    public void setDefaultTemplate(Long templateId, Long merchantId) {
        // 取消其他默认模板
        LambdaQueryWrapper<ShippingTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShippingTemplate::getMerchantId, merchantId)
                .eq(ShippingTemplate::getIsDefault, 1)
                .eq(ShippingTemplate::getDeleted, 0)
                .ne(ShippingTemplate::getId, templateId);
        List<ShippingTemplate> defaultTemplates = templateMapper.selectList(wrapper);
        for (ShippingTemplate t : defaultTemplates) {
            t.setIsDefault(0);
            t.setUpdatedAt(LocalDateTime.now());
            templateMapper.updateById(t);
        }

        // 设置当前模板为默认
        ShippingTemplate template = templateMapper.selectById(templateId);
        if (template != null && template.getMerchantId().equals(merchantId)) {
            template.setIsDefault(1);
            template.setUpdatedAt(LocalDateTime.now());
            templateMapper.updateById(template);
        }
    }

    /**
     * 删除运费模板
     */
    @Transactional
    public void deleteTemplate(Long id, Long merchantId) {
        ShippingTemplate template = templateMapper.selectOne(
                new LambdaQueryWrapper<ShippingTemplate>()
                        .eq(ShippingTemplate::getId, id)
                        .eq(ShippingTemplate::getMerchantId, merchantId)
                        .eq(ShippingTemplate::getDeleted, 0)
        );
        if (template == null) {
            throw new RuntimeException("运费模板不存在或无权限");
        }

        template.setDeleted(1);
        template.setUpdatedAt(LocalDateTime.now());
        templateMapper.updateById(template);
    }

    /**
     * 获取运费模板规则列表
     */
    public List<ShippingTemplateRule> getTemplateRules(Long templateId, Long merchantId) {
        // 验证模板属于当前商家
        ShippingTemplate template = getTemplate(templateId, merchantId);
        
        LambdaQueryWrapper<ShippingTemplateRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShippingTemplateRule::getTemplateId, templateId);
        return ruleMapper.selectList(wrapper);
    }

    /**
     * 删除运费模板规则
     */
    @Transactional
    public void deleteRule(Long ruleId, Long merchantId) {
        ShippingTemplateRule rule = ruleMapper.selectById(ruleId);
        if (rule == null) {
            throw new RuntimeException("规则不存在");
        }
        
        // 验证模板属于当前商家
        ShippingTemplate template = getTemplate(rule.getTemplateId(), merchantId);
        
        ruleMapper.deleteById(ruleId);
    }
}

