package com.jguard.infrastructure.persistence.repository;

import com.jguard.domain.audit.aggregate.AuditLog;
import com.jguard.infrastructure.persistence.converter.AuditLogConverter;
import com.jguard.infrastructure.persistence.mapper.AuditLogMapper;
import com.jguard.infrastructure.persistence.po.AuditLogPO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditLogRepositoryImpl 测试")
class AuditLogRepositoryImplTest {

    @Mock
    private AuditLogMapper auditLogMapper;

    private AuditLogRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new AuditLogRepositoryImpl(auditLogMapper, new AuditLogConverter());
    }

    @Test
    @DisplayName("findByUserId - 正确转换返回的审计日志")
    void findByUserId_shouldReturnConvertedAuditLogs() {
        // Given
        AuditLogPO po = new AuditLogPO();
        po.setId(1L);
        po.setUserId(100L);
        po.setUsername("testuser");
        po.setOperation("LOGIN");
        po.setModule("AUTH");
        po.setDescription("用户登录");
        po.setIp("127.0.0.1");
        po.setTraceId("trace-1");
        po.setStatus(1);
        po.setDuration(100L);
        po.setCreateTime(LocalDateTime.now());

        when(auditLogMapper.selectByUserId(100L)).thenReturn(List.of(po));

        // When
        List<AuditLog> logs = repository.findByUserId("100", 1, 10);

        // Then
        assertEquals(1, logs.size());
        AuditLog log = logs.get(0);
        assertNotNull(log.getUserId());
        assertEquals(100L, log.getUserId().value());
        assertEquals("testuser", log.getUsername().value());
        assertNotNull(log.getTraceInfo());
        assertEquals("127.0.0.1", log.getTraceInfo().ip());
        assertTrue(log.isSuccess());
    }

    @Test
    @DisplayName("findByTraceId - 正确转换返回的审计日志")
    void findByTraceId_shouldReturnConvertedAuditLog() {
        // Given
        AuditLogPO po = new AuditLogPO();
        po.setId(1L);
        po.setUserId(100L);
        po.setUsername("testuser");
        po.setOperation("UPDATE");
        po.setModule("USER");
        po.setDescription("更新用户信息");
        po.setTraceId("trace-abc");
        po.setStatus(1);
        po.setCreateTime(LocalDateTime.now());

        when(auditLogMapper.selectByTraceId("trace-abc")).thenReturn(po);

        // When
        AuditLog log = repository.findByTraceId("trace-abc");

        // Then
        assertNotNull(log);
        assertEquals("testuser", log.getUsername().value());
        assertEquals("trace-abc", log.getTraceInfo().traceId());
    }
}
