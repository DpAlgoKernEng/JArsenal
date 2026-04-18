package com.jguard.domain.permission.repository;

import com.jguard.domain.permission.entity.PermissionAuditLog;
import java.util.List;

/**
 * 权限审计日志仓储接口
 */
public interface PermissionAuditLogRepository {

    /**
     * 保存审计日志
     */
    void save(PermissionAuditLog log);

    /**
     * 根据目标查询审计日志
     */
    List<PermissionAuditLog> findByTarget(String targetType, Long targetId, int limit);

    /**
     * 根据操作人查询审计日志
     */
    List<PermissionAuditLog> findByOperator(Long operatorId, int limit);

    /**
     * 根据变更类型和时间范围查询
     */
    List<PermissionAuditLog> findByTypeAndTimeRange(String changeType, String startTime, String endTime);
}