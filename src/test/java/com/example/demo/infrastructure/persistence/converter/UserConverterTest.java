package com.example.demo.infrastructure.persistence.converter;

import com.example.demo.domain.user.aggregate.User;
import com.example.demo.domain.user.valueobject.Email;
import com.example.demo.domain.user.valueobject.EncryptedPassword;
import com.example.demo.domain.user.valueobject.UserId;
import com.example.demo.domain.user.valueobject.Username;
import com.example.demo.domain.user.valueobject.UserStatus;
import com.example.demo.infrastructure.persistence.po.UserPO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserConverter 测试
 */
class UserConverterTest {

    private UserConverter userConverter;

    @BeforeEach
    void setUp() {
        userConverter = new UserConverter();
    }

    @Test
    @DisplayName("PO 转 领域对象 - 成功")
    void toDomain_success_shouldConvertToUser() {
        // given
        UserPO po = new UserPO();
        po.setId(1L);
        po.setUsername("testuser");
        po.setEmail("test@example.com");
        po.setPassword("$2a$10$hashedPassword");
        po.setStatus(1);
        po.setCreateTime(LocalDateTime.now());
        po.setUpdateTime(LocalDateTime.now());

        // when
        User user = userConverter.toDomain(po);

        // then
        assertNotNull(user);
        assertEquals(1L, user.getId().value());
        assertEquals("testuser", user.getUsername().value());
        assertEquals("test@example.com", user.getEmail().value());
        assertEquals("$2a$10$hashedPassword", user.getPassword().value());
        assertEquals(UserStatus.ENABLED, user.getStatus());
    }

    @Test
    @DisplayName("PO 转 领域对象 - null 输入")
    void toDomain_nullInput_shouldReturnNull() {
        // when
        User user = userConverter.toDomain(null);

        // then
        assertNull(user);
    }

    @Test
    @DisplayName("领域对象 转 PO - 成功")
    void toPO_success_shouldConvertToPO() {
        // given
        User user = User.rebuild(
            new UserId(1L),
            new Username("testuser"),
            new Email("test@example.com"),
            new EncryptedPassword("$2a$10$hashedPassword"),
            UserStatus.ENABLED,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        // when
        UserPO po = userConverter.toPO(user);

        // then
        assertNotNull(po);
        assertEquals(1L, po.getId());
        assertEquals("testuser", po.getUsername());
        assertEquals("test@example.com", po.getEmail());
        assertEquals("$2a$10$hashedPassword", po.getPassword());
        assertEquals(1, po.getStatus());
    }

    @Test
    @DisplayName("领域对象 转 PO - null 输入")
    void toPO_nullInput_shouldReturnNull() {
        // when
        UserPO po = userConverter.toPO(null);

        // then
        assertNull(po);
    }

    @Test
    @DisplayName("领域对象 转 PO - 无 ID 的新用户")
    void toPO_newUser_shouldNotSetId() {
        // given
        User user = User.register(
            new Username("newuser"),
            new Email("new@example.com"),
            new EncryptedPassword("$2a$10$hashedPassword")
        );

        // when
        UserPO po = userConverter.toPO(user);

        // then
        assertNotNull(po);
        assertNull(po.getId());
        assertEquals("newuser", po.getUsername());
        assertEquals("new@example.com", po.getEmail());
    }
}