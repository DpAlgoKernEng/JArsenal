package com.example.demo.domain.permission.service;

import com.example.demo.domain.permission.entity.PermissionAuditLog;
import com.example.demo.domain.permission.repository.PermissionAuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 权限审计服务测试
 */
@ExtendWith(MockitoExtension.class)
class PermissionAuditServiceTest {

    @Mock
    private PermissionAuditLogRepository auditLogRepository;

    private PermissionAuditService auditService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        auditService = new PermissionAuditService(auditLogRepository, objectMapper);
    }

    @Test
    @DisplayName("记录角色创建事件")
    void logRoleCreate_shouldSaveAuditLog() {
        // given
        Long operatorId = 100L;
        Long roleId = 1L;
        String roleCode = "TEST_ROLE";
        String roleName = "测试角色";
        MDC.put("traceId", "trace-123");

        // when
        auditService.logRoleCreate(operatorId, roleId, roleCode, roleName);

        // then
        verify(auditLogRepository).save(any(PermissionAuditLog.class));
        MDC.remove("traceId");
    }

    @Test
    @DisplayName("记录角色删除事件")
    void logRoleDelete_shouldSaveAuditLog() {
        // given
        Long operatorId = 100L;
        Long roleId = 1L;
        String roleCode = "TEST_ROLE";
        MDC.put("traceId", "trace-123");

        // when
        auditService.logRoleDelete(operatorId, roleId, roleCode);

        // then
        verify(auditLogRepository).save(any(PermissionAuditLog.class));
        MDC.remove("traceId");
    }

    @Test
    @DisplayName("记录权限分配事件")
    void logPermissionAssign_shouldSaveAuditLog() {
        // given
        Long operatorId = 100L;
        Long roleId = 1L;
        Long resourceId = 10L;
        String changeType = "ADD";
        MDC.put("traceId", "trace-123");

        // when
        auditService.logPermissionAssign(operatorId, roleId, resourceId, changeType);

        // then
        verify(auditLogRepository).save(any(PermissionAuditLog.class));
        MDC.remove("traceId");
    }

    @Test
    @DisplayName("记录权限移除事件")
    void logPermissionRemove_shouldSaveAuditLog() {
        // given
        Long operatorId = 100L;
        Long roleId = 1L;
        Long resourceId = 10L;
        String changeType = "REMOVE";
        MDC.put("traceId", "trace-123");

        // when
        auditService.logPermissionAssign(operatorId, roleId, resourceId, changeType);

        // then
        verify(auditLogRepository).save(any(PermissionAuditLog.class));
        MDC.remove("traceId");
    }

    @Test
    @DisplayName("记录用户角色分配事件")
    void logUserRoleAssign_shouldSaveAuditLog() {
        // given
        Long operatorId = 100L;
        Long userId = 10L;
        Long roleId = 1L;
        MDC.put("traceId", "trace-123");

        // when
        auditService.logUserRoleAssign(operatorId, userId, roleId);

        // then
        verify(auditLogRepository).save(any(PermissionAuditLog.class));
        MDC.remove("traceId");
    }

    @Test
    @DisplayName("记录资源创建事件")
    void logResourceCreate_shouldSaveAuditLog() {
        // given
        Long operatorId = 100L;
        Long resourceId = 1L;
        String resourceCode = "USER_RESOURCE";
        String resourceName = "用户资源";
        MDC.put("traceId", "trace-123");

        // when
        auditService.logResourceCreate(operatorId, resourceId, resourceCode, resourceName);

        // then
        verify(auditLogRepository).save(any(PermissionAuditLog.class));
        MDC.remove("traceId");
    }

    @Test
    @DisplayName("审计日志保存失败不应抛出异常")
    void logChange_saveFails_shouldNotThrowException() {
        // given
        doThrow(new RuntimeException("Database error")).when(auditLogRepository).save(any());

        // when & then - should not throw
        auditService.logRoleCreate(100L, 1L, "TEST_ROLE", "测试角色");
        verify(auditLogRepository).save(any(PermissionAuditLog.class));
    }

    @Test
    @DisplayName("记录变更事件 - 包含前后值")
    void logChange_withBeforeAndAfterValues_shouldSaveAuditLog() {
        // given
        Long operatorId = 100L;
        String operatorName = "admin";
        String changeType = "ROLE_UPDATE";
        String targetType = "ROLE";
        Long targetId = 1L;
        Object beforeValue = Map.of("name", "旧名称");
        Object afterValue = Map.of("name", "新名称");
        String reason = "业务调整";
        MDC.put("traceId", "trace-123");

        // when
        auditService.logChange(operatorId, operatorName, changeType, targetType, targetId,
            beforeValue, afterValue, reason);

        // then
        verify(auditLogRepository).save(any(PermissionAuditLog.class));
        MDC.remove("traceId");
    }
}