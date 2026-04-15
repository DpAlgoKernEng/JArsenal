package com.example.demo.annotation;

import com.example.demo.domain.permission.valueobject.ActionType;
import java.lang.annotation.*;

/**
 * Batch permission check annotation
 * Applied to batch operation methods for permission verification
 * Supports data scope validation for each target item
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireBatchPermission {
    /**
     * Resource code to check permission for
     */
    String resourceCode();

    /**
     * Required action type (single action for batch operations)
     */
    ActionType action();

    /**
     * Parameter name containing target IDs
     * Used for data scope validation
     */
    String idParam() default "ids";

    /**
     * Custom error message
     */
    String message() default "无批量操作权限";
}