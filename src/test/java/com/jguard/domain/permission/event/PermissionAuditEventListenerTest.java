package com.jguard.domain.permission.event;

import com.jguard.domain.permission.service.PermissionAuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

/**
 * 权限审计事件监听器测试
 */
@ExtendWith(MockitoExtension.class)
class PermissionAuditEventListenerTest {

    @Mock
    private PermissionAuditService auditService;

    private PermissionAuditEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new PermissionAuditEventListener(auditService);
    }

    @Test
    @DisplayName("监听角色创建事件")
    void onRoleCreated_shouldLogAudit() {
        // given
        RoleCreatedEvent event = new RoleCreatedEvent(1L, "TEST_ROLE", "测试角色", 100L);

        // when
        listener.onRoleCreated(event);

        // then
        verify(auditService).logRoleCreate(100L, 1L, "TEST_ROLE", "测试角色");
    }

    @Test
    @DisplayName("监听角色权限变更事件 - ADD")
    void onRolePermissionChanged_add_shouldLogAudit() {
        // given
        RolePermissionChangedEvent event = new RolePermissionChangedEvent(1L, 10L, "ADD", 100L);

        // when
        listener.onRolePermissionChanged(event);

        // then
        verify(auditService).logPermissionAssign(100L, 1L, 10L, "ADD");
    }

    @Test
    @DisplayName("监听角色权限变更事件 - REMOVE")
    void onRolePermissionChanged_remove_shouldLogAudit() {
        // given
        RolePermissionChangedEvent event = new RolePermissionChangedEvent(1L, 10L, "REMOVE", 100L);

        // when
        listener.onRolePermissionChanged(event);

        // then
        verify(auditService).logPermissionAssign(100L, 1L, 10L, "REMOVE");
    }

    @Test
    @DisplayName("监听用户角色分配事件")
    void onUserRoleAssigned_shouldLogAudit() {
        // given
        UserRoleAssignedEvent event = new UserRoleAssignedEvent(10L, 1L, 100L);

        // when
        listener.onUserRoleAssigned(event);

        // then
        verify(auditService).logUserRoleAssign(100L, 10L, 1L);
    }

    @Test
    @DisplayName("监听角色删除事件")
    void onRoleDeleted_shouldLogAudit() {
        // given
        RoleDeletedEvent event = new RoleDeletedEvent(1L, "TEST_ROLE", 100L);

        // when
        listener.onRoleDeleted(event);

        // then
        verify(auditService).logRoleDelete(100L, 1L, "TEST_ROLE");
    }
}