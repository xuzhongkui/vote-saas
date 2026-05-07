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
@TableName("t_email_template")
public class EmailTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String templateKey;

    private String nameZh;

    private String nameEn;

    private String subjectZh;

    private String subjectEn;

    private String contentZh;

    private String contentEn;

    private String variables;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

