package com.jm.vote.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 平台全局配置实体
 * 用于存储SEO设置、多语言配置、平台基础信息等
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_platform_config")
public class PlatformConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 配置键（唯一）
     */
    private String configKey;

    /**
     * 配置值（JSON格式，支持复杂数据结构）
     */
    private String configValue;

    /**
     * 配置名称（中文）
     */
    private String nameZh;

    /**
     * 配置名称（英文）
     */
    private String nameEn;

    /**
     * 配置描述
     */
    private String description;

    /**
     * 配置类型：SEO/PAYMENT/RISK_CONTROL/APP_DOWNLOAD/BASIC_INFO
     */
    private String configType;

    /**
     * 配置分组：SEO_META/SEO_KEYWORDS/PAYMENT_WECHAT/PAYMENT_ALIPAY/PAYMENT_BANK/PAYMENT_PAYPAL等
     */
    private String configGroup;

    /**
     * 排序
     */
    private Integer sort;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer deleted;
}

