package com.jguard.interfaces.controller;

import com.jguard.application.service.UserApplicationService;
import com.jguard.domain.permission.repository.ResourceRepository;
import com.jguard.domain.permission.service.PermissionCacheServiceImpl;
import com.jguard.domain.permission.valueobject.PermissionBitmap;
import com.jguard.domain.user.aggregate.User;
import com.jguard.domain.user.valueobject.Email;
import com.jguard.domain.user.valueobject.EncryptedPassword;
import com.jguard.domain.user.valueobject.UserId;
import com.jguard.domain.user.valueobject.Username;
import com.jguard.domain.user.valueobject.UserStatus;
import com.jguard.interfaces.assembler.UserAssembler;
import com.jguard.interfaces.dto.response.UserResponse;
import com.jguard.infrastructure.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 用户查询控制器测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "rate-limit.fail-open=true",
    "jwt.secret=test-secret-key-at-least-256-bits-long-for-hs256-algorithm"
})
class UserQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserApplicationService userApplicationService;

    @MockBean
    private UserAssembler userAssembler;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private PermissionCacheServiceImpl permissionCacheService;

    @MockBean
    private ResourceRepository resourceRepository;

    @BeforeEach
    void setUp() {
        // Mock JwtUtil 让所有 token 都有效
        when(jwtUtil.validateAccessToken(anyString())).thenReturn(true);
        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(1L);
        when(jwtUtil.getUsernameFromToken(anyString())).thenReturn("testuser");

        // Mock PermissionCacheServiceImpl 返回空的权限位图（允许通过）
        PermissionBitmap emptyBitmap = PermissionBitmap.empty(System.currentTimeMillis());
        when(permissionCacheService.getPermissionBitmap(anyLong())).thenReturn(emptyBitmap);

        // Mock ResourceRepository 返回空的 API 资源列表（让 PermissionInterceptor 跳过权限检查）
        when(resourceRepository.findAllApis()).thenReturn(Collections.emptyList());
    }

    private User createTestUser(Long id, String username, String email, UserStatus status) {
        return User.rebuild(
            new UserId(id),
            new Username(username),
            new Email(email),
            new EncryptedPassword("$2a$10$encrypted"),
            status,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("查询用户列表 - 成功返回分页数据")
    void listUsers_success_shouldReturnPageResult() throws Exception {
        // given
        User user1 = createTestUser(1L, "张三", "zhangsan@example.com", UserStatus.ENABLED);
        User user2 = createTestUser(2L, "李四", "lisi@example.com", UserStatus.ENABLED);
        List<User> users = Arrays.asList(user1, user2);

        UserResponse response1 = new UserResponse(1L, "张三", "zhangsan@example.com", 1, null, null);
        UserResponse response2 = new UserResponse(2L, "李四", "lisi@example.com", 1, null, null);

        when(userApplicationService.listUsers(anyString(), any(), anyInt(), anyInt())).thenReturn(users);
        when(userApplicationService.countUsers(anyString(), any())).thenReturn(2L);
        when(userAssembler.toResponseList(users)).thenReturn(Arrays.asList(response1, response2));

        // when & then
        mockMvc.perform(get("/api/v1/users")
                .param("pageNum", "1")
                .param("pageSize", "10")
                .param("username", "张")
                .header("Authorization", "Bearer test-token")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.total").value(2))
            .andExpect(jsonPath("$.data.list.length()").value(2))
            .andExpect(jsonPath("$.data.list[0].username").value("张三"));

        verify(userApplicationService).listUsers("张", null, 1, 10);
        verify(userApplicationService).countUsers("张", null);
    }

    @Test
    @DisplayName("查询用户列表 - 空列表")
    void listUsers_empty_shouldReturnEmptyList() throws Exception {
        // given
        when(userApplicationService.listUsers(any(), any(), anyInt(), anyInt()))
            .thenReturn(Collections.emptyList());
        when(userApplicationService.countUsers(any(), any())).thenReturn(0L);
        when(userAssembler.toResponseList(any())).thenReturn(Collections.emptyList());

        // when & then
        mockMvc.perform(get("/api/v1/users")
                .param("pageNum", "1")
                .param("pageSize", "10")
                .header("Authorization", "Bearer test-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.total").value(0))
            .andExpect(jsonPath("$.data.list").isEmpty());
    }

    @Test
    @DisplayName("查询用户列表 - 按状态筛选")
    void listUsers_filterByStatus_shouldReturnFilteredResult() throws Exception {
        // given
        User user = createTestUser(1L, "张三", "zhangsan@example.com", UserStatus.ENABLED);
        UserResponse response = new UserResponse(1L, "张三", "zhangsan@example.com", 1, null, null);

        when(userApplicationService.listUsers(any(), eq(1), anyInt(), anyInt()))
            .thenReturn(Arrays.asList(user));
        when(userApplicationService.countUsers(any(), eq(1))).thenReturn(1L);
        when(userAssembler.toResponseList(any())).thenReturn(Arrays.asList(response));

        // when & then
        mockMvc.perform(get("/api/v1/users")
                .param("pageNum", "1")
                .param("pageSize", "10")
                .param("status", "1")
                .header("Authorization", "Bearer test-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    @DisplayName("根据ID查询用户 - 成功")
    void getUserById_success_shouldReturnUser() throws Exception {
        // given
        User user = createTestUser(1L, "张三", "zhangsan@example.com", UserStatus.ENABLED);
        UserResponse response = new UserResponse(1L, "张三", "zhangsan@example.com", 1, null, null);

        when(userApplicationService.getUserById(1L)).thenReturn(user);
        when(userAssembler.toResponse(user)).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/users/1")
                .header("Authorization", "Bearer test-token")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.id").value(1))
            .andExpect(jsonPath("$.data.username").value("张三"));

        verify(userApplicationService).getUserById(1L);
    }

    @Test
    @DisplayName("根据ID查询用户 - 用户不存在")
    void getUserById_notFound_shouldReturn404() throws Exception {
        // given
        when(userApplicationService.getUserById(999L)).thenReturn(null);

        // when & then
        mockMvc.perform(get("/api/v1/users/999")
                .header("Authorization", "Bearer test-token")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(404))
            .andExpect(jsonPath("$.message").value("用户不存在"));

        verify(userApplicationService).getUserById(999L);
        verify(userAssembler, never()).toResponse(any());
    }

    @Test
    @DisplayName("根据ID查询用户 - 默认分页参数")
    void listUsers_defaultParams_shouldUseDefaults() throws Exception {
        // given
        when(userApplicationService.listUsers(any(), any(), anyInt(), anyInt()))
            .thenReturn(Collections.emptyList());
        when(userApplicationService.countUsers(any(), any())).thenReturn(0L);
        when(userAssembler.toResponseList(any())).thenReturn(Collections.emptyList());

        // when & then - 不传参数时，应使用默认值
        mockMvc.perform(get("/api/v1/users")
                .header("Authorization", "Bearer test-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }
}