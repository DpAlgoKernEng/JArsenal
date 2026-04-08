package com.example.demo.infrastructure.persistence.converter;

import com.example.demo.domain.audit.aggregate.AuditLog;
import com.example.demo.infrastructure.persistence.po.AuditLogPO;
import org.springframework.stereotype.Component;

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
}