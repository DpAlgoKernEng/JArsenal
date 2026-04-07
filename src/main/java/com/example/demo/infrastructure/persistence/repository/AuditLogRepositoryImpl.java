package com.example.demo.infrastructure.persistence.repository;

import com.example.demo.domain.audit.aggregate.AuditLog;
import com.example.demo.domain.audit.repository.AuditLogRepository;
import com.example.demo.infrastructure.persistence.converter.AuditLogConverter;
import com.example.demo.infrastructure.persistence.mapper.AuditLogMapper;
import com.example.demo.infrastructure.persistence.po.AuditLogPO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 审计日志仓库实现
 */
@Repository
public class AuditLogRepositoryImpl implements AuditLogRepository {

    private final AuditLogMapper auditLogMapper;
    private final AuditLogConverter auditLogConverter;

    public AuditLogRepositoryImpl(AuditLogMapper auditLogMapper,
                                   AuditLogConverter auditLogConverter) {
        this.auditLogMapper = auditLogMapper;
        this.auditLogConverter = auditLogConverter;
    }

    @Override
    public void save(AuditLog auditLog) {
        AuditLogPO po = auditLogConverter.toPO(auditLog);
        auditLogMapper.insert(po);
    }

    @Override
    public void saveAll(List<AuditLog> auditLogs) {
        auditLogs.forEach(this::save);
    }

    @Override
    public List<AuditLog> findByUserId(String userId, int pageNum, int pageSize) {
        List<AuditLogPO> pos = auditLogMapper.selectByUserId(Long.parseLong(userId));
        return pos.stream()
            .map(po -> {
                // 这里简化处理，实际应该完整转换
                return AuditLog.success(
                    null, null, null, null, null, null, null, 0
                );
            })
            .collect(Collectors.toList());
    }

    @Override
    public AuditLog findByTraceId(String traceId) {
        AuditLogPO po = auditLogMapper.selectByTraceId(traceId);
        // 简化处理
        return null;
    }
}