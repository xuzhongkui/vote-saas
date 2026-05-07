package com.jm.vote.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jm.vote.dto.OperationLogDTO;
import com.jm.vote.entity.OperationLog;
import com.jm.vote.repository.OperationLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogMapper logMapper;

    @Transactional
    public void logOperation(String operation, String module, String description, HttpServletRequest request) {
        OperationLog logEntity = new OperationLog();
        
        // 从SecurityContext获取用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof org.springframework.security.core.userdetails.User userDetails) {
                logEntity.setUsername(userDetails.getUsername());
                logEntity.setUserType("ADMIN");
            } else if (principal instanceof String username) {
                logEntity.setUsername(username);
                // 可以从JWT中获取更多信息
                logEntity.setUserType("USER"); // 默认，实际应该从JWT解析
            }
        }
        
        logEntity.setOperation(operation);
        logEntity.setModule(module);
        logEntity.setDescription(description);
        if (request != null) {
            logEntity.setRequestUrl(request.getRequestURI());
            logEntity.setRequestMethod(request.getMethod());
            logEntity.setIpAddress(getClientIpAddress(request));
        }
        logEntity.setCreatedAt(LocalDateTime.now());
        logMapper.insert(logEntity);
        log.debug("操作日志已记录: {} - {} - {}", logEntity.getUsername(), operation, module);
    }

    /**
     * 记录操作日志（手动指定用户信息）
     * 用于登录等场景，此时SecurityContext中还没有用户信息
     */
    @Transactional
    public void logOperationWithUser(Long userId, String userType, String username, 
                                      String operation, String module, String description, 
                                      HttpServletRequest request) {
        OperationLog logEntity = new OperationLog();
        logEntity.setUserId(userId);
        logEntity.setUserType(userType);
        logEntity.setUsername(username);
        logEntity.setOperation(operation);
        logEntity.setModule(module);
        logEntity.setDescription(description);
        
        if (request != null) {
            logEntity.setRequestUrl(request.getRequestURI());
            logEntity.setRequestMethod(request.getMethod());
            logEntity.setIpAddress(getClientIpAddress(request));
        }
        logEntity.setCreatedAt(LocalDateTime.now());
        logMapper.insert(logEntity);
        log.debug("操作日志已记录: {} - {} - {}", username, operation, module);
    }

    public Page<OperationLogDTO> getLogs(String userType, String module, int page, int size) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        if (userType != null && !userType.isEmpty()) {
            wrapper.eq(OperationLog::getUserType, userType);
        }
        if (module != null && !module.isEmpty()) {
            wrapper.eq(OperationLog::getModule, module);
        }
        wrapper.orderByDesc(OperationLog::getCreatedAt);
        
        Page<OperationLog> logPage = new Page<>(page, size);
        Page<OperationLog> result = logMapper.selectPage(logPage, wrapper);
        Page<OperationLogDTO> dtoPage = new Page<>(page, size, result.getTotal());
        dtoPage.setRecords(result.getRecords().stream().map(this::toLogDTO).collect(Collectors.toList()));
        return dtoPage;
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private OperationLogDTO toLogDTO(OperationLog logEntity) {
        OperationLogDTO dto = new OperationLogDTO();
        dto.setId(logEntity.getId());
        dto.setUserId(logEntity.getUserId());
        dto.setUserType(logEntity.getUserType());
        dto.setUsername(logEntity.getUsername());
        dto.setOperation(logEntity.getOperation());
        dto.setModule(logEntity.getModule());
        dto.setDescription(logEntity.getDescription());
        dto.setRequestUrl(logEntity.getRequestUrl());
        dto.setRequestMethod(logEntity.getRequestMethod());
        dto.setIpAddress(logEntity.getIpAddress());
        dto.setCreatedAt(logEntity.getCreatedAt());
        return dto;
    }
}

