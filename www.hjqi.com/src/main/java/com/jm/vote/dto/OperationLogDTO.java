package com.jm.vote.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OperationLogDTO {
    private Long id;
    private Long userId;
    private String userType;
    private String username;
    private String operation;
    private String module;
    private String description;
    private String requestUrl;
    private String requestMethod;
    private String ipAddress;
    private LocalDateTime createdAt;
}

