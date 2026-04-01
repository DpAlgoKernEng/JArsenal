package com.example.demo.service;

import com.example.demo.dto.PageResult;
import com.example.demo.dto.UserQueryRequest;
import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.impl.UserServiceImpl;
import com.github.pagehelper.PageInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * UserService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserQueryRequest queryRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setEmail("test@example.com");
        testUser.setStatus(1);
        testUser.setCreateTime(LocalDateTime.now());
        testUser.setUpdateTime(LocalDateTime.now());

        queryRequest = new UserQueryRequest();
        queryRequest.setPageNum(1);
        queryRequest.setPageSize(10);
    }

    // ========== listUsers Tests ==========

    @Test
    @DisplayName("分页查询用户 - 成功")
    void listUsers_success_shouldReturnPageResult() {
        // given
        List<User> users = Arrays.asList(testUser);
        when(userMapper.selectByCondition(any(), any())).thenReturn(users);

        // when
        PageResult<User> result = userService.listUsers(queryRequest);

        // then
        assertNotNull(result);
        // 注意：由于 PageHelper 需要真实环境，这里主要验证方法调用
        verify(userMapper).selectByCondition(queryRequest.getUsername(), queryRequest.getStatus());
    }

    @Test
    @DisplayName("分页查询用户 - 空列表")
    void listUsers_emptyList_shouldReturnEmptyResult() {
        // given
        when(userMapper.selectByCondition(any(), any())).thenReturn(Collections.emptyList());

        // when
        PageResult<User> result = userService.listUsers(queryRequest);

        // then
        assertNotNull(result);
        verify(userMapper).selectByCondition(queryRequest.getUsername(), queryRequest.getStatus());
    }

    @Test
    @DisplayName("分页查询用户 - 带条件")
    void listUsers_withCondition_shouldQueryWithParams() {
        // given
        queryRequest.setUsername("test");
        queryRequest.setStatus(1);
        when(userMapper.selectByCondition("test", 1)).thenReturn(Arrays.asList(testUser));

        // when
        userService.listUsers(queryRequest);

        // then
        verify(userMapper).selectByCondition("test", 1);
    }

    // ========== getUserById Tests ==========

    @Test
    @DisplayName("根据ID查询用户 - 成功")
    void getUserById_success_shouldReturnUser() {
        // given
        when(userMapper.selectById(1L)).thenReturn(testUser);

        // when
        User result = userService.getUserById(1L);

        // then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());

        verify(userMapper).selectById(1L);
    }

    @Test
    @DisplayName("根据ID查询用户 - 不存在")
    void getUserById_notFound_shouldReturnNull() {
        // given
        when(userMapper.selectById(999L)).thenReturn(null);

        // when
        User result = userService.getUserById(999L);

        // then
        assertNull(result);

        verify(userMapper).selectById(999L);
    }

    // ========== createUser Tests ==========

    @Test
    @DisplayName("创建用户 - 成功")
    void createUser_success_shouldReturnUserWithId() {
        // given
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("new@example.com");

        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L);
            return 1;
        });

        // when
        User result = userService.createUser(newUser);

        // then
        assertNotNull(result);
        assertEquals(2L, result.getId());

        verify(userMapper).insert(any(User.class));
    }

    // ========== updateUser Tests ==========

    @Test
    @DisplayName("更新用户 - 成功")
    void updateUser_success_shouldReturnUpdatedUser() {
        // given
        User updateUser = new User();
        updateUser.setId(1L);
        updateUser.setEmail("updated@example.com");

        when(userMapper.update(any(User.class))).thenReturn(1);
        when(userMapper.selectById(1L)).thenReturn(testUser);

        // when
        User result = userService.updateUser(updateUser);

        // then
        assertNotNull(result);

        verify(userMapper).update(any(User.class));
        verify(userMapper).selectById(1L);
    }

    // ========== deleteUser Tests ==========

    @Test
    @DisplayName("删除用户 - 成功")
    void deleteUser_success_shouldCallMapper() {
        // given
        when(userMapper.deleteById(1L)).thenReturn(1);

        // when
        userService.deleteUser(1L);

        // then
        verify(userMapper).deleteById(1L);
    }
}