package com.example.demo.infrastructure.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 限流注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * 限流key前缀
     */
    String key() default "";

    /**
     * 时间窗口（秒）
     */
    int time() default 60;

    /**
     * 限制次数
     */
    int count() default 10;

    /**
     * 限流类型
     */
    LimitType limitType() default LimitType.DEFAULT;

    enum LimitType {
        DEFAULT,    // 默认全局限流
        IP,         // 按 IP 限流
        USER        // 按 用户限流
    }
}