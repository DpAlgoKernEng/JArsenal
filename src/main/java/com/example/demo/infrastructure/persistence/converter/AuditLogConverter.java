package com.example.demo.infrastructure.persistence.converter;

import com.example.demo.domain.audit.aggregate.AuditLog;
import com.example.demo.domain.audit.valueobject.ModuleType;
import com.example.demo.domain.audit.valueobject.OperationType;
import com.example.demo.domain.audit.valueobject.TraceInfo;
import com.example.demo.domain.user.valueobject.UserId;
import com.example.demo.domain.user.valueobject.Username;
import com.example.demo.infrastructure.persistence.po.AuditLogPO;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * AuditLog 领域对象与持久化对象转换器
 */
@Component
public class AuditLogConverter {

    /**
     * 领域对象 转 PO
     */
    public AuditLogPO toPO(AuditLog log) {
        if (log == null) {
            return null;
        }
        AuditLogPO po = new AuditLogPO();
        if (log.getUserId() != null) {
            po.setUserId(log.getUserId().value());
        }
        if (log.getUsername() != null) {
            po.setUsername(log.getUsername().value());
        }
        po.setOperation(log.getOperation().code());
        po.setModule(log.getModule().code());
        po.setDescription(log.getDescription());
        po.setTargetId(log.getTargetId());
        if (log.getTraceInfo() != null) {
            po.setIp(log.getTraceInfo().ip());
            po.setTraceId(log.getTraceInfo().traceId());
        }
        po.setStatus(log.isSuccess() ? 1 : 0);
        po.setErrorMsg(log.getErrorMessage());
        po.setDuration(log.getDuration());
        po.setCreateTime(log.getCreatedAt());
        return po;
    }

    /**
     * PO 转领域对象
     */
    public AuditLog toDomain(AuditLogPO po) {
        if (po == null) {
            return null;
        }

        UserId userId = po.getUserId() != null ? new UserId(po.getUserId()) : null;
        Username username = po.getUsername() != null ? new Username(po.getUsername()) : null;
        OperationType operation = Arrays.stream(OperationType.values())
            .filter(o -> o.code().equals(po.getOperation()))
            .findFirst()
            .orElse(null);
        ModuleType module = Arrays.stream(ModuleType.values())
            .filter(m -> m.code().equals(po.getModule()))
            .findFirst()
            .orElse(null);
        TraceInfo traceInfo = (po.getIp() != null || po.getTraceId() != null)
            ? new TraceInfo(po.getIp(), po.getTraceId()) : null;

        if (po.getStatus() != null && po.getStatus() == 1) {
            return AuditLog.success(
                userId, username, operation, module,
                po.getDescription(), po.getTargetId(),
                traceInfo, po.getDuration() != null ? po.getDuration() : 0L
            );
        } else {
            return AuditLog.failure(
                userId, username, operation, module,
                po.getDescription(), po.getTargetId(),
                traceInfo, po.getErrorMsg(),
                po.getDuration() != null ? po.getDuration() : 0L
            );
        }
    }
}