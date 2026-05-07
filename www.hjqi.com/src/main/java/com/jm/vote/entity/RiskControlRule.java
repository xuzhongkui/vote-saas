package com.jm.vote.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 风控规则实体
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_risk_control_rule")
public class RiskControlRule {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 规则类型：ORDER_AMOUNT/ORDER_FREQUENCY/IP_BLACKLIST/DEVICE_BLACKLIST/USER_BLACKLIST等
     */
    private String ruleType;

    /**
     * 规则配置（JSON格式）
     * 例如：{"maxAmount": 10000, "maxCount": 10, "timeWindow": 3600}
     */
    private String ruleConfig;

    /**
     * 触发动作：BLOCK/ALERT/REVIEW
     */
    private String action;

    /**
     * 是否启用：0-禁用 1-启用
     */
    private Integer enabled;

    /**
     * 优先级（数字越大优先级越高）
     */
    private Integer priority;

    /**
     * 规则描述
     */
    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer deleted;
}

