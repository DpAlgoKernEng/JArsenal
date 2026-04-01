package com.example.demo.service.impl;

import com.example.demo.entity.AuditLogEntity;
import com.example.demo.mapper.AuditLogMapper;
import com.example.demo.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 审计日志服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogMapper auditLogMapper;

    @Override
    @Async
    public void saveAsync(AuditLogEntity auditLog) {
        save(auditLog);
    }

    @Override
    public void save(AuditLogEntity auditLog) {
        try {
            auditLogMapper.insert(auditLog);
            log.debug("审计日志保存成功: operation={}, module={}", auditLog.getOperation(), auditLog.getModule());
        } catch (Exception e) {
            log.error("审计日志保存失败: {}", e.getMessage(), e);
        }
    }
}