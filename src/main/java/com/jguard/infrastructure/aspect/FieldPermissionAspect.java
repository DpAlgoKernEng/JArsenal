package com.jguard.infrastructure.aspect;

import com.jguard.infrastructure.annotation.FieldPermissionCheck;
import com.jguard.domain.permission.service.FieldPermissionDomainService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 字段权限切面
 * 自动处理响应数据的字段脱敏和编辑权限校验
 */
@Aspect
@Component
public class FieldPermissionAspect {

    private final FieldPermissionDomainService fieldPermissionDomainService;

    public FieldPermissionAspect(FieldPermissionDomainService fieldPermissionDomainService) {
        this.fieldPermissionDomainService = fieldPermissionDomainService;
    }

    @Around("@annotation(fieldPermissionCheck)")
    public Object processFieldPermissions(ProceedingJoinPoint joinPoint, FieldPermissionCheck fieldPermissionCheck) throws Throwable {
        String resourceCode = fieldPermissionCheck.resourceCode();

        // 如果需要检查编辑权限
        if (fieldPermissionCheck.checkEdit()) {
            Object[] args = joinPoint.getArgs();
            if (args.length > 0 && args[0] != null) {
                fieldPermissionDomainService.validateEditPermission(args[0], resourceCode);
            }
        }

        // 执行原方法
        Object result = joinPoint.proceed();

        // 处理响应数据的字段脱敏
        if (result != null) {
            result = fieldPermissionDomainService.processFieldPermissions(result, resourceCode);
        }

        return result;
    }
}