package com.jm.vote.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jm.vote.entity.PaymentInfoConfig;
import com.jm.vote.repository.PaymentInfoConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentInfoConfigService {

    private final PaymentInfoConfigMapper configMapper;

    /**
     * 创建支付说明配置
     */
    @Transactional
    public PaymentInfoConfig createConfig(Long merchantId, String language, String title, String content, Integer sort) {
        PaymentInfoConfig config = PaymentInfoConfig.builder()
                .merchantId(merchantId)
                .language(language != null ? language : "zh")
                .title(title)
                .content(content)
                .sort(sort != null ? sort : 0)
                .status(1)
                .deleted(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        configMapper.insert(config);
        return config;
    }

    /**
     * 获取支付说明列表
     */
    public Page<PaymentInfoConfig> getConfigs(Long merchantId, String language, int page, int size) {
        LambdaQueryWrapper<PaymentInfoConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(merchantId != null, PaymentInfoConfig::getMerchantId, merchantId)
                .isNull(merchantId == null, PaymentInfoConfig::getMerchantId)
                .eq(PaymentInfoConfig::getDeleted, 0)
                .eq(PaymentInfoConfig::getStatus, 1);
        
        if (language != null && !language.isEmpty()) {
            wrapper.eq(PaymentInfoConfig::getLanguage, language);
        }
        
        wrapper.orderByAsc(PaymentInfoConfig::getSort)
                .orderByDesc(PaymentInfoConfig::getCreatedAt);
        
        return configMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 更新配置
     */
    @Transactional
    public PaymentInfoConfig updateConfig(Long id, Long merchantId, String title, String content, Integer sort, Integer status) {
        PaymentInfoConfig config = configMapper.selectById(id);
        if (config == null || (merchantId != null && !config.getMerchantId().equals(merchantId))) {
            throw new RuntimeException("配置不存在或无权限");
        }
        
        if (title != null) config.setTitle(title);
        if (content != null) config.setContent(content);
        if (sort != null) config.setSort(sort);
        if (status != null) config.setStatus(status);
        
        config.setUpdatedAt(LocalDateTime.now());
        configMapper.updateById(config);
        return config;
    }

    /**
     * 删除配置
     */
    @Transactional
    public void deleteConfig(Long id, Long merchantId) {
        PaymentInfoConfig config = configMapper.selectById(id);
        if (config == null || (merchantId != null && !config.getMerchantId().equals(merchantId))) {
            throw new RuntimeException("配置不存在或无权限");
        }
        
        config.setDeleted(1);
        config.setUpdatedAt(LocalDateTime.now());
        configMapper.updateById(config);
    }
}

