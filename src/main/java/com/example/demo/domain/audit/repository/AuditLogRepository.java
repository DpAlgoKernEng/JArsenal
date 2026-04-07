package com.example.demo.domain.audit.repository;

import com.example.demo.domain.audit.aggregate.AuditLog;
import java.util.List;

/**
 * 审计日志仓库接口
 */
public interface AuditLogRepository {

    /**
     * 保存审计日志
     */
    void save(AuditLog auditLog);

    /**
     * 批量保存审计日志
     */
    void saveAll(List<AuditLog> auditLogs);

    /**
     * 根据用户ID查询审计日志
     */
    List<AuditLog> findByUserId(String userId, int pageNum, int pageSize);

    /**
     * 根据追踪ID查询审计日志
     */
    AuditLog findByTraceId(String traceId);
}