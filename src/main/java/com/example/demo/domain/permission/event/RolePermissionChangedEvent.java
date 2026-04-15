package com.example.demo.domain.permission.event;

import java.time.LocalDateTime;

/**
 * 角色权限变更事件
 */
public record RolePermissionChangedEvent(
    Long roleId,
    Long resourceId,
    String changeType,  // ADD/REMOVE
    Long operatorId,
    LocalDateTime occurredAt
) {
    public RolePermissionChangedEvent(Long roleId, Long resourceId, String changeType, Long operatorId) {
        this(roleId, resourceId, changeType, operatorId, LocalDateTime.now());
    }
}