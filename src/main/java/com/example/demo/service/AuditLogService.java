package com.example.demo.service;

import com.example.demo.entity.AuditLogEntity;

/**
 * 审计日志服务
 */
public interface AuditLogService {

    /**
     * 异步保存审计日志
     */
    void saveAsync(AuditLogEntity auditLog);

    /**
     * 保存审计日志
     */
    void save(AuditLogEntity auditLog);
}