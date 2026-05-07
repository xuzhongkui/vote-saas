package com.jm.vote.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_plan")
public class Plan {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String nameZh;

    private String nameEn;

    private String descriptionZh;

    private String descriptionEn;

    private BigDecimal price;

    private Integer durationDays;

    private String features;

    private Integer maxProducts;

    private Integer maxOrdersPerMonth;

    private Integer status;

    private Integer sort;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer deleted;
}

