package com.jm.vote.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * App下载管理实体
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_app_download")
public class AppDownload {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 平台类型：IOS/ANDROID
     */
    private String platform;

    /**
     * 版本号
     */
    private String version;

    /**
     * 版本名称
     */
    private String versionName;

    /**
     * 下载链接
     */
    private String downloadUrl;

    /**
     * 更新说明（中文）
     */
    private String updateNotesZh;

    /**
     * 更新说明（英文）
     */
    private String updateNotesEn;

    /**
     * 是否强制更新：0-否 1-是
     */
    private Integer forceUpdate;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件MD5
     */
    private String fileMd5;

    /**
     * 是否启用：0-禁用 1-启用
     */
    private Integer enabled;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer deleted;
}

