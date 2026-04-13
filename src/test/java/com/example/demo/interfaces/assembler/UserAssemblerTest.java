package com.example.demo.interfaces.assembler;

import com.example.demo.domain.user.aggregate.User;
import com.example.demo.domain.user.valueobject.Email;
import com.example.demo.domain.user.valueobject.EncryptedPassword;
import com.example.demo.domain.user.valueobject.UserId;
import com.example.demo.domain.user.valueobject.Username;
import com.example.demo.domain.user.valueobject.UserStatus;
import com.example.demo.interfaces.dto.response.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserAssembler 测试
 */
class UserAssemblerTest {

    private UserAssembler userAssembler;

    @BeforeEach
    void setUp() {
        userAssembler = new UserAssembler();
    }

    @Test
    @DisplayName("领域对象 转 Response - 成功")
    void toResponse_success_shouldConvertToResponse() {
        // given
        LocalDateTime createTime = LocalDateTime.now().minusDays(1);
        LocalDateTime updateTime = LocalDateTime.now();
        User user = User.rebuild(
            new UserId(1L),
            new Username("testuser"),
            new Email("test@example.com"),
            new EncryptedPassword("$2a$10$hashedPassword"),
            UserStatus.ENABLED,
            createTime,
            updateTime
        );

        // when
        UserResponse response = userAssembler.toResponse(user);

        // then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        assertEquals(1, response.getStatus());
        assertEquals(createTime, response.getCreateTime());
        assertEquals(updateTime, response.getUpdateTime());
    }

    @Test
    @DisplayName("领域对象 转 Response - null 输入")
    void toResponse_nullInput_shouldReturnNull() {
        // when
        UserResponse response = userAssembler.toResponse(null);

        // then
        assertNull(response);
    }

    @Test
    @DisplayName("领域对象 转 Response - 禁用状态")
    void toResponse_disabledUser_shouldReturnStatusZero() {
        // given
        User user = User.rebuild(
            new UserId(1L),
            new Username("testuser"),
            new Email("test@example.com"),
            new EncryptedPassword("$2a$10$hashedPassword"),
            UserStatus.DISABLED,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        // when
        UserResponse response = userAssembler.toResponse(user);

        // then
        assertNotNull(response);
        assertEquals(0, response.getStatus());
    }

    @Test
    @DisplayName("领域对象列表 转 Response 列表 - 成功")
    void toResponseList_success_shouldConvertToList() {
        // given
        User user1 = User.rebuild(
            new UserId(1L),
            new Username("user1"),
            new Email("user1@example.com"),
            new EncryptedPassword("$2a$10$hashed"),
            UserStatus.ENABLED,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        User user2 = User.rebuild(
            new UserId(2L),
            new Username("user2"),
            new Email("user2@example.com"),
            new EncryptedPassword("$2a$10$hashed"),
            UserStatus.ENABLED,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        List<User> users = Arrays.asList(user1, user2);

        // when
        List<UserResponse> responses = userAssembler.toResponseList(users);

        // then
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("user1", responses.get(0).getUsername());
        assertEquals("user2", responses.get(1).getUsername());
    }

    @Test
    @DisplayName("领域对象列表 转 Response 列表 - 空列表")
    void toResponseList_emptyList_shouldReturnEmptyList() {
        // when
        List<UserResponse> responses = userAssembler.toResponseList(List.of());

        // then
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }
}