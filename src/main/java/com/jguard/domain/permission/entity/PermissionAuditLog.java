package com.jguard.domain.permission.entity;

import java.time.LocalDateTime;

/**
 * 权限审计日志实体
 * 记录权限相关变更操作的审计信息
 */
public class PermissionAuditLog {

    private Long id;
    private Long operatorId;
    private String operatorName;
    private String changeType;  // ROLE_CREATE/ROLE_UPDATE/PERM_ASSIGN/USER_ROLE_ASSIGN 等
    private String targetType;  // ROLE/RESOURCE/USER
    private Long targetId;
    private String beforeValue; // JSON
    private String afterValue;  // JSON
    private String reason;
    private String traceId;
    private LocalDateTime createTime;

    /**
     * 工厂方法：创建权限审计日志
     */
    public static PermissionAuditLog of(Long operatorId, String operatorName,
                                         String changeType, String targetType, Long targetId,
                                         String beforeValue, String afterValue, String reason, String traceId) {
        PermissionAuditLog log = new PermissionAuditLog();
        log.operatorId = operatorId;
        log.operatorName = operatorName;
        log.changeType = changeType;
        log.targetType = targetType;
        log.targetId = targetId;
        log.beforeValue = beforeValue;
        log.afterValue = afterValue;
        log.reason = reason;
        log.traceId = traceId;
        log.createTime = LocalDateTime.now();
        return log;
    }

    /**
     * 工厂方法：重建已存在的审计日志（从数据库加载时使用）
     */
    public static PermissionAuditLog rebuild(Long id, Long operatorId, String operatorName,
                                             String changeType, String targetType, Long targetId,
                                             String beforeValue, String afterValue, String reason,
                                             String traceId, LocalDateTime createTime) {
        PermissionAuditLog log = new PermissionAuditLog();
        log.id = id;
        log.operatorId = operatorId;
        log.operatorName = operatorName;
        log.changeType = changeType;
        log.targetType = targetType;
        log.targetId = targetId;
        log.beforeValue = beforeValue;
        log.afterValue = afterValue;
        log.reason = reason;
        log.traceId = traceId;
        log.createTime = createTime;
        return log;
    }

    // Getters

    public Long getId() {
        return id;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public String getChangeType() {
        return changeType;
    }

    public String getTargetType() {
        return targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public String getBeforeValue() {
        return beforeValue;
    }

    public String getAfterValue() {
        return afterValue;
    }

    public String getReason() {
        return reason;
    }

    public String getTraceId() {
        return traceId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }
}