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
@TableName("t_operation_log")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String userType; // ADMIN/MERCHANT/USER

    private String username;

    private String operation; // CREATE/UPDATE/DELETE/AUDIT/LOGIN

    private String module; // MERCHANT/ORDER/PRODUCT/CONFIG

    private String description;

    private String requestUrl;

    private String requestMethod;

    private String ipAddress;

    private LocalDateTime createdAt;
}

