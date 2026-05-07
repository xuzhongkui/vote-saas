package com.jm.vote.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_app_download_config")
public class AppDownloadConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long merchantId;

    private String platform;

    private String version;

    private String downloadUrl;

    private String updateLogZh;

    private String updateLogEn;

    private Integer forceUpdate;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

