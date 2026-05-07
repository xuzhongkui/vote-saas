package com.jm.vote.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CreateAppDownloadRequest {
    @NotBlank(message = "平台类型不能为空")
    private String platform;
    
    @NotBlank(message = "版本号不能为空")
    private String version;
    
    private String versionName;
    
    @NotBlank(message = "下载链接不能为空")
    private String downloadUrl;
    
    private String updateNotesZh;
    
    private String updateNotesEn;
    
    private Integer forceUpdate = 0;
    
    private Long fileSize;
    
    private String fileMd5;
    
    private Integer enabled = 1;
    
    private LocalDateTime publishTime;
}

