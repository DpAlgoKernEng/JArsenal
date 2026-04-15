package com.example.demo.annotation;

import com.example.demo.domain.permission.valueobject.ActionType;
import java.lang.annotation.*;

/**
 * Permission check annotation
 * Applied to controller methods for functional permission verification
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {
    /**
     * Resource code to check permission for
     */
    String resourceCode();

    /**
     * Required action types
     * User must have ALL specified actions to pass
     */
    ActionType[] actions();

    /**
     * Custom error message
     */
    String message() default "无操作权限";
}