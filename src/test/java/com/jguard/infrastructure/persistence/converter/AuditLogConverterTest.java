package com.jguard.infrastructure.persistence.converter;

import com.jguard.domain.audit.aggregate.AuditLog;
import com.jguard.domain.audit.valueobject.ModuleType;
import com.jguard.domain.audit.valueobject.OperationType;
import com.jguard.domain.audit.valueobject.TraceInfo;
import com.jguard.domain.user.valueobject.UserId;
import com.jguard.domain.user.valueobject.Username;
import com.jguard.infrastructure.persistence.po.AuditLogPO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AuditLogConverter 转换测试
 */
@DisplayName("AuditLogConverter 转换测试")
class AuditLogConverterTest {

    private final AuditLogConverter converter = new AuditLogConverter();

    @Test
    @DisplayName("toDomain - 成功转换 PO 到领域对象")
    void toDomain_shouldConvertPOToDomainCorrectly() {
        // Given
        AuditLogPO po = new AuditLogPO();
        po.setId(1L);
        po.setUserId(100L);
        po.setUsername("testuser");
        po.setOperation("LOGIN");
        po.setModule("AUTH");
        po.setDescription("用户登录成功");
        po.setTargetId(null);
        po.setIp("192.168.1.1");
        po.setTraceId("trace-123");
        po.setStatus(1);
        po.setErrorMsg(null);
        po.setDuration(150L);
        po.setCreateTime(LocalDateTime.of(2024, 1, 1, 10, 0));

        // When
        AuditLog domain = converter.toDomain(po);

        // Then
        assertNotNull(domain);
        assertEquals(new UserId(100L), domain.getUserId());
        assertEquals(new Username("testuser"), domain.getUsername());
        assertEquals(OperationType.LOGIN, domain.getOperation());
        assertEquals(ModuleType.AUTH, domain.getModule());
        assertEquals("用户登录成功", domain.getDescription());
        assertNull(domain.getTargetId());
        assertNotNull(domain.getTraceInfo());
        assertEquals("192.168.1.1", domain.getTraceInfo().ip());
        assertEquals("trace-123", domain.getTraceInfo().traceId());
        assertTrue(domain.isSuccess());
        assertNull(domain.getErrorMessage());
        assertEquals(150L, domain.getDuration());
        assertEquals(LocalDateTime.of(2024, 1, 1, 10, 0), domain.getCreatedAt());
    }

    @Test
    @DisplayName("toDomain - 正确转换失败日志")
    void toDomain_shouldConvertFailureLogCorrectly() {
        // Given
        AuditLogPO po = new AuditLogPO();
        po.setId(2L);
        po.setUserId(100L);
        po.setUsername("testuser");
        po.setOperation("LOGIN");
        po.setModule("AUTH");
        po.setDescription("用户登录失败");
        po.setStatus(0);  // 失败
        po.setErrorMsg("密码错误");
        po.setDuration(50L);
        po.setCreateTime(LocalDateTime.now());

        // When
        AuditLog domain = converter.toDomain(po);

        // Then
        assertFalse(domain.isSuccess());
        assertEquals("密码错误", domain.getErrorMessage());
    }

    @Test
    @DisplayName("toDomain - 处理空 PO")
    void toDomain_shouldReturnNullForNullPO() {
        assertNull(converter.toDomain(null));
    }
}
