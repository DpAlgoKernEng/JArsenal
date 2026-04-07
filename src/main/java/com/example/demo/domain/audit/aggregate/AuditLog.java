package com.example.demo.domain.audit.aggregate;

import com.example.demo.domain.audit.valueobject.ModuleType;
import com.example.demo.domain.audit.valueobject.OperationType;
import com.example.demo.domain.audit.valueobject.TraceInfo;
import com.example.demo.domain.shared.common.BaseEntity;
import com.example.demo.domain.user.valueobject.UserId;
import com.example.demo.domain.user.valueobject.Username;
import java.time.LocalDateTime;

/**
 * 审计日志聚合根
 */
public class AuditLog extends BaseEntity<String> {

    private UserId userId;
    private Username username;
    private OperationType operation;
    private ModuleType module;
    private String description;
    private Long targetId;
    private TraceInfo traceInfo;
    private boolean success;
    private String errorMessage;
    private long duration;
    private LocalDateTime createdAt;

    /**
     * 工厂方法：创建成功的审计日志
     */
    public static AuditLog success(UserId userId, Username username,
                                   OperationType operation, ModuleType module,
                                   String description, Long targetId,
                                   TraceInfo traceInfo, long duration) {
        AuditLog log = new AuditLog();
        log.userId = userId;
        log.username = username;
        log.operation = operation;
        log.module = module;
        log.description = description;
        log.targetId = targetId;
        log.traceInfo = traceInfo;
        log.success = true;
        log.duration = duration;
        log.createdAt = LocalDateTime.now();
        return log;
    }

    /**
     * 工厂方法：创建失败的审计日志
     */
    public static AuditLog failure(UserId userId, Username username,
                                   OperationType operation, ModuleType module,
                                   String description, Long targetId,
                                   TraceInfo traceInfo, String errorMessage, long duration) {
        AuditLog log = new AuditLog();
        log.userId = userId;
        log.username = username;
        log.operation = operation;
        log.module = module;
        log.description = description;
        log.targetId = targetId;
        log.traceInfo = traceInfo;
        log.success = false;
        log.errorMessage = errorMessage;
        log.duration = duration;
        log.createdAt = LocalDateTime.now();
        return log;
    }

    /**
     * 工厂方法：从领域事件创建审计日志
     */
    public static AuditLog fromEvent(UserId userId, Username username,
                                     OperationType operation, ModuleType module,
                                     TraceInfo traceInfo) {
        return success(userId, username, operation, module,
            operation.description(), null, traceInfo, 0);
    }

    // Getters

    public UserId getUserId() {
        return userId;
    }

    public Username getUsername() {
        return username;
    }

    public OperationType getOperation() {
        return operation;
    }

    public ModuleType getModule() {
        return module;
    }

    public String getDescription() {
        return description;
    }

    public Long getTargetId() {
        return targetId;
    }

    public TraceInfo getTraceInfo() {
        return traceInfo;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public long getDuration() {
        return duration;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}