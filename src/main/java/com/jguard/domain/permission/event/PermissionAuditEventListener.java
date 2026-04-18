package com.jguard.domain.permission.event;

import com.jguard.domain.permission.service.PermissionAuditService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 权限审计事件监听器
 * 监听领域事件并记录审计日志
 */
@Component
public class PermissionAuditEventListener {

    private static final Logger logger = LoggerFactory.getLogger(PermissionAuditEventListener.class);

    private final PermissionAuditService auditService;

    public PermissionAuditEventListener(PermissionAuditService auditService) {
        this.auditService = auditService;
    }

    /**
     * 监听角色创建事件
     */
    @EventListener
    public void onRoleCreated(RoleCreatedEvent event) {
        logger.debug("收到角色创建事件: roleId={}, code={}", event.roleId(), event.roleCode());
        auditService.logRoleCreate(event.operatorId(), event.roleId(), event.roleCode(), event.roleName());
    }

    /**
     * 监听角色权限变更事件
     */
    @EventListener
    public void onRolePermissionChanged(RolePermissionChangedEvent event) {
        logger.debug("收到权限变更事件: roleId={}, resourceId={}, type={}",
            event.roleId(), event.resourceId(), event.changeType());
        auditService.logPermissionAssign(
            event.operatorId(),
            event.roleId(),
            event.resourceId(),
            event.changeType()
        );
    }

    /**
     * 监听用户角色分配事件
     */
    @EventListener
    public void onUserRoleAssigned(UserRoleAssignedEvent event) {
        logger.debug("收到用户角色分配事件: userId={}, roleId={}", event.userId(), event.roleId());
        auditService.logUserRoleAssign(event.operatorId(), event.userId(), event.roleId());
    }

    /**
     * 监听角色删除事件
     */
    @EventListener
    public void onRoleDeleted(RoleDeletedEvent event) {
        logger.debug("收到角色删除事件: roleId={}, code={}", event.roleId(), event.roleCode());
        auditService.logRoleDelete(event.operatorId(), event.roleId(), event.roleCode());
    }
}