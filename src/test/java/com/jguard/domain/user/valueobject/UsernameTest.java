package com.jguard.domain.user.valueobject;

import com.jguard.domain.shared.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户名值对象单元测试
 */
class UsernameTest {

    @Test
    @DisplayName("有效用户名应该创建成功")
    void validUsername_shouldCreate() {
        // when
        Username username = new Username("testuser");

        // then
        assertEquals("testuser", username.value());
    }

    @Test
    @DisplayName("空用户名应该抛出异常")
    void nullUsername_shouldThrowException() {
        assertThrows(DomainException.class, () -> new Username(null));
    }

    @Test
    @DisplayName("过短用户名应该抛出异常")
    void tooShortUsername_shouldThrowException() {
        assertThrows(DomainException.class, () -> new Username("a"));
    }

    @Test
    @DisplayName("过长用户名应该抛出异常")
    void tooLongUsername_shouldThrowException() {
        String longName = "a".repeat(51);
        assertThrows(DomainException.class, () -> new Username(longName));
    }

    @Test
    @DisplayName("边界值用户名应该创建成功")
    void boundaryUsername_shouldCreate() {
        // 2 字符 - 最小边界
        assertDoesNotThrow(() -> new Username("ab"));

        // 50 字符 - 最大边界
        String maxName = "a".repeat(50);
        assertDoesNotThrow(() -> new Username(maxName));
    }
}