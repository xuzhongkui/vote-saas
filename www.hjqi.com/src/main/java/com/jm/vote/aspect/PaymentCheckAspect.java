package com.jm.vote.aspect;

import com.jm.vote.annotation.RequirePayment;
import com.jm.vote.entity.Merchant;
import com.jm.vote.repository.MerchantMapper;
import com.jm.vote.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;

/**
 * 缴费状态检查切面
 * 检查商家是否已完成缴费，未缴费则拒绝访问
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PaymentCheckAspect {

    private final SecurityUtil securityUtil;
    private final MerchantMapper merchantMapper;

    /**
     * 定义切点：所有标注了 @RequirePayment 的方法
     */
    @Pointcut("@annotation(com.jm.vote.annotation.RequirePayment)")
    public void requirePaymentPointcut() {
    }

    /**
     * 定义切点：所有标注了 @RequirePayment 的类
     */
    @Pointcut("@within(com.jm.vote.annotation.RequirePayment)")
    public void requirePaymentClassPointcut() {
    }

    /**
     * 在方法执行前检查缴费状态
     */
    @Before("requirePaymentPointcut() || requirePaymentClassPointcut()")
    public void checkPaymentStatus(JoinPoint joinPoint) {
        // 获取当前商家ID
        Long merchantId = securityUtil.getCurrentMerchantId();
        if (merchantId == null) {
            // 如果不是商家，可能是其他角色，不拦截
            return;
        }

        // 查询商家信息
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "商家不存在");
        }

        // 检查缴费状态：0-未缴费，1-已缴费，2-已退款
        Integer paymentStatus = merchant.getPaymentStatus();
        if (paymentStatus == null || paymentStatus == 0) {
            // 获取注解信息
            String message = getPaymentMessage(joinPoint);
            log.warn("商家未缴费，拒绝访问: merchantId={}, method={}", merchantId, joinPoint.getSignature().getName());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, message);
        }
    }

    /**
     * 获取注解中的错误消息
     */
    private String getPaymentMessage(JoinPoint joinPoint) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            
            // 先检查方法上的注解
            RequirePayment annotation = method.getAnnotation(RequirePayment.class);
            if (annotation != null) {
                return annotation.message();
            }
            
            // 再检查类上的注解
            annotation = joinPoint.getTarget().getClass().getAnnotation(RequirePayment.class);
            if (annotation != null) {
                return annotation.message();
            }
        } catch (Exception e) {
            log.error("获取缴费检查注解消息失败", e);
        }
        
        return "请先完成缴费后才能使用此功能";
    }
}

