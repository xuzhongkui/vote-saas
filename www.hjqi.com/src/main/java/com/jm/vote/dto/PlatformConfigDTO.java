package com.jm.vote.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PlatformConfigDTO {
    private Long id;
    private String configKey;
    private String configValue;
    private String nameZh;
    private String nameEn;
    private String description;
    private String configType;
    private String configGroup;
    private Integer sort;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

