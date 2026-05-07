package com.jm.vote.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jm.vote.entity.MerchantNotificationConfig;
import com.jm.vote.repository.MerchantNotificationConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MerchantNotificationConfigService {

    private final MerchantNotificationConfigMapper configMapper;

    /**
     * 获取通知配置列表
     */
    public List<MerchantNotificationConfig> getConfigs(Long merchantId) {
        LambdaQueryWrapper<MerchantNotificationConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MerchantNotificationConfig::getMerchantId, merchantId)
                .eq(MerchantNotificationConfig::getDeleted, 0)
                .orderByAsc(MerchantNotificationConfig::getNotificationType)
                .orderByAsc(MerchantNotificationConfig::getNotificationChannel);
        List<MerchantNotificationConfig> configs = configMapper.selectList(wrapper);
        
        // 如果没有配置，初始化默认配置
        if (configs.isEmpty()) {
            initializeDefaultConfigs(merchantId);
            // 重新查询
            configs = configMapper.selectList(wrapper);
        }
        
        return configs;
    }
    
    /**
     * 初始化默认通知配置
     */
    @Transactional
    public void initializeDefaultConfigs(Long merchantId) {
        // 定义所有通知类型和渠道的组合
        String[] notificationTypes = {
            "ORDER_CREATED",      // 订单创建
            "ORDER_CONFIRMED",    // 订单确认
            "ORDER_SHIPPED",      // 订单发货
            "ORDER_COMPLETED",    // 订单完成
            "ORDER_CANCELLED",    // 订单取消
            "PAYMENT_SUCCESS",    // 支付成功
            "PAYMENT_FAILED"      // 支付失败
        };
        
        String[] notificationChannels = {
            "EMAIL",  // 邮件
            "SMS",    // 短信
            "PUSH",   // 推送
            "WECHAT"  // 微信
        };
        
        LocalDateTime now = LocalDateTime.now();
        
        // 为每个通知类型和渠道组合创建默认配置
        for (String type : notificationTypes) {
            for (String channel : notificationChannels) {
                MerchantNotificationConfig config = MerchantNotificationConfig.builder()
                        .merchantId(merchantId)
                        .notificationType(type)
                        .notificationChannel(channel)
                        .enabled(channel.equals("EMAIL") ? 1 : 0) // 默认只启用邮件通知
                        .configJson(null)
                        .deleted(0)
                        .createdAt(now)
                        .updatedAt(now)
                        .build();
                configMapper.insert(config);
            }
        }
    }

    /**
     * 获取通知配置（按类型分组）
     */
    public Map<String, List<MerchantNotificationConfig>> getConfigsGrouped(Long merchantId) {
        List<MerchantNotificationConfig> configs = getConfigs(merchantId);
        return configs.stream()
                .collect(Collectors.groupingBy(MerchantNotificationConfig::getNotificationType));
    }

    /**
     * 创建或更新通知配置
     */
    @Transactional
    public MerchantNotificationConfig saveConfig(
            Long merchantId,
            String notificationType,
            String notificationChannel,
            Integer enabled,
            String configJson) {
        
        LambdaQueryWrapper<MerchantNotificationConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MerchantNotificationConfig::getMerchantId, merchantId)
                .eq(MerchantNotificationConfig::getNotificationType, notificationType)
                .eq(MerchantNotificationConfig::getNotificationChannel, notificationChannel)
                .eq(MerchantNotificationConfig::getDeleted, 0);
        
        MerchantNotificationConfig config = configMapper.selectOne(wrapper);
        
        if (config == null) {
            config = MerchantNotificationConfig.builder()
                    .merchantId(merchantId)
                    .notificationType(notificationType)
                    .notificationChannel(notificationChannel)
                    .enabled(enabled != null ? enabled : 1)
                    .configJson(configJson)
                    .deleted(0)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            configMapper.insert(config);
        } else {
            if (enabled != null) config.setEnabled(enabled);
            if (configJson != null) config.setConfigJson(configJson);
            config.setUpdatedAt(LocalDateTime.now());
            configMapper.updateById(config);
        }
        
        return config;
    }

    /**
     * 批量保存通知配置
     */
    @Transactional
    public void saveConfigs(Long merchantId, List<SaveNotificationConfigRequest> requests) {
        for (SaveNotificationConfigRequest request : requests) {
            saveConfig(
                    merchantId,
                    request.getNotificationType(),
                    request.getNotificationChannel(),
                    request.getEnabled(),
                    request.getConfigJson()
            );
        }
    }

    /**
     * 删除通知配置
     */
    @Transactional
    public void deleteConfig(Long id, Long merchantId) {
        MerchantNotificationConfig config = configMapper.selectById(id);
        if (config == null || !config.getMerchantId().equals(merchantId)) {
            throw new RuntimeException("配置不存在或无权限");
        }

        config.setDeleted(1);
        config.setUpdatedAt(LocalDateTime.now());
        configMapper.updateById(config);
    }

    @lombok.Data
    public static class SaveNotificationConfigRequest {
        private String notificationType;
        private String notificationChannel;
        private Integer enabled;
        private String configJson;
    }
}

