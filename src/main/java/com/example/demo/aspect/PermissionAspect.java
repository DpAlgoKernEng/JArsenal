package com.example.demo.aspect;

import com.example.demo.annotation.RequirePermission;
import com.example.demo.annotation.RequireBatchPermission;
import com.example.demo.domain.permission.service.PermissionCacheService;
import com.example.demo.domain.permission.valueobject.ActionType;
import com.example.demo.domain.permission.valueobject.PermissionBitmap;
import com.example.demo.domain.permission.repository.ResourceRepository;
import com.example.demo.exception.BusinessException;
import com.example.demo.security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Permission aspect for @RequirePermission and @RequireBatchPermission annotations
 * Handles method-level permission verification with bitmap-based checks
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {

    private final PermissionCacheService permissionCache;
    private final ResourceRepository resourceRepository;

    /**
     * Check permission for @RequirePermission annotation
     * User must have ALL specified actions for the resource
     */
    @Around("@annotation(requirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) throws Throwable {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }

        PermissionBitmap bitmap = permissionCache.getPermissionBitmap(userId);

        Long resourceId = resourceRepository.findByCode(requirePermission.resourceCode())
            .orElseThrow(() -> new BusinessException(500, "资源不存在: " + requirePermission.resourceCode()))
            .getId();

        for (ActionType action : requirePermission.actions()) {
            if (!bitmap.hasAction(resourceId, action)) {
                log.warn("Permission denied: userId={}, resource={}, action={}",
                        userId, requirePermission.resourceCode(), action);
                throw new BusinessException(403, requirePermission.message());
            }
        }

        log.debug("Permission granted: userId={}, resource={}, actions={}",
                userId, requirePermission.resourceCode(), Arrays.toString(requirePermission.actions()));
        return joinPoint.proceed();
    }

    /**
     * Check permission for @RequireBatchPermission annotation
     * Single action check with optional data scope validation
     *
     * Note: Data scope validation (DataScopeService) is a P4 dependency
     * and will be integrated when P4 is implemented
     */
    @Around("@annotation(requireBatchPermission)")
    public Object checkBatchPermission(ProceedingJoinPoint joinPoint, RequireBatchPermission requireBatchPermission) throws Throwable {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }

        PermissionBitmap bitmap = permissionCache.getPermissionBitmap(userId);

        Long resourceId = resourceRepository.findByCode(requireBatchPermission.resourceCode())
            .orElseThrow(() -> new BusinessException(500, "资源不存在: " + requireBatchPermission.resourceCode()))
            .getId();

        ActionType action = requireBatchPermission.action();
        if (!bitmap.hasAction(resourceId, action)) {
            log.warn("Batch permission denied: userId={}, resource={}, action={}",
                    userId, requireBatchPermission.resourceCode(), action);
            throw new BusinessException(403, requireBatchPermission.message());
        }

        // Extract target IDs from method parameters
        Set<Long> targetIds = extractTargetIds(joinPoint, requireBatchPermission.idParam());

        // TODO: Data scope validation will be added when P4 (DataScopeDomainService) is implemented
        // For now, functional permission check is sufficient
        if (!targetIds.isEmpty()) {
            log.debug("Batch permission granted: userId={}, resource={}, action={}, targetIds={}",
                    userId, requireBatchPermission.resourceCode(), action, targetIds);
        }

        return joinPoint.proceed();
    }

    /**
     * Extract target IDs from method parameters
     * Supports List<Long>, Set<Long>, Long[], and individual Long parameters
     */
    private Set<Long> extractTargetIds(ProceedingJoinPoint joinPoint, String idParamName) {
        Set<Long> ids = new HashSet<>();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        // Find the parameter by name
        for (int i = 0; i < paramNames.length && i < args.length; i++) {
            if (paramNames[i].equals(idParamName)) {
                extractIdsFromArg(args[i], ids);
                return ids;
            }
        }

        // Fallback: search all args for ID collections
        for (Object arg : args) {
            extractIdsFromArg(arg, ids);
        }

        return ids;
    }

    /**
     * Extract IDs from a single argument
     */
    private void extractIdsFromArg(Object arg, Set<Long> ids) {
        if (arg instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Long id) {
                    ids.add(id);
                }
            }
        } else if (arg instanceof Set<?> set) {
            for (Object item : set) {
                if (item instanceof Long id) {
                    ids.add(id);
                }
            }
        } else if (arg instanceof Long[] array) {
            ids.addAll(Arrays.asList(array));
        } else if (arg instanceof Long id) {
            ids.add(id);
        }
    }
}