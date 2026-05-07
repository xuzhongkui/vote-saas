package com.jm.vote.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppDownloadDTO {
    private Long id;
    private String platform;
    private String version;
    private String versionName;
    private String downloadUrl;
    private String updateNotesZh;
    private String updateNotesEn;
    private Integer forceUpdate;
    private Long fileSize;
    private String fileMd5;
    private Integer enabled;
    private LocalDateTime publishTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

