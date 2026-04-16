package com.example.demo.application.service;

import com.example.demo.application.dto.AuditLogResponse;
import com.example.demo.domain.permission.entity.PermissionAuditLog;
import com.example.demo.domain.permission.repository.PermissionAuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * 权限审计查询服务
 * 纯查询服务，标记readOnly优化数据库读操作
 */
@Service
@Transactional(readOnly = true)
public class PermissionAuditQueryService {

    private final PermissionAuditLogRepository auditLogRepository;

    public PermissionAuditQueryService(PermissionAuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * 根据目标查询审计日志
     */
    public List<AuditLogResponse> getLogsByTarget(String targetType, Long targetId, int limit) {
        List<PermissionAuditLog> logs = auditLogRepository.findByTarget(targetType, targetId, limit);
        return logs.stream().map(this::toResponse).toList();
    }

    /**
     * 根据操作人查询审计日志
     */
    public List<AuditLogResponse> getLogsByOperator(Long operatorId, int limit) {
        List<PermissionAuditLog> logs = auditLogRepository.findByOperator(operatorId, limit);
        return logs.stream().map(this::toResponse).toList();
    }

    /**
     * 搜索审计日志
     */
    public List<AuditLogResponse> searchLogs(String changeType, String startTime, String endTime) {
        List<PermissionAuditLog> logs = auditLogRepository.findByTypeAndTimeRange(changeType, startTime, endTime);
        return logs.stream().map(this::toResponse).toList();
    }

    private AuditLogResponse toResponse(PermissionAuditLog log) {
        return new AuditLogResponse(
            log.getId(),
            log.getOperatorId(),
            log.getOperatorName(),
            log.getChangeType(),
            log.getTargetType(),
            log.getTargetId(),
            log.getBeforeValue(),
            log.getAfterValue(),
            log.getReason(),
            log.getTraceId(),
            log.getCreateTime()
        );
    }
}