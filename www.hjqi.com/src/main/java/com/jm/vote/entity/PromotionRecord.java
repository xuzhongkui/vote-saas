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
@TableName("t_promotion_record")
public class PromotionRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long merchantId;

    private Long inviterId; // 邀请人ID（商家或用户）

    private Long inviteeId; // 被邀请人ID（商家或用户）

    private String inviteeType; // MERCHANT/USER

    private String promotionCode;

    private Integer rewardAmount; // 奖励金额（分）

    private String rewardType; // FIXED-固定金额 PERCENTAGE-百分比

    private BigDecimal rewardRate; // 奖励比例（百分比）

    private BigDecimal orderAmount; // 关联订单金额

    private Integer status; // 0-待确认 1-已确认 2-已取消

    private LocalDateTime createdAt;

    private LocalDateTime confirmedAt;

    private LocalDateTime settlementTime;

    private String remark;
}

