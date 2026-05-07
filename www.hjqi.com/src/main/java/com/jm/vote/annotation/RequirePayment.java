package com.jm.vote.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 要求商家必须完成缴费才能访问
 * 标注在需要缴费后才能使用的接口上
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePayment {
    /**
     * 错误消息
     */
    String message() default "请先完成缴费后才能使用此功能";
}

