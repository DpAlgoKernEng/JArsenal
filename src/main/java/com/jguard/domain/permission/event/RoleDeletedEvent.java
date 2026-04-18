package com.jguard.domain.permission.event;

import java.time.LocalDateTime;

/**
 * 角色删除事件
 */
public record RoleDeletedEvent(
    Long roleId,
    String roleCode,
    Long operatorId,
    LocalDateTime occurredAt
) {
    public RoleDeletedEvent(Long roleId, String roleCode, Long operatorId) {
        this(roleId, roleCode, operatorId, LocalDateTime.now());
    }
}