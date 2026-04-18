package com.jguard.infrastructure.annotation;

import java.lang.annotation.*;

/**
 * 字段权限检查注解
 * 应用在服务方法上，自动处理响应数据的字段脱敏
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FieldPermissionCheck {
    /**
     * 资源编码
     */
    String resourceCode();

    /**
     * 是否检查编辑权限（用于更新操作）
     */
    boolean checkEdit() default false;
}