package com.jguard.domain.permission.service;

import com.jguard.domain.permission.entity.PermissionAuditLog;
import com.jguard.domain.permission.repository.PermissionAuditLogRepository;
import com.jguard.infrastructure.security.UserContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 权限审计服务
 * 异步记录权限变更审计日志
 */
@Service
public class PermissionAuditService {

    private static final Logger logger = LoggerFactory.getLogger(PermissionAuditService.class);

    private final PermissionAuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public PermissionAuditService(PermissionAuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * 异步记录权限变更
     */
    @Async
    public void logChange(Long operatorId, String operatorName,
                          String changeType, String targetType, Long targetId,
                          Object beforeValue, Object afterValue, String reason) {
        try {
            String traceId = MDC.get("traceId");

            String beforeJson = beforeValue != null ? objectMapper.writeValueAsString(beforeValue) : null;
            String afterJson = afterValue != null ? objectMapper.writeValueAsString(afterValue) : null;

            PermissionAuditLog log = PermissionAuditLog.of(
                operatorId, operatorName, changeType, targetType, targetId,
                beforeJson, afterJson, reason, traceId
            );

            auditLogRepository.save(log);
            logger.debug("审计日志已记录: {} - {} {}", changeType, targetType, targetId);
        } catch (Exception e) {
            // 审计日志失败不应影响业务操作
            // 记录错误但不抛出
            logger.error("审计日志记录失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 记录角色创建事件
     * 参数与RoleCreatedEvent匹配：roleId, roleCode, roleName, operatorId
     */
    public void logRoleCreate(Long operatorId, Long roleId, String roleCode, String roleName) {
        logChange(operatorId, getCurrentUsername(),
            "ROLE_CREATE", "ROLE", roleId,
            null, Map.of("code", roleCode, "name", roleName), null);
    }

    /**
     * 记录角色更新事件
     */
    public void logRoleUpdate(Long operatorId, Long roleId, Object beforeValue, Object afterValue) {
        logChange(operatorId, getCurrentUsername(),
            "ROLE_UPDATE", "ROLE", roleId,
            beforeValue, afterValue, null);
    }

    /**
     * 记录角色删除事件
     */
    public void logRoleDelete(Long operatorId, Long roleId, String roleCode) {
        logChange(operatorId, getCurrentUsername(),
            "ROLE_DELETE", "ROLE", roleId,
            Map.of("code", roleCode), null, null);
    }

    /**
     * 记录权限分配事件
     * 参数与RolePermissionChangedEvent匹配：roleId, resourceId, changeType, operatorId
     */
    public void logPermissionAssign(Long operatorId, Long roleId, Long resourceId, String changeType) {
        logChange(operatorId, getCurrentUsername(),
            changeType.equals("REMOVE") ? "PERM_REMOVE" : "PERM_ASSIGN",
            "ROLE", roleId,
            null, Map.of("resourceId", resourceId, "changeType", changeType), null);
    }

    /**
     * 记录用户角色分配事件
     * 参数与UserRoleAssignedEvent匹配：userId, roleId, operatorId
     */
    public void logUserRoleAssign(Long operatorId, Long userId, Long roleId) {
        logChange(operatorId, getCurrentUsername(),
            "USER_ROLE_ASSIGN", "USER", userId,
            null, Map.of("roleId", roleId), null);
    }

    /**
     * 记录用户角色移除事件
     */
    public void logUserRoleRemove(Long operatorId, Long userId, Long roleId) {
        logChange(operatorId, getCurrentUsername(),
            "USER_ROLE_REMOVE", "USER", userId,
            Map.of("roleId", roleId), null, null);
    }

    /**
     * 记录资源创建事件
     */
    public void logResourceCreate(Long operatorId, Long resourceId, String resourceCode, String resourceName) {
        logChange(operatorId, getCurrentUsername(),
            "RESOURCE_CREATE", "RESOURCE", resourceId,
            null, Map.of("code", resourceCode, "name", resourceName), null);
    }

    /**
     * 记录资源更新事件
     */
    public void logResourceUpdate(Long operatorId, Long resourceId, Object beforeValue, Object afterValue) {
        logChange(operatorId, getCurrentUsername(),
            "RESOURCE_UPDATE", "RESOURCE", resourceId,
            beforeValue, afterValue, null);
    }

    /**
     * 记录资源删除事件
     */
    public void logResourceDelete(Long operatorId, Long resourceId, String resourceCode) {
        logChange(operatorId, getCurrentUsername(),
            "RESOURCE_DELETE", "RESOURCE", resourceId,
            Map.of("code", resourceCode), null, null);
    }

    /**
     * 记录字段权限变更事件
     */
    public void logFieldPermissionChange(Long operatorId, Long roleId, Long fieldId,
                                          Object beforeValue, Object afterValue) {
        logChange(operatorId, getCurrentUsername(),
            "FIELD_PERM_CHANGE", "ROLE", roleId,
            beforeValue, afterValue, null);
    }

    /**
     * 获取当前用户名
     */
    private String getCurrentUsername() {
        var user = UserContext.getCurrentUser();
        return user != null ? user.getUsername() : "SYSTEM";
    }
}