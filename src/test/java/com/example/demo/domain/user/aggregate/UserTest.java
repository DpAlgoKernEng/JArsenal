package com.example.demo.domain.user.aggregate;

import com.example.demo.domain.shared.exception.DomainException;
import com.example.demo.domain.user.valueobject.Email;
import com.example.demo.domain.user.valueobject.EncryptedPassword;
import com.example.demo.domain.user.valueobject.UserStatus;
import com.example.demo.domain.user.valueobject.Username;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * User 聚合根单元测试
 */
class UserTest {

    @Test
    @DisplayName("注册新用户应该创建启用状态的用户")
    void register_shouldCreateEnabledUser() {
        // given
        Username username = new Username("testuser");
        Email email = new Email("test@example.com");
        EncryptedPassword password = new EncryptedPassword("$2a$10$abcdefghijklmnopqrstuvwxyz1234567890ABCDEFGHIJ");

        // when
        User user = User.register(username, email, password);

        // then
        assertNull(user.getId());  // ID 在持久化前为 null
        assertEquals("testuser", user.getUsername().value());
        assertEquals("test@example.com", user.getEmail().value());
        assertEquals(UserStatus.ENABLED, user.getStatus());
        assertNotNull(user.getCreateTime());
        assertNotNull(user.getUpdateTime());
    }

    @Test
    @DisplayName("设置持久化后的 ID")
    void setIdAfterPersist_shouldSetId() {
        // given
        User user = createTestUser();

        // when
        user.setIdAfterPersist(123L);

        // then
        assertNotNull(user.getId());
        assertEquals(123L, user.getId().value());
    }

    @Test
    @DisplayName("重建用户应该包含正确的 ID")
    void rebuild_shouldHaveCorrectId() {
        // given
        Long expectedId = 456L;

        // when
        User user = User.rebuild(
            new com.example.demo.domain.user.valueobject.UserId(expectedId),
            new Username("testuser"),
            new Email("test@example.com"),
            new EncryptedPassword("$2a$10$abcdefghijklmnopqrstuvwxyz1234567890ABCDEFGHIJ"),
            UserStatus.ENABLED,
            null, null
        );

        // then
        assertNotNull(user.getId());
        assertEquals(expectedId, user.getId().value());
    }

    @Test
    @DisplayName("注册新用户应该生成 UserRegistered 事件")
    void register_shouldGenerateUserRegisteredEvent() {
        // given
        Username username = new Username("testuser");
        Email email = new Email("test@example.com");
        EncryptedPassword password = new EncryptedPassword("$2a$10$abcdefghijklmnopqrstuvwxyz1234567890ABCDEFGHIJ");

        // when
        User user = User.register(username, email, password);

        // then
        assertEquals(1, user.pendingEvents().size());
        assertTrue(user.pendingEvents().get(0) instanceof com.example.demo.domain.user.event.UserRegistered);
    }

    @Test
    @DisplayName("禁用用户应该将状态改为禁用")
    void disable_shouldChangeStatusToDisabled() {
        // given
        User user = createTestUserWithId();

        // when
        user.disable();

        // then
        assertEquals(UserStatus.DISABLED, user.getStatus());
    }

    @Test
    @DisplayName("重复禁用已禁用的用户应该抛出异常")
    void disable_alreadyDisabled_shouldThrowException() {
        // given
        User user = createTestUserWithId();
        user.disable();

        // when & then
        assertThrows(DomainException.class, user::disable);
    }

    @Test
    @DisplayName("启用用户应该将状态改为启用")
    void enable_shouldChangeStatusToEnabled() {
        // given
        User user = createTestUserWithId();
        user.disable();

        // when
        user.enable();

        // then
        assertEquals(UserStatus.ENABLED, user.getStatus());
    }

    @Test
    @DisplayName("重复启用已启用的用户应该抛出异常")
    void enable_alreadyEnabled_shouldThrowException() {
        // given
        User user = createTestUserWithId();

        // when & then
        assertThrows(DomainException.class, user::enable);
    }

    @Test
    @DisplayName("禁用用户应该生成 UserStatusChanged 事件")
    void disable_shouldGenerateStatusChangedEvent() {
        // given
        User user = createTestUserWithId();
        user.clearPendingEvents();

        // when
        user.disable();

        // then
        assertEquals(1, user.pendingEvents().size());
        assertTrue(user.pendingEvents().get(0) instanceof com.example.demo.domain.user.event.UserStatusChanged);
    }

    @Test
    @DisplayName("验证可登录状态 - 启用用户应该通过")
    void validateCanLogin_enabledUser_shouldPass() {
        // given
        User user = createTestUserWithId();

        // when & then
        assertDoesNotThrow(user::validateCanLogin);
    }

    @Test
    @DisplayName("验证可登录状态 - 禁用用户应该抛出异常")
    void validateCanLogin_disabledUser_shouldThrowException() {
        // given
        User user = createTestUserWithId();
        user.disable();

        // when & then
        assertThrows(DomainException.class, user::validateCanLogin);
    }

    private User createTestUser() {
        return User.register(
            new Username("testuser"),
            new Email("test@example.com"),
            new EncryptedPassword("$2a$10$abcdefghijklmnopqrstuvwxyz1234567890ABCDEFGHIJ")
        );
    }

    private User createTestUserWithId() {
        User user = createTestUser();
        user.setIdAfterPersist(1L);
        return user;
    }
}