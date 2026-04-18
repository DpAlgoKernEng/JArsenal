package com.jguard.infrastructure.persistence.annotation;

import java.lang.annotation.*;

/**
 * 数据权限注解
 * 用于 Mapper 方法上标记需要应用数据权限过滤
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {

    /**
     * 数据维度编码（如 DEPARTMENT, PROJECT, CUSTOMER）
     */
    String dimension() default "DEPARTMENT";

    /**
     * 表别名（用于多表查询时指定表别名）
     */
    String tableAlias() default "";

    /**
     * 过滤字段名（如 dept_id, project_id）
     */
    String column() default "";
}