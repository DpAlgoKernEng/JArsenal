package com.example.demo.domain.permission;

import com.example.demo.domain.permission.valueobject.RoleId;
import com.example.demo.domain.shared.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("角色ID值对象测试")
class RoleIdTest {

    @Test
    @DisplayName("有效正整数ID应该创建成功")
    void validPositiveId_shouldCreate() {
        RoleId id = new RoleId(1L);
        assertEquals(1L, id.value());
    }

    @Test
    @DisplayName("null值应该抛出异常")
    void nullValue_shouldThrow() {
        assertThrows(DomainException.class, () -> new RoleId(null));
    }

    @Test
    @DisplayName("零值应该抛出异常")
    void zeroValue_shouldThrow() {
        assertThrows(DomainException.class, () -> new RoleId(0L));
    }

    @Test
    @DisplayName("负数应该抛出异常")
    void negativeValue_shouldThrow() {
        assertThrows(DomainException.class, () -> new RoleId(-1L));
    }
}