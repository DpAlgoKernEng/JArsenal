package com.example.demo.service;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.RefreshToken;
import com.example.demo.entity.User;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.RefreshTokenMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.impl.AuthServiceImpl;
import com.example.demo.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AuthService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private RefreshTokenMapper refreshTokenMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setEmail("test@example.com");
        testUser.setStatus(1);

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("password123");
        registerRequest.setEmail("new@example.com");
    }

    // ========== Login Tests ==========

    @Test
    @DisplayName("登录 - 成功")
    void login_success_shouldReturnTokens() {
        // given
        when(userMapper.selectByUsername("testuser")).thenReturn(testUser);
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateAccessToken(1L, "testuser")).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(1L)).thenReturn("refresh-token");
        when(jwtUtil.getRefreshExpiration()).thenReturn(604800000L);
        when(refreshTokenMapper.insert(any(RefreshToken.class))).thenReturn(1);

        // when
        LoginResponse response = authService.login(loginRequest);

        // then
        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals(1L, response.getUserId());
        assertEquals("testuser", response.getUsername());

        verify(userMapper).selectByUsername("testuser");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(jwtUtil).generateAccessToken(1L, "testuser");
        verify(jwtUtil).generateRefreshToken(1L);
        verify(refreshTokenMapper).insert(any(RefreshToken.class));
    }

    @Test
    @DisplayName("登录 - 用户不存在")
    void login_userNotFound_shouldThrowException() {
        // given
        when(userMapper.selectByUsername("testuser")).thenReturn(null);

        // when & then
        BusinessException exception = assertThrows(BusinessException.class,
            () -> authService.login(loginRequest));

        assertEquals(401, exception.getCode());
        assertEquals("用户名或密码错误", exception.getMessage());

        verify(userMapper).selectByUsername("testuser");
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    @DisplayName("登录 - 密码错误")
    void login_wrongPassword_shouldThrowException() {
        // given
        when(userMapper.selectByUsername("testuser")).thenReturn(testUser);
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(false);

        // when & then
        BusinessException exception = assertThrows(BusinessException.class,
            () -> authService.login(loginRequest));

        assertEquals(401, exception.getCode());
        assertEquals("用户名或密码错误", exception.getMessage());

        verify(passwordEncoder).matches("password123", "encodedPassword");
    }

    @Test
    @DisplayName("登录 - 账号已禁用")
    void login_accountDisabled_shouldThrowException() {
        // given
        testUser.setStatus(0);
        when(userMapper.selectByUsername("testuser")).thenReturn(testUser);
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        // when & then
        BusinessException exception = assertThrows(BusinessException.class,
            () -> authService.login(loginRequest));

        assertEquals(403, exception.getCode());
        assertEquals("账号已被禁用", exception.getMessage());
    }

    // ========== Register Tests ==========

    @Test
    @DisplayName("注册 - 成功")
    void register_success_shouldCreateUser() {
        // given
        when(userMapper.selectByUsername("newuser")).thenReturn(null);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L);
            return 1;
        });

        // when
        authService.register(registerRequest);

        // then
        verify(userMapper).selectByUsername("newuser");
        verify(passwordEncoder).encode("password123");
        verify(userMapper).insert(any(User.class));
    }

    @Test
    @DisplayName("注册 - 用户名已存在")
    void register_usernameExists_shouldThrowException() {
        // given
        when(userMapper.selectByUsername("newuser")).thenReturn(testUser);

        // when & then
        BusinessException exception = assertThrows(BusinessException.class,
            () -> authService.register(registerRequest));

        assertEquals(400, exception.getCode());
        assertEquals("用户名已存在", exception.getMessage());

        verify(userMapper).selectByUsername("newuser");
        verify(passwordEncoder, never()).encode(any());
        verify(userMapper, never()).insert(any());
    }
}