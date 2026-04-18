package com.jguard.domain.permission;

import com.jguard.domain.permission.valueobject.RoleCode;
import com.jguard.domain.shared.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("角色编码值对象测试")
class RoleCodeTest {

    @Test
    @DisplayName("有效编码应该创建成功")
    void shouldAcceptValidCode() {
        RoleCode code = new RoleCode("ADMIN");
        assertEquals("ADMIN", code.value());
    }

    @Test
    @DisplayName("null值应该抛出异常")
    void shouldRejectNullCode() {
        assertThrows(DomainException.class, () -> new RoleCode(null));
    }

    @Test
    @DisplayName("空白编码应该抛出异常")
    void shouldRejectBlankCode() {
        assertThrows(DomainException.class, () -> new RoleCode(""));
        assertThrows(DomainException.class, () -> new RoleCode("   "));
    }

    @Test
    @DisplayName("无效格式应该抛出异常")
    void shouldRejectInvalidFormat() {
        assertThrows(DomainException.class, () -> new RoleCode("admin"));  // 小写
        assertThrows(DomainException.class, () -> new RoleCode("1ADMIN")); // 数字开头
        assertThrows(DomainException.class, () -> new RoleCode("A"));      // 太短
    }
}