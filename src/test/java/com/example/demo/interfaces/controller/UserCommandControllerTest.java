package com.example.demo.interfaces.controller;

import com.example.demo.application.command.RegisterCommand;
import com.example.demo.application.command.UpdateUserCommand;
import com.example.demo.application.service.UserApplicationService;
import com.example.demo.domain.permission.repository.ResourceRepository;
import com.example.demo.domain.permission.service.PermissionCacheServiceImpl;
import com.example.demo.domain.permission.valueobject.PermissionBitmap;
import com.example.demo.domain.shared.exception.DomainException;
import com.example.demo.domain.user.aggregate.User;
import com.example.demo.domain.user.valueobject.Email;
import com.example.demo.domain.user.valueobject.EncryptedPassword;
import com.example.demo.domain.user.valueobject.UserId;
import com.example.demo.domain.user.valueobject.Username;
import com.example.demo.domain.user.valueobject.UserStatus;
import com.example.demo.interfaces.assembler.UserAssembler;
import com.example.demo.interfaces.dto.request.UserCreateRequest;
import com.example.demo.interfaces.dto.request.UserUpdateRequest;
import com.example.demo.interfaces.dto.response.UserResponse;
import com.example.demo.infrastructure.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 用户命令控制器测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "rate-limit.fail-open=true",
    "jwt.secret=test-secret-key-at-least-256-bits-long-for-hs256-algorithm"
})
class UserCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserApplicationService userApplicationService;

    @MockBean
    private UserAssembler userAssembler;

    @MockBean
    private PasswordEncoder passwordEncoder;

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
    @DisplayName("创建用户 - 成功")
    void createUser_success_shouldReturnCreatedUser() throws Exception {
        // given
        UserCreateRequest request = new UserCreateRequest("新用户", "password123", "new@example.com", 1);
        User createdUser = createTestUser(10L, "新用户", "new@example.com", UserStatus.ENABLED);
        UserResponse response = new UserResponse(10L, "新用户", "new@example.com", 1, null, null);

        when(userApplicationService.register(any(RegisterCommand.class))).thenReturn(10L);
        when(userApplicationService.getUserById(10L)).thenReturn(createdUser);
        when(userAssembler.toResponse(createdUser)).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/users")
                .header("Authorization", "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.username").value("新用户"));

        verify(userApplicationService).register(any(RegisterCommand.class));
        verify(userApplicationService).getUserById(10L);
    }

    @Test
    @DisplayName("创建用户 - 用户名已存在")
    void createUser_duplicateUsername_shouldReturnError() throws Exception {
        // given
        UserCreateRequest request = new UserCreateRequest("已存在用户", "password123", "test@example.com", 1);

        when(userApplicationService.register(any(RegisterCommand.class)))
            .thenThrow(new DomainException("用户名已存在"));

        // when & then
        mockMvc.perform(post("/api/v1/users")
                .header("Authorization", "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").value("用户名已存在"));
    }

    @Test
    @DisplayName("创建用户 - 参数校验失败(用户名过短)")
    void createUser_invalidUsername_shouldReturnBadRequest() throws Exception {
        // given - 用户名少于2字符
        UserCreateRequest request = new UserCreateRequest("a", "password123", "test@example.com", 1);

        // when & then
        mockMvc.perform(post("/api/v1/users")
                .header("Authorization", "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(400));

        verify(userApplicationService, never()).register(any());
    }

    @Test
    @DisplayName("创建用户 - 参数校验失败(邮箱格式错误)")
    void createUser_invalidEmail_shouldReturnBadRequest() throws Exception {
        // given
        UserCreateRequest request = new UserCreateRequest("validuser", "password123", "invalid-email", 1);

        // when & then
        mockMvc.perform(post("/api/v1/users")
                .header("Authorization", "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(400));

        verify(userApplicationService, never()).register(any());
    }

    @Test
    @DisplayName("更新用户 - 成功")
    void updateUser_success_shouldReturnUpdatedUser() throws Exception {
        // given
        UserUpdateRequest request = new UserUpdateRequest("新名字", "new@example.com", 1);
        User updatedUser = createTestUser(1L, "新名字", "new@example.com", UserStatus.ENABLED);
        UserResponse response = new UserResponse(1L, "新名字", "new@example.com", 1, null, null);

        when(userApplicationService.getUserById(1L)).thenReturn(updatedUser);
        when(userAssembler.toResponse(updatedUser)).thenReturn(response);
        doNothing().when(userApplicationService).updateUser(any(UpdateUserCommand.class));

        // when & then
        mockMvc.perform(put("/api/v1/users/1")
                .header("Authorization", "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.username").value("新名字"));

        verify(userApplicationService).updateUser(any(UpdateUserCommand.class));
        verify(userApplicationService).getUserById(1L);
    }

    @Test
    @DisplayName("更新用户 - 用户不存在")
    void updateUser_notFound_shouldReturnError() throws Exception {
        // given
        UserUpdateRequest request = new UserUpdateRequest("新名字", "new@example.com", 1);

        doThrow(new DomainException("用户不存在"))
            .when(userApplicationService).updateUser(any(UpdateUserCommand.class));

        // when & then
        mockMvc.perform(put("/api/v1/users/999")
                .header("Authorization", "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").value("用户不存在"));
    }

    @Test
    @DisplayName("更新用户 - 启用账号")
    void updateUser_enableUser_shouldUpdateStatus() throws Exception {
        // given
        UserUpdateRequest request = new UserUpdateRequest(null, null, 1); // status=1表示启用
        User updatedUser = createTestUser(1L, "张三", "test@example.com", UserStatus.ENABLED);
        UserResponse response = new UserResponse(1L, "张三", "test@example.com", 1, null, null);

        when(userApplicationService.getUserById(1L)).thenReturn(updatedUser);
        when(userAssembler.toResponse(updatedUser)).thenReturn(response);
        doNothing().when(userApplicationService).updateUser(any(UpdateUserCommand.class));

        // when & then
        mockMvc.perform(put("/api/v1/users/1")
                .header("Authorization", "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value(1));
    }

    @Test
    @DisplayName("更新用户 - 禁用账号")
    void updateUser_disableUser_shouldUpdateStatus() throws Exception {
        // given
        UserUpdateRequest request = new UserUpdateRequest(null, null, 0); // status=0表示禁用
        User updatedUser = createTestUser(1L, "张三", "test@example.com", UserStatus.DISABLED);
        UserResponse response = new UserResponse(1L, "张三", "test@example.com", 0, null, null);

        when(userApplicationService.getUserById(1L)).thenReturn(updatedUser);
        when(userAssembler.toResponse(updatedUser)).thenReturn(response);
        doNothing().when(userApplicationService).updateUser(any(UpdateUserCommand.class));

        // when & then
        mockMvc.perform(put("/api/v1/users/1")
                .header("Authorization", "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value(0));
    }

    @Test
    @DisplayName("删除用户 - 成功")
    void deleteUser_success_shouldReturnSuccess() throws Exception {
        // given
        doNothing().when(userApplicationService).deleteUser(1L);

        // when & then
        mockMvc.perform(delete("/api/v1/users/1")
                .header("Authorization", "Bearer test-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        verify(userApplicationService).deleteUser(1L);
    }

    @Test
    @DisplayName("删除用户 - 用户不存在仍返回成功")
    void deleteUser_notExist_shouldStillReturnSuccess() throws Exception {
        // given - 删除不存在的用户也返回成功(幂等性)
        doNothing().when(userApplicationService).deleteUser(999L);

        // when & then
        mockMvc.perform(delete("/api/v1/users/999")
                .header("Authorization", "Bearer test-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        verify(userApplicationService).deleteUser(999L);
    }
}