package com.jguard.application.dto;

import java.time.LocalDateTime;

/**
 * 审计日志响应 DTO
 */
public record AuditLogResponse(
    Long id,
    Long operatorId,
    String operatorName,
    String changeType,
    String targetType,
    Long targetId,
    String beforeValue,
    String afterValue,
    String reason,
    String traceId,
    LocalDateTime createTime
) {}