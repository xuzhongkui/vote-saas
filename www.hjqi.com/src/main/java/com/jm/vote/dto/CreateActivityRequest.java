package com.jm.vote.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CreateActivityRequest {
    @NotBlank(message = "活动名称（中文）不能为空")
    private String nameZh;
    
    private String nameEn;
    
    private String descriptionZh;
    
    private String descriptionEn;
    
    @NotBlank(message = "活动类型不能为空")
    private String activityType;
    
    private String coverImage;
    
    private String h5Url;
    
    private String pcUrl;
    
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;
    
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;
    
    private Integer enabled = 1;
    
    private Integer sort = 0;
    
    private Long merchantId;
}

