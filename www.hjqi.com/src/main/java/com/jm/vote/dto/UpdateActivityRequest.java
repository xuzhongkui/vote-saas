package com.jm.vote.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UpdateActivityRequest {
    private String nameZh;
    private String nameEn;
    private String descriptionZh;
    private String descriptionEn;
    private String activityType;
    private String coverImage;
    private String h5Url;
    private String pcUrl;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private Integer enabled;
    private Integer sort;
    private Long merchantId;
}

