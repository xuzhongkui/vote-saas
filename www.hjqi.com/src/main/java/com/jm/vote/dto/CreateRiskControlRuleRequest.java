package com.jm.vote.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateRiskControlRuleRequest {
    @NotBlank(message = "规则名称不能为空")
    private String ruleName;
    
    @NotBlank(message = "规则类型不能为空")
    private String ruleType;
    
    @NotBlank(message = "规则配置不能为空")
    private String ruleConfig;
    
    @NotBlank(message = "触发动作不能为空")
    private String action;
    
    private Integer enabled = 1;
    
    @NotNull(message = "优先级不能为空")
    private Integer priority;
    
    private String description;
}

