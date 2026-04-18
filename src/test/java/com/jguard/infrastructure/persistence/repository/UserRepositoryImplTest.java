package com.jguard.infrastructure.persistence.repository;

import com.jguard.domain.user.aggregate.User;
import com.jguard.domain.user.valueobject.Email;
import com.jguard.domain.user.valueobject.EncryptedPassword;
import com.jguard.domain.user.valueobject.UserId;
import com.jguard.domain.user.valueobject.Username;
import com.jguard.domain.user.valueobject.UserStatus;
import com.jguard.infrastructure.persistence.converter.UserConverter;
import com.jguard.infrastructure.persistence.mapper.UserMapper;
import com.jguard.infrastructure.persistence.po.UserPO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 用户仓库实现测试
 */
@ExtendWith(MockitoExtension.class)
class UserRepositoryImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserConverter userConverter;

    private UserRepositoryImpl userRepository;

    @BeforeEach
    void setUp() {
        userRepository = new UserRepositoryImpl(userMapper, userConverter);
    }

    @Test
    @DisplayName("根据ID查询 - 找到用户")
    void findById_found_shouldReturnUser() {
        // given
        UserPO po = new UserPO();
        po.setId(1L);
        po.setUsername("testuser");
        po.setEmail("test@example.com");

        User user = User.rebuild(
            new UserId(1L),
            new Username("testuser"),
            new Email("test@example.com"),
            new EncryptedPassword("$2a$10$hashed"),
            UserStatus.ENABLED,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        when(userMapper.selectById(1L)).thenReturn(po);
        when(userConverter.toDomain(po)).thenReturn(user);

        // when
        User result = userRepository.findById(new UserId(1L));

        // then
        assertNotNull(result);
        assertEquals(1L, result.getId().value());

        verify(userMapper).selectById(1L);
        verify(userConverter).toDomain(po);
    }

    @Test
    @DisplayName("根据ID查询 - 用户不存在")
    void findById_notFound_shouldReturnNull() {
        // given
        when(userMapper.selectById(999L)).thenReturn(null);
        when(userConverter.toDomain(null)).thenReturn(null);

        // when
        User result = userRepository.findById(new UserId(999L));

        // then
        assertNull(result);
    }

    @Test
    @DisplayName("根据用户名查询 - 找到用户")
    void findByUsername_found_shouldReturnUser() {
        // given
        UserPO po = new UserPO();
        po.setId(1L);
        po.setUsername("testuser");

        User user = User.rebuild(
            new UserId(1L),
            new Username("testuser"),
            new Email("test@example.com"),
            new EncryptedPassword("$2a$10$hashed"),
            UserStatus.ENABLED,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        when(userMapper.selectByUsername("testuser")).thenReturn(po);
        when(userConverter.toDomain(po)).thenReturn(user);

        // when
        User result = userRepository.findByUsername(new Username("testuser"));

        // then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername().value());
    }

    @Test
    @DisplayName("保存新用户 - 插入并设置ID")
    void save_newUser_shouldInsertAndSetId() {
        // given
        User newUser = User.register(
            new Username("newuser"),
            new Email("new@example.com"),
            new EncryptedPassword("$2a$10$hashed")
        );

        UserPO po = new UserPO();
        po.setUsername("newuser");
        po.setEmail("new@example.com");

        when(userConverter.toPO(newUser)).thenReturn(po);
        when(userMapper.insert(po)).thenAnswer(invocation -> {
            UserPO insertedPo = invocation.getArgument(0);
            insertedPo.setId(10L); // 模拟数据库生成ID
            return 1;
        });

        // when
        userRepository.save(newUser);

        // then
        verify(userMapper).insert(po);
        assertEquals(10L, newUser.getId().value());
    }

    @Test
    @DisplayName("保存已存在用户 - 更新")
    void save_existingUser_shouldUpdate() {
        // given
        User existingUser = User.rebuild(
            new UserId(1L),
            new Username("testuser"),
            new Email("updated@example.com"),
            new EncryptedPassword("$2a$10$hashed"),
            UserStatus.ENABLED,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        UserPO po = new UserPO();
        po.setId(1L);
        po.setUsername("testuser");
        po.setEmail("updated@example.com");

        when(userConverter.toPO(existingUser)).thenReturn(po);
        when(userMapper.update(po)).thenReturn(1);

        // when
        userRepository.save(existingUser);

        // then
        verify(userMapper).update(po);
        verify(userMapper, never()).insert(any());
    }

    @Test
    @DisplayName("删除用户")
    void delete_shouldCallDeleteById() {
        // given
        when(userMapper.deleteById(1L)).thenReturn(1);

        // when
        userRepository.delete(new UserId(1L));

        // then
        verify(userMapper).deleteById(1L);
    }

    @Test
    @DisplayName("检查用户名存在 - 存在")
    void existsByUsername_exists_shouldReturnTrue() {
        // given
        when(userMapper.countByUsername("existinguser")).thenReturn(1);

        // when
        boolean result = userRepository.existsByUsername(new Username("existinguser"));

        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("检查用户名存在 - 不存在")
    void existsByUsername_notExists_shouldReturnFalse() {
        // given
        when(userMapper.countByUsername("nonexistent")).thenReturn(0);

        // when
        boolean result = userRepository.existsByUsername(new Username("nonexistent"));

        // then
        assertFalse(result);
    }

    @Test
    @DisplayName("分页查询用户列表")
    void findAll_withPagination_shouldReturnList() {
        // given
        UserPO po1 = new UserPO();
        po1.setId(1L);
        po1.setUsername("user1");

        UserPO po2 = new UserPO();
        po2.setId(2L);
        po2.setUsername("user2");

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

        when(userMapper.selectByCondition(any(), any())).thenReturn(Arrays.asList(po1, po2));
        when(userConverter.toDomain(po1)).thenReturn(user1);
        when(userConverter.toDomain(po2)).thenReturn(user2);

        // when
        List<User> result = userRepository.findAll(null, null, 1, 10);

        // then
        assertEquals(2, result.size());
        verify(userMapper).selectByCondition(null, null);
    }

    @Test
    @DisplayName("分页查询 - 空结果")
    void findAll_emptyResult_shouldReturnEmptyList() {
        // given
        when(userMapper.selectByCondition(any(), any())).thenReturn(Collections.emptyList());

        // when
        List<User> result = userRepository.findAll("nonexistent", null, 1, 10);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("统计用户数量")
    void count_shouldReturnCount() {
        // given
        when(userMapper.countByCondition(any(), any())).thenReturn(100L);

        // when
        long count = userRepository.count(null, null);

        // then
        assertEquals(100L, count);
    }

    @Test
    @DisplayName("查询所有用户")
    void findAll_noParams_shouldReturnAllUsers() {
        // given
        UserPO po = new UserPO();
        po.setId(1L);
        po.setUsername("user1");

        User user = User.rebuild(
            new UserId(1L), new Username("user1"), new Email("user1@example.com"),
            new EncryptedPassword("$2a$10$hashed"), UserStatus.ENABLED,
            LocalDateTime.now(), LocalDateTime.now()
        );

        when(userMapper.selectAll()).thenReturn(Arrays.asList(po));
        when(userConverter.toDomain(po)).thenReturn(user);

        // when
        List<User> result = userRepository.findAll();

        // then
        assertEquals(1, result.size());
        verify(userMapper).selectAll();
    }
}