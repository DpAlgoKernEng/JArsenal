package com.example.demo.interfaces.controller;

import com.example.demo.application.command.LoginCommand;
import com.example.demo.application.command.RefreshTokenCommand;
import com.example.demo.application.service.AuthApplicationService;
import com.example.demo.domain.auth.valueobject.AccessToken;
import com.example.demo.domain.auth.valueobject.TokenPair;
import com.example.demo.interfaces.assembler.AuthAssembler;
import com.example.demo.interfaces.dto.request.LoginRequest;
import com.example.demo.interfaces.dto.request.RefreshTokenRequest;
import com.example.demo.interfaces.dto.request.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 认证控制器测试
 */
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthApplicationService authApplicationService;

    @MockBean
    private AuthAssembler authAssembler;

    @Test
    @DisplayName("登录成功 - 返回Token")
    void login_success_shouldReturnTokens() throws Exception {
        // given
        LoginRequest request = new LoginRequest("testuser", "password123");
        AccessToken accessToken = mock(AccessToken.class);
        when(accessToken.token()).thenReturn("access-token-value");
        when(accessToken.remainingTimeMillis()).thenReturn(1800000L);

        TokenPair tokenPair = new TokenPair(accessToken, "refresh-token-value");

        when(authApplicationService.login(any(LoginCommand.class))).thenReturn(tokenPair);

        // when & then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.accessToken").value("access-token-value"))
            .andExpect(jsonPath("$.data.refreshToken").value("refresh-token-value"));

        verify(authApplicationService).login(any(LoginCommand.class));
    }

    @Test
    @DisplayName("登录失败 - 用户名为空")
    void login_emptyUsername_shouldReturnBadRequest() throws Exception {
        // given
        LoginRequest request = new LoginRequest("", "password123");

        // when & then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(400));

        verify(authApplicationService, never()).login(any());
    }

    @Test
    @DisplayName("登录失败 - 密码过短")
    void login_shortPassword_shouldReturnBadRequest() throws Exception {
        // given
        LoginRequest request = new LoginRequest("testuser", "12345"); // 少于6位

        // when & then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(400));

        verify(authApplicationService, never()).login(any());
    }

    @Test
    @DisplayName("刷新Token成功")
    void refreshToken_success_shouldReturnNewTokens() throws Exception {
        // given
        RefreshTokenRequest request = new RefreshTokenRequest("old-refresh-token");
        AccessToken accessToken = mock(AccessToken.class);
        when(accessToken.token()).thenReturn("new-access-token");
        when(accessToken.remainingTimeMillis()).thenReturn(1800000L);

        TokenPair tokenPair = new TokenPair(accessToken, "new-refresh-token");

        when(authApplicationService.refreshToken(any(RefreshTokenCommand.class))).thenReturn(tokenPair);

        // when & then
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.accessToken").value("new-access-token"));

        verify(authApplicationService).refreshToken(any(RefreshTokenCommand.class));
    }

    @Test
    @DisplayName("注册成功")
    void register_success_shouldReturnSuccess() throws Exception {
        // given
        RegisterRequest request = new RegisterRequest("newuser", "password123", "test@example.com");

        // when & then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("注册失败 - 邮箱格式错误")
    void register_invalidEmail_shouldReturnBadRequest() throws Exception {
        // given
        RegisterRequest request = new RegisterRequest("newuser", "password123", "invalid-email");

        // when & then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("登出成功")
    void logout_success_shouldReturnSuccess() throws Exception {
        // given
        RefreshTokenRequest request = new RefreshTokenRequest("refresh-token");
        doNothing().when(authApplicationService).logout("refresh-token");

        // when & then
        mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        verify(authApplicationService).logout("refresh-token");
    }

    @Test
    @DisplayName("登出 - 无Token时也返回成功")
    void logout_noToken_shouldReturnSuccess() throws Exception {
        // when & then
        mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        verify(authApplicationService, never()).logout(any());
    }
}