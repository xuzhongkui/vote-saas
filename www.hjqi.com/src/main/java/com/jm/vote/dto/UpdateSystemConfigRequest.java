package com.jm.vote.dto;

import lombok.Data;

@Data
public class UpdateSystemConfigRequest {
    private String configValue;
    private String nameZh;
    private String nameEn;
    private String description;
    private String configType;
    private String configGroup;
    private Integer sort;
}

