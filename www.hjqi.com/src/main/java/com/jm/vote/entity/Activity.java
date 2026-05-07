package com.jm.vote.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 活动实体
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_activity")
public class Activity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 活动编号
     */
    private String activityNo;

    /**
     * 活动名称（中文）
     */
    private String nameZh;

    /**
     * 活动名称（英文）
     */
    private String nameEn;

    /**
     * 活动描述（中文）
     */
    private String descriptionZh;

    /**
     * 活动描述（英文）
     */
    private String descriptionEn;

    /**
     * 活动类型：DISCOUNT/COUPON/GIFT/FREE_SHIPPING/NEW_USER等
     */
    private String activityType;

    /**
     * 活动封面图
     */
    private String coverImage;

    /**
     * 活动链接（H5）
     */
    private String h5Url;

    /**
     * 活动链接（PC）
     */
    private String pcUrl;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 活动状态：0-未开始 1-进行中 2-已结束 3-已取消
     */
    private Integer status;

    /**
     * 是否启用：0-禁用 1-启用
     */
    private Integer enabled;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 参与商家ID（为空表示全平台活动）
     */
    private Long merchantId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer deleted;
}

