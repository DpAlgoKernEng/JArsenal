package com.jguard.infrastructure.persistence.repository;

import com.jguard.domain.permission.entity.PermissionAuditLog;
import com.jguard.domain.permission.repository.PermissionAuditLogRepository;
import com.jguard.infrastructure.persistence.mapper.PermissionAuditLogMapper;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 权限审计日志仓储实现
 */
@Repository
public class PermissionAuditLogRepositoryImpl implements PermissionAuditLogRepository {

    private final PermissionAuditLogMapper mapper;

    public PermissionAuditLogRepositoryImpl(PermissionAuditLogMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void save(PermissionAuditLog log) {
        mapper.insert(log);
    }

    @Override
    public List<PermissionAuditLog> findByTarget(String targetType, Long targetId, int limit) {
        return mapper.findByTarget(targetType, targetId, limit);
    }

    @Override
    public List<PermissionAuditLog> findByOperator(Long operatorId, int limit) {
        return mapper.findByOperator(operatorId, limit);
    }

    @Override
    public List<PermissionAuditLog> findByTypeAndTimeRange(String changeType, String startTime, String endTime) {
        return mapper.findByTypeAndTimeRange(changeType, startTime, endTime);
    }
}