package com.example.demo.domain.user.valueobject;

import com.example.demo.domain.shared.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户 ID 值对象单元测试
 */
class UserIdTest {

    @Test
    @DisplayName("有效 Long ID 应该创建成功")
    void validLongId_shouldCreate() {
        // when
        UserId userId = new UserId(123L);

        // then
        assertEquals(123L, userId.value());
    }

    @Test
    @DisplayName("null ID 应该抛出异常")
    void nullId_shouldThrowException() {
        assertThrows(DomainException.class, () -> new UserId(null));
    }

    @Test
    @DisplayName("负数 ID 应该抛出异常")
    void negativeId_shouldThrowException() {
        assertThrows(DomainException.class, () -> new UserId(-1L));
    }

    @Test
    @DisplayName("零 ID 应该抛出异常")
    void zeroId_shouldThrowException() {
        assertThrows(DomainException.class, () -> new UserId(0L));
    }

    @Test
    @DisplayName("从字符串创建有效 ID")
    void fromString_validString_shouldCreate() {
        // when
        UserId userId = UserId.fromString("456");

        // then
        assertEquals(456L, userId.value());
    }

    @Test
    @DisplayName("从无效字符串创建应该抛出异常")
    void fromString_invalidString_shouldThrowException() {
        assertThrows(DomainException.class, () -> UserId.fromString("invalid"));
        assertThrows(DomainException.class, () -> UserId.fromString(null));
        assertThrows(DomainException.class, () -> UserId.fromString(""));
    }

    @Test
    @DisplayName("asString 应该返回字符串形式")
    void asString_shouldReturnStringValue() {
        // given
        UserId userId = new UserId(789L);

        // when
        String result = userId.asString();

        // then
        assertEquals("789", result);
    }
}