package com.jm.vote.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jm.vote.entity.SystemConfig;
import com.jm.vote.repository.SystemConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemConfigService {

    private final SystemConfigMapper systemConfigMapper;

    /**
     * 获取配置值（字符串）
     */
    @Cacheable(value = "systemConfig", key = "#configKey")
    public String getString(String configKey, String defaultValue) {
        SystemConfig config = getConfig(configKey);
        return config != null ? config.getConfigValue() : defaultValue;
    }

    /**
     * 获取配置值（布尔）
     */
    @Cacheable(value = "systemConfig", key = "#configKey")
    public Boolean getBoolean(String configKey, Boolean defaultValue) {
        SystemConfig config = getConfig(configKey);
        if (config == null) {
            return defaultValue;
        }
        String value = config.getConfigValue();
        if (value == null) {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }

    /**
     * 获取配置值（整数）
     */
    @Cacheable(value = "systemConfig", key = "#configKey")
    public Integer getInteger(String configKey, Integer defaultValue) {
        SystemConfig config = getConfig(configKey);
        if (config == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(config.getConfigValue());
        } catch (NumberFormatException e) {
            log.warn("配置值转换失败: {} = {}", configKey, config.getConfigValue());
            return defaultValue;
        }
    }

    /**
     * 获取配置值（小数）
     */
    @Cacheable(value = "systemConfig", key = "#configKey")
    public BigDecimal getDecimal(String configKey, BigDecimal defaultValue) {
        SystemConfig config = getConfig(configKey);
        if (config == null) {
            return defaultValue;
        }
        try {
            return new BigDecimal(config.getConfigValue());
        } catch (NumberFormatException e) {
            log.warn("配置值转换失败: {} = {}", configKey, config.getConfigValue());
            return defaultValue;
        }
    }

    /**
     * 获取配置对象
     */
    private SystemConfig getConfig(String configKey) {
        return systemConfigMapper.selectOne(
                new LambdaQueryWrapper<SystemConfig>()
                        .eq(SystemConfig::getConfigKey, configKey)
                        .eq(SystemConfig::getDeleted, 0)
        );
    }

    /**
     * 设置配置值
     */
    @CacheEvict(value = "systemConfig", key = "#configKey")
    public void setConfig(String configKey, String configValue) {
        SystemConfig config = getConfig(configKey);
        if (config != null) {
            config.setConfigValue(configValue);
            systemConfigMapper.updateById(config);
        } else {
            config = new SystemConfig();
            config.setConfigKey(configKey);
            config.setConfigValue(configValue);
            config.setConfigType("STRING");
            config.setDeleted(0);
            systemConfigMapper.insert(config);
        }
    }

    /**
     * 批量获取配置（按分组）
     */
    public Map<String, String> getConfigsByGroup(String configGroup) {
        List<SystemConfig> configs = systemConfigMapper.selectList(
                new LambdaQueryWrapper<SystemConfig>()
                        .eq(SystemConfig::getConfigGroup, configGroup)
                        .eq(SystemConfig::getDeleted, 0)
                        .orderByAsc(SystemConfig::getSort)
        );
        
        Map<String, String> result = new HashMap<>();
        for (SystemConfig config : configs) {
            result.put(config.getConfigKey(), config.getConfigValue());
        }
        return result;
    }

    /**
     * 获取所有配置
     */
    public List<SystemConfig> getAllConfigs() {
        return systemConfigMapper.selectList(
                new LambdaQueryWrapper<SystemConfig>()
                        .eq(SystemConfig::getDeleted, 0)
                        .orderByAsc(SystemConfig::getConfigGroup, SystemConfig::getSort)
        );
    }

    /**
     * 商家相关配置
     */
    public boolean isMerchantAuditEnabled() {
        return getBoolean("merchant.audit.enabled", true);
    }

    public BigDecimal getMerchantRegistrationFee() {
        return getDecimal("merchant.registration.fee", new BigDecimal("1000.00"));
    }

    public boolean isMerchantRegistrationFeeEnabled() {
        return getBoolean("merchant.registration.fee.enabled", true);
    }

    public BigDecimal getMerchantPromotionReward() {
        return getDecimal("merchant.promotion.reward.merchant", new BigDecimal("100.00"));
    }

    public BigDecimal getUserPromotionRewardRate() {
        return getDecimal("merchant.promotion.reward.user", new BigDecimal("5.00"));
    }

    public boolean isMerchantPromotionEnabled() {
        return getBoolean("merchant.promotion.enabled", true);
    }

    public boolean isInvoiceAutoGenerate() {
        return getBoolean("merchant.invoice.auto.generate", true);
    }

    public BigDecimal getInvoiceTaxRate() {
        return getDecimal("merchant.invoice.tax.rate", new BigDecimal("6.00"));
    }

    public boolean isWelcomeEmailEnabled() {
        return getBoolean("merchant.welcome.email.enabled", true);
    }

    public boolean isQrCodeAutoGenerate() {
        return getBoolean("merchant.qrcode.auto.generate", true);
    }

    /**
     * 清除所有配置缓存
     */
    @CacheEvict(value = "systemConfig", allEntries = true)
    public void clearCache() {
        log.info("系统配置缓存已清除");
    }
}

