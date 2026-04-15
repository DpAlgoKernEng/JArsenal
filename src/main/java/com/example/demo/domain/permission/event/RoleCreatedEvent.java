package com.example.demo.domain.permission.event;

import java.time.LocalDateTime;

/**
 * 角色创建事件
 */
public record RoleCreatedEvent(
    Long roleId,
    String roleCode,
    String roleName,
    Long operatorId,
    LocalDateTime occurredAt
) {
    public RoleCreatedEvent(Long roleId, String roleCode, String roleName, Long operatorId) {
        this(roleId, roleCode, roleName, operatorId, LocalDateTime.now());
    }
}