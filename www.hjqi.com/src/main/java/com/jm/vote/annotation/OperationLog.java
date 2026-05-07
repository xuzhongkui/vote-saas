package com.jm.vote.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 用于标记需要记录操作日志的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {
    
    /**
     * 操作类型
     * CREATE/UPDATE/DELETE/AUDIT/LOGIN/QUERY/EXPORT
     */
    String operation();
    
    /**
     * 模块名称
     * MERCHANT/ORDER/PRODUCT/CONFIG/USER/SYSTEM/WITHDRAWAL
     */
    String module();
    
    /**
     * 操作描述
     */
    String description() default "";
}

