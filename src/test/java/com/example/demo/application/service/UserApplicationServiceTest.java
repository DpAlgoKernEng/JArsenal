package com.example.demo.application.service;

import com.example.demo.application.command.RegisterCommand;
import com.example.demo.application.command.UpdateUserCommand;
import com.example.demo.domain.shared.exception.DomainException;
import com.example.demo.domain.user.aggregate.User;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.domain.user.service.UserDomainService;
import com.example.demo.domain.user.valueobject.Email;
import com.example.demo.domain.user.valueobject.EncryptedPassword;
import com.example.demo.domain.user.valueobject.UserId;
import com.example.demo.domain.user.valueobject.Username;
import com.example.demo.domain.user.valueobject.UserStatus;
import com.example.demo.infrastructure.outbox.OutboxDomainEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 用户应用服务测试
 */
@ExtendWith(MockitoExtension.class)
class UserApplicationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDomainService userDomainService;

    @Mock
    private OutboxDomainEventPublisher eventPublisher;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserApplicationService userApplicationService;

    private User testUser;

    @BeforeEach
    void setUp() {
        userApplicationService = new UserApplicationService(
            userRepository, userDomainService, eventPublisher, passwordEncoder
        );

        testUser = User.rebuild(
            new UserId(1L),
            new Username("testuser"),
            new Email("test@example.com"),
            new EncryptedPassword("$2a$10$hashedPassword"),
            UserStatus.ENABLED,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("注册成功 - 创建用户并返回ID")
    void register_success_shouldReturnUserId() {
        // given
        RegisterCommand command = new RegisterCommand("newuser", "password123", "new@example.com");

        doNothing().when(userDomainService).ensureUsernameUnique(any(Username.class));
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encryptedPassword");
        doNothing().when(userRepository).save(any(User.class));
        doNothing().when(eventPublisher).setAggregateContext(anyString(), anyString());
        doNothing().when(eventPublisher).publishAll(anyList());
        doNothing().when(eventPublisher).clearAggregateContext();

        // when
        Long userId = userApplicationService.register(command);

        // then
        assertNotNull(userId);

        verify(userDomainService).ensureUsernameUnique(any(Username.class));
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(eventPublisher).publishAll(anyList());
    }

    @Test
    @DisplayName("注册失败 - 用户名已存在")
    void register_duplicateUsername_shouldThrowException() {
        // given
        RegisterCommand command = new RegisterCommand("existinguser", "password123", "test@example.com");

        doThrow(new DomainException("用户名已存在"))
            .when(userDomainService).ensureUsernameUnique(any(Username.class));

        // when & then
        assertThrows(DomainException.class, () -> userApplicationService.register(command));

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("更新用户成功 - 更新资料")
    void updateUser_success_shouldUpdateProfile() {
        // given
        UpdateUserCommand command = new UpdateUserCommand(1L, "newusername", "new@example.com", null);

        when(userRepository.findById(new UserId(1L))).thenReturn(testUser);
        doNothing().when(userRepository).save(any(User.class));
        doNothing().when(eventPublisher).setAggregateContext(anyString(), anyString());
        doNothing().when(eventPublisher).publishAll(anyList());

        // when
        userApplicationService.updateUser(command);

        // then
        verify(userRepository).findById(new UserId(1L));
        verify(userRepository).save(any(User.class));
        verify(eventPublisher).publishAll(anyList());
    }

    @Test
    @DisplayName("更新用户成功 - 启用账号")
    void updateUser_enable_shouldChangeStatus() {
        // given
        User disabledUser = User.rebuild(
            new UserId(1L),
            new Username("testuser"),
            new Email("test@example.com"),
            new EncryptedPassword("$2a$10$hashedPassword"),
            UserStatus.DISABLED,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        UpdateUserCommand command = new UpdateUserCommand(1L, null, null, 1);

        when(userRepository.findById(new UserId(1L))).thenReturn(disabledUser);
        doNothing().when(userRepository).save(any(User.class));
        doNothing().when(eventPublisher).setAggregateContext(anyString(), anyString());
        doNothing().when(eventPublisher).publishAll(anyList());

        // when
        userApplicationService.updateUser(command);

        // then
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("更新用户成功 - 禁用账号")
    void updateUser_disable_shouldChangeStatus() {
        // given
        UpdateUserCommand command = new UpdateUserCommand(1L, null, null, 0);

        when(userRepository.findById(new UserId(1L))).thenReturn(testUser);
        doNothing().when(userRepository).save(any(User.class));
        doNothing().when(eventPublisher).setAggregateContext(anyString(), anyString());
        doNothing().when(eventPublisher).publishAll(anyList());

        // when
        userApplicationService.updateUser(command);

        // then
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("更新用户失败 - 用户不存在")
    void updateUser_notFound_shouldThrowException() {
        // given
        UpdateUserCommand command = new UpdateUserCommand(999L, "newusername", "new@example.com", null);

        when(userRepository.findById(new UserId(999L))).thenReturn(null);

        // when & then
        assertThrows(DomainException.class, () -> userApplicationService.updateUser(command));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("删除用户成功")
    void deleteUser_success_shouldDelete() {
        // given
        doNothing().when(userRepository).delete(new UserId(1L));

        // when
        userApplicationService.deleteUser(1L);

        // then
        verify(userRepository).delete(new UserId(1L));
    }

    @Test
    @DisplayName("根据ID查询用户 - 成功")
    void getUserById_success_shouldReturnUser() {
        // given
        when(userRepository.findById(new UserId(1L))).thenReturn(testUser);

        // when
        User result = userApplicationService.getUserById(1L);

        // then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername().value());
    }

    @Test
    @DisplayName("根据ID查询用户 - 不存在")
    void getUserById_notFound_shouldReturnNull() {
        // given
        when(userRepository.findById(new UserId(999L))).thenReturn(null);

        // when
        User result = userApplicationService.getUserById(999L);

        // then
        assertNull(result);
    }

    @Test
    @DisplayName("查询用户列表 - 成功")
    void listUsers_success_shouldReturnList() {
        // given
        User user1 = User.rebuild(
            new UserId(1L), new Username("user1"), new Email("user1@example.com"),
            new EncryptedPassword("$2a$10$hashed"), UserStatus.ENABLED,
            LocalDateTime.now(), LocalDateTime.now()
        );
        User user2 = User.rebuild(
            new UserId(2L), new Username("user2"), new Email("user2@example.com"),
            new EncryptedPassword("$2a$10$hashed"), UserStatus.ENABLED,
            LocalDateTime.now(), LocalDateTime.now()
        );

        when(userRepository.findAll(any(), any(), anyInt(), anyInt()))
            .thenReturn(Arrays.asList(user1, user2));

        // when
        List<User> result = userApplicationService.listUsers(null, null, 1, 10);

        // then
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("查询用户列表 - 空列表")
    void listUsers_empty_shouldReturnEmptyList() {
        // given
        when(userRepository.findAll(any(), any(), anyInt(), anyInt()))
            .thenReturn(Collections.emptyList());

        // when
        List<User> result = userApplicationService.listUsers("nonexistent", null, 1, 10);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("统计用户数量 - 成功")
    void countUsers_success_shouldReturnCount() {
        // given
        when(userRepository.count(any(), any())).thenReturn(10L);

        // when
        long count = userApplicationService.countUsers(null, null);

        // then
        assertEquals(10L, count);
    }

    @Test
    @DisplayName("统计用户数量 - 按状态筛选")
    void countUsers_filterByStatus_shouldReturnFilteredCount() {
        // given
        when(userRepository.count(any(), eq(1))).thenReturn(8L);

        // when
        long count = userApplicationService.countUsers(null, 1);

        // then
        assertEquals(8L, count);
        verify(userRepository).count(any(), eq(1));
    }
}