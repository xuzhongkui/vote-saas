package com.jm.vote.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateSystemConfigRequest {
    @NotBlank(message = "配置键不能为空")
    private String configKey;
    
    private String configValue;
    
    private String nameZh;
    
    private String nameEn;
    
    private String description;
    
    @NotBlank(message = "配置类型不能为空")
    private String configType; // STRING/NUMBER/BOOLEAN/JSON
    
    private String configGroup;
    
    private Integer sort = 0;
}

