package com.jguard.infrastructure.annotation;

import com.jguard.domain.audit.valueobject.ModuleType;
import com.jguard.domain.audit.valueobject.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 审计日志注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {

    /**
     * 操作类型
     */
    OperationType operation();

    /**
     * 模块
     */
    ModuleType module();

    /**
     * 操作描述
     */
    String description() default "";
}