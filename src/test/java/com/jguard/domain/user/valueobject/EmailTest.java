package com.jguard.domain.user.valueobject;

import com.jguard.domain.shared.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 邮箱值对象单元测试
 */
class EmailTest {

    @Test
    @DisplayName("有效邮箱应该创建成功")
    void validEmail_shouldCreate() {
        // when
        Email email = new Email("test@example.com");

        // then
        assertEquals("test@example.com", email.value());
    }

    @Test
    @DisplayName("空邮箱应该抛出异常")
    void nullEmail_shouldThrowException() {
        assertThrows(DomainException.class, () -> new Email(null));
    }

    @Test
    @DisplayName("无效邮箱格式应该抛出异常")
    void invalidEmail_shouldThrowException() {
        assertThrows(DomainException.class, () -> new Email("invalid-email"));
        assertThrows(DomainException.class, () -> new Email("test@"));
        assertThrows(DomainException.class, () -> new Email("@example.com"));
    }

    @Test
    @DisplayName("各种有效邮箱格式")
    void variousValidEmails_shouldCreate() {
        assertDoesNotThrow(() -> new Email("user@example.com"));
        assertDoesNotThrow(() -> new Email("user.name@example.com"));
        assertDoesNotThrow(() -> new Email("user+tag@example.com"));
        assertDoesNotThrow(() -> new Email("user@sub.example.com"));
    }
}