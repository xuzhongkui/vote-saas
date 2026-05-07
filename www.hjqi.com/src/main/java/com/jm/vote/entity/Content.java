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
@TableName("t_content")
public class Content {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String contentType; // SEO/ACTIVITY/NEWS/ANNOUNCEMENT

    private String titleZh;

    private String titleEn;

    private String contentZh;

    private String contentEn;

    private String coverImage;

    private String linkUrl;

    private Integer sort;

    private Integer status; // 0-禁用 1-启用

    private LocalDateTime publishAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer deleted;
}

