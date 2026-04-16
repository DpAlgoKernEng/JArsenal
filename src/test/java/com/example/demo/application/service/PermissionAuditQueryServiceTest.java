package com.example.demo.application.service;

import com.example.demo.application.dto.AuditLogResponse;
import com.example.demo.domain.permission.entity.PermissionAuditLog;
import com.example.demo.domain.permission.repository.PermissionAuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 权限审计查询服务测试
 */
@ExtendWith(MockitoExtension.class)
class PermissionAuditQueryServiceTest {

    @Mock
    private PermissionAuditLogRepository auditLogRepository;

    private PermissionAuditQueryService queryService;

    @BeforeEach
    void setUp() {
        queryService = new PermissionAuditQueryService(auditLogRepository);
    }

    @Test
    @DisplayName("根据目标查询审计日志")
    void getLogsByTarget_shouldReturnLogs() {
        // given
        PermissionAuditLog log1 = PermissionAuditLog.rebuild(1L, 100L, "admin",
            "ROLE_CREATE", "ROLE", 1L, null, "{\"code\":\"ADMIN\",\"name\":\"管理员\"}",
            null, "trace-123", LocalDateTime.now());
        PermissionAuditLog log2 = PermissionAuditLog.rebuild(2L, 100L, "admin",
            "ROLE_UPDATE", "ROLE", 1L, "{\"name\":\"旧名称\"}", "{\"name\":\"新名称\"}",
            null, "trace-124", LocalDateTime.now());

        when(auditLogRepository.findByTarget("ROLE", 1L, 20))
            .thenReturn(Arrays.asList(log1, log2));

        // when
        List<AuditLogResponse> result = queryService.getLogsByTarget("ROLE", 1L, 20);

        // then
        assertEquals(2, result.size());
        assertEquals("ROLE_CREATE", result.get(0).changeType());
        assertEquals("ROLE_UPDATE", result.get(1).changeType());
        verify(auditLogRepository).findByTarget("ROLE", 1L, 20);
    }

    @Test
    @DisplayName("根据目标查询审计日志 - 无结果")
    void getLogsByTarget_noLogs_shouldReturnEmptyList() {
        // given
        when(auditLogRepository.findByTarget("ROLE", 999L, 20))
            .thenReturn(Collections.emptyList());

        // when
        List<AuditLogResponse> result = queryService.getLogsByTarget("ROLE", 999L, 20);

        // then
        assertTrue(result.isEmpty());
        verify(auditLogRepository).findByTarget("ROLE", 999L, 20);
    }

    @Test
    @DisplayName("根据操作人查询审计日志")
    void getLogsByOperator_shouldReturnLogs() {
        // given
        PermissionAuditLog log1 = PermissionAuditLog.rebuild(1L, 100L, "admin",
            "ROLE_CREATE", "ROLE", 1L, null, "{\"code\":\"ADMIN\"}",
            null, "trace-123", LocalDateTime.now());

        when(auditLogRepository.findByOperator(100L, 20))
            .thenReturn(Arrays.asList(log1));

        // when
        List<AuditLogResponse> result = queryService.getLogsByOperator(100L, 20);

        // then
        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).operatorId());
        verify(auditLogRepository).findByOperator(100L, 20);
    }

    @Test
    @DisplayName("搜索审计日志")
    void searchLogs_shouldReturnLogs() {
        // given
        PermissionAuditLog log1 = PermissionAuditLog.rebuild(1L, 100L, "admin",
            "ROLE_CREATE", "ROLE", 1L, null, "{\"code\":\"ADMIN\"}",
            null, "trace-123", LocalDateTime.now());

        when(auditLogRepository.findByTypeAndTimeRange("ROLE_CREATE", "2024-01-01", "2024-12-31"))
            .thenReturn(Arrays.asList(log1));

        // when
        List<AuditLogResponse> result = queryService.searchLogs("ROLE_CREATE", "2024-01-01", "2024-12-31");

        // then
        assertEquals(1, result.size());
        assertEquals("ROLE_CREATE", result.get(0).changeType());
        verify(auditLogRepository).findByTypeAndTimeRange("ROLE_CREATE", "2024-01-01", "2024-12-31");
    }
}