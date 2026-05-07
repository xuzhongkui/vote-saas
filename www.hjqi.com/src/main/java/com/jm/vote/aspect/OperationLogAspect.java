package com.jm.vote.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jm.vote.annotation.OperationLog;
import com.jm.vote.entity.Admin;
import com.jm.vote.entity.Merchant;
import com.jm.vote.entity.User;
import com.jm.vote.repository.AdminMapper;
import com.jm.vote.repository.MerchantMapper;
import com.jm.vote.repository.OperationLogMapper;
import com.jm.vote.repository.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 操作日志切面
 * 自动记录带有 @OperationLog 注解的方法调用
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogMapper operationLogMapper;
    private final AdminMapper adminMapper;
    private final MerchantMapper merchantMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    /**
     * 切点：所有带有 @OperationLog 注解的方法
     */
    @Pointcut("@annotation(com.jm.vote.annotation.OperationLog)")
    public void operationLogPointcut() {
    }

    /**
     * 方法执行成功后记录日志
     */
    @AfterReturning(pointcut = "operationLogPointcut()", returning = "result")
    public void afterReturning(JoinPoint joinPoint, Object result) {
        try {
            recordLog(joinPoint, null);
        } catch (Exception e) {
            log.error("记录操作日志失败", e);
        }
    }

    /**
     * 记录操作日志
     */
    private void recordLog(JoinPoint joinPoint, Exception exception) {
        // 获取方法上的注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperationLog operationLogAnnotation = method.getAnnotation(OperationLog.class);
        
        if (operationLogAnnotation == null) {
            return;
        }

        // 创建日志实体
        com.jm.vote.entity.OperationLog logEntity = new com.jm.vote.entity.OperationLog();
        
        // 设置操作信息
        logEntity.setOperation(operationLogAnnotation.operation());
        logEntity.setModule(operationLogAnnotation.module());
        
        // 处理描述，支持动态参数
        String description = operationLogAnnotation.description();
        if (description.isEmpty()) {
            description = generateDescription(operationLogAnnotation.operation(), operationLogAnnotation.module());
        }
        logEntity.setDescription(description);

        // 获取当前用户信息
        setUserInfo(logEntity);

        // 获取请求信息
        setRequestInfo(logEntity);

        // 设置创建时间
        logEntity.setCreatedAt(LocalDateTime.now());

        // 保存日志
        operationLogMapper.insert(logEntity);
        log.debug("操作日志已记录: {} - {} - {}", logEntity.getUsername(), logEntity.getOperation(), logEntity.getModule());
    }

    /**
     * 设置用户信息
     */
    private void setUserInfo(com.jm.vote.entity.OperationLog logEntity) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            logEntity.setUsername("anonymous");
            logEntity.setUserType("UNKNOWN");
            return;
        }

        String username = authentication.getName();
        logEntity.setUsername(username);

        // 判断用户类型并获取用户ID
        // 先尝试管理员
        Admin admin = adminMapper.selectOne(
                new LambdaQueryWrapper<Admin>()
                        .eq(Admin::getUsername, username)
                        .eq(Admin::getDeleted, 0)
        );
        if (admin != null) {
            logEntity.setUserId(admin.getId());
            logEntity.setUserType("ADMIN");
            return;
        }

        // 再尝试商家
        Merchant merchant = merchantMapper.selectOne(
                new LambdaQueryWrapper<Merchant>()
                        .eq(Merchant::getUsername, username)
                        .eq(Merchant::getDeleted, 0)
        );
        if (merchant != null) {
            logEntity.setUserId(merchant.getId());
            logEntity.setUserType("MERCHANT");
            return;
        }

        // 最后尝试用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
                        .eq(User::getDeleted, 0)
        );
        if (user != null) {
            logEntity.setUserId(user.getId());
            logEntity.setUserType("USER");
            return;
        }

        logEntity.setUserType("UNKNOWN");
    }

    /**
     * 设置请求信息
     */
    private void setRequestInfo(com.jm.vote.entity.OperationLog logEntity) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }

        HttpServletRequest request = attributes.getRequest();
        logEntity.setRequestUrl(request.getRequestURI());
        logEntity.setRequestMethod(request.getMethod());
        logEntity.setIpAddress(getClientIpAddress(request));
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果是多个代理，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 生成默认描述
     */
    private String generateDescription(String operation, String module) {
        String operationName = switch (operation) {
            case "CREATE" -> "创建";
            case "UPDATE" -> "更新";
            case "DELETE" -> "删除";
            case "AUDIT" -> "审核";
            case "LOGIN" -> "登录";
            case "QUERY" -> "查询";
            case "EXPORT" -> "导出";
            default -> operation;
        };

        String moduleName = switch (module) {
            case "MERCHANT" -> "商家";
            case "ORDER" -> "订单";
            case "PRODUCT" -> "商品";
            case "CONFIG" -> "系统配置";
            case "USER" -> "用户";
            case "SYSTEM" -> "系统";
            case "WITHDRAWAL" -> "提现";
            case "CATEGORY" -> "分类";
            case "SKU" -> "SKU";
            default -> module;
        };

        return operationName + moduleName;
    }
}

