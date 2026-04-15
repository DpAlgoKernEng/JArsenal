package com.example.demo.domain.permission.event;

import java.time.LocalDateTime;

/**
 * 用户角色分配事件
 */
public record UserRoleAssignedEvent(
    Long userId,
    Long roleId,
    Long operatorId,
    LocalDateTime occurredAt
) {
    public UserRoleAssignedEvent(Long userId, Long roleId, Long operatorId) {
        this(userId, roleId, operatorId, LocalDateTime.now());
    }
}