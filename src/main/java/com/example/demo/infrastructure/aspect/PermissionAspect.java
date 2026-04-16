package com.example.demo.infrastructure.aspect;

import com.example.demo.infrastructure.annotation.RequirePermission;
import com.example.demo.infrastructure.annotation.RequireBatchPermission;
import com.example.demo.domain.permission.aggregate.Resource;
import com.example.demo.domain.permission.entity.DataScope;
import com.example.demo.domain.permission.entity.UserDataScope;
import com.example.demo.domain.permission.service.DataScopeDomainService;
import com.example.demo.domain.permission.service.PermissionCacheService;
import com.example.demo.domain.permission.valueobject.ActionType;
import com.example.demo.domain.permission.valueobject.PermissionBitmap;
import com.example.demo.domain.permission.valueobject.ScopeType;
import com.example.demo.domain.permission.repository.ResourceRepository;
import com.example.demo.domain.permission.repository.UserDimensionRepository;
import com.example.demo.domain.permission.repository.DepartmentRepository;
import com.example.demo.infrastructure.exception.BusinessException;
import com.example.demo.infrastructure.security.UserContext;
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
import java.util.stream.Collectors;

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
    private final DataScopeDomainService dataScopeDomainService;
    private final UserDimensionRepository userDimensionRepository;
    private final DepartmentRepository departmentRepository;

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
     * Single action check with data scope validation
     */
    @Around("@annotation(requireBatchPermission)")
    public Object checkBatchPermission(ProceedingJoinPoint joinPoint, RequireBatchPermission requireBatchPermission) throws Throwable {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }

        // 1. 功能权限检查
        PermissionBitmap bitmap = permissionCache.getPermissionBitmap(userId);

        Resource resource = resourceRepository.findByCode(requireBatchPermission.resourceCode())
            .orElseThrow(() -> new BusinessException(500, "资源不存在: " + requireBatchPermission.resourceCode()));

        ActionType action = requireBatchPermission.action();
        if (!bitmap.hasAction(resource.getId(), action)) {
            log.warn("Batch permission denied: userId={}, resource={}, action={}",
                    userId, requireBatchPermission.resourceCode(), action);
            throw new BusinessException(403, requireBatchPermission.message());
        }

        // 2. 数据权限检查
        Set<Long> targetIds = extractTargetIds(joinPoint, requireBatchPermission.idParam());
        String dimensionCode = resource.getDataDimensionCode();

        if (dimensionCode != null && !targetIds.isEmpty()) {
            Set<Long> deniedIds = validateDataScope(userId, dimensionCode, targetIds);
            if (!deniedIds.isEmpty()) {
                log.warn("Data scope denied: userId={}, dimension={}, deniedIds={}",
                        userId, dimensionCode, deniedIds);
                throw new BusinessException(403, "部分数据无操作权限，被拒绝的ID: " + deniedIds);
            }
        }

        log.debug("Batch permission granted: userId={}, resource={}, action={}, targetIds={}",
                userId, requireBatchPermission.resourceCode(), action, targetIds);

        return joinPoint.proceed();
    }

    /**
     * Validate data scope for target IDs
     * Returns IDs that are denied access
     */
    private Set<Long> validateDataScope(Long userId, String dimensionCode, Set<Long> targetIds) {
        UserDataScope userScope = dataScopeDomainService.getUserDataScope(userId);
        DataScope scope = userScope.getScope(dimensionCode);

        if (scope == null) {
            // 无数据权限配置，拒绝所有
            return targetIds;
        }

        switch (scope.getScopeType()) {
            case ALL:
                // 全部数据权限，允许所有
                return Set.of();

            case CUSTOM:
                // 自定义范围，只允许配置的值
                return targetIds.stream()
                    .filter(id -> !scope.getScopeValues().contains(id))
                    .collect(Collectors.toSet());

            case SELF:
                // 仅自己数据
                Long userValue = userDimensionRepository.getValueByDimension(userId, dimensionCode);
                if (userValue == null) {
                    return targetIds;
                }
                return targetIds.stream()
                    .filter(id -> !id.equals(userValue))
                    .collect(Collectors.toSet());

            case SELF_DEPT:
                // 本部门数据
                List<Long> deptValues = userDimensionRepository.findValuesByDimension(userId, dimensionCode);
                return targetIds.stream()
                    .filter(id -> !deptValues.contains(id))
                    .collect(Collectors.toSet());

            case DEPT_TREE:
                // 部门树（含子部门）- 递归查询所有子部门
                Long userDeptId = userDimensionRepository.getValueByDimension(userId, dimensionCode);
                if (userDeptId == null) {
                    return targetIds;  // 无部门配置，拒绝所有
                }
                Set<Long> allDeptIds = new HashSet<>();
                allDeptIds.add(userDeptId);
                allDeptIds.addAll(departmentRepository.findAllSubDeptIds(userDeptId));
                return targetIds.stream()
                    .filter(id -> !allDeptIds.contains(id))
                    .collect(Collectors.toSet());

            default:
                return targetIds;
        }
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