package com.jm.vote.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RiskControlRuleDTO {
    private Long id;
    private String ruleName;
    private String ruleType;
    private String ruleConfig;
    private String action;
    private Integer enabled;
    private Integer priority;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

