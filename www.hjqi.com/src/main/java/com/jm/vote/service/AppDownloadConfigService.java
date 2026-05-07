package com.jm.vote.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jm.vote.entity.AppDownloadConfig;
import com.jm.vote.repository.AppDownloadConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppDownloadConfigService {

    private final AppDownloadConfigMapper configMapper;

    /**
     * 创建或更新App下载配置
     */
    @Transactional
    public AppDownloadConfig saveConfig(Long merchantId, String platform, String version, 
                                       String downloadUrl, String updateLogZh, String updateLogEn, 
                                       Boolean forceUpdate) {
        // 查找现有配置
        AppDownloadConfig config = configMapper.selectOne(
                new LambdaQueryWrapper<AppDownloadConfig>()
                        .eq(merchantId != null, AppDownloadConfig::getMerchantId, merchantId)
                        .isNull(merchantId == null, AppDownloadConfig::getMerchantId)
                        .eq(AppDownloadConfig::getPlatform, platform)
                        .eq(AppDownloadConfig::getStatus, 1)
        );
        
        if (config == null) {
            config = AppDownloadConfig.builder()
                    .merchantId(merchantId)
                    .platform(platform)
                    .version(version)
                    .downloadUrl(downloadUrl)
                    .updateLogZh(updateLogZh)
                    .updateLogEn(updateLogEn)
                    .forceUpdate(forceUpdate != null && forceUpdate ? 1 : 0)
                    .status(1)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            configMapper.insert(config);
        } else {
            config.setVersion(version);
            config.setDownloadUrl(downloadUrl);
            config.setUpdateLogZh(updateLogZh);
            config.setUpdateLogEn(updateLogEn);
            config.setForceUpdate(forceUpdate != null && forceUpdate ? 1 : 0);
            config.setUpdatedAt(LocalDateTime.now());
            configMapper.updateById(config);
        }
        
        return config;
    }

    /**
     * 获取App下载配置
     */
    public AppDownloadConfig getConfig(Long merchantId, String platform) {
        return configMapper.selectOne(
                new LambdaQueryWrapper<AppDownloadConfig>()
                        .eq(merchantId != null, AppDownloadConfig::getMerchantId, merchantId)
                        .isNull(merchantId == null, AppDownloadConfig::getMerchantId)
                        .eq(AppDownloadConfig::getPlatform, platform)
                        .eq(AppDownloadConfig::getStatus, 1)
                        .orderByDesc(AppDownloadConfig::getCreatedAt)
                        .last("LIMIT 1")
        );
    }

    /**
     * 获取所有平台配置
     */
    public List<AppDownloadConfig> getAllConfigs(Long merchantId) {
        return configMapper.selectList(
                new LambdaQueryWrapper<AppDownloadConfig>()
                        .eq(merchantId != null, AppDownloadConfig::getMerchantId, merchantId)
                        .isNull(merchantId == null, AppDownloadConfig::getMerchantId)
                        .eq(AppDownloadConfig::getStatus, 1)
                        .orderByDesc(AppDownloadConfig::getCreatedAt)
        );
    }
}

