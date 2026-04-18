package com.jguard.application.service;

import com.jguard.application.command.LoginCommand;
import com.jguard.application.command.RefreshTokenCommand;
import com.jguard.domain.auth.aggregate.Session;
import com.jguard.domain.auth.entity.RefreshToken;
import com.jguard.domain.auth.repository.TokenRepository;
import com.jguard.domain.auth.service.TokenGenerator;
import com.jguard.domain.auth.valueobject.AccessToken;
import com.jguard.domain.auth.valueobject.TokenPair;
import com.jguard.domain.shared.exception.DomainException;
import com.jguard.domain.user.aggregate.User;
import com.jguard.domain.user.repository.UserRepository;
import com.jguard.domain.user.valueobject.Email;
import com.jguard.domain.user.valueobject.EncryptedPassword;
import com.jguard.domain.user.valueobject.UserId;
import com.jguard.domain.user.valueobject.Username;
import com.jguard.domain.user.valueobject.UserStatus;
import com.jguard.infrastructure.outbox.OutboxDomainEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 认证应用服务测试
 */
@ExtendWith(MockitoExtension.class)
class AuthApplicationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private TokenGenerator tokenGenerator;

    @Mock
    private OutboxDomainEventPublisher eventPublisher;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthApplicationService authApplicationService;

    private User testUser;

    @BeforeEach
    void setUp() {
        authApplicationService = new AuthApplicationService(
            userRepository, tokenRepository, tokenGenerator, eventPublisher, passwordEncoder
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
    @DisplayName("登录成功 - 返回Token")
    void login_success_shouldReturnTokenPair() {
        // given
        LoginCommand command = new LoginCommand("testuser", "password123");
        AccessToken accessToken = mock(AccessToken.class);
        when(accessToken.token()).thenReturn("access-token");

        when(userRepository.findByUsername(any(Username.class))).thenReturn(testUser);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(tokenGenerator.generateAccessToken(any(), any())).thenReturn(accessToken);
        when(tokenGenerator.generateRefreshToken(any())).thenReturn("refresh-token");
        doNothing().when(tokenRepository).save(any(RefreshToken.class));
        doNothing().when(eventPublisher).setAggregateContext(anyString(), anyString());
        doNothing().when(eventPublisher).publishAll(anyList());
        doNothing().when(eventPublisher).clearAggregateContext();

        // when
        TokenPair result = authApplicationService.login(command);

        // then
        assertNotNull(result);
        assertEquals("access-token", result.accessToken().token());
        assertEquals("refresh-token", result.refreshToken());

        verify(userRepository).findByUsername(any(Username.class));
        verify(passwordEncoder).matches("password123", "$2a$10$hashedPassword");
        verify(tokenRepository).save(any(RefreshToken.class));
        verify(eventPublisher).publishAll(anyList());
    }

    @Test
    @DisplayName("登录失败 - 用户不存在")
    void login_userNotFound_shouldThrowException() {
        // given
        LoginCommand command = new LoginCommand("nonexistent", "password123");
        when(userRepository.findByUsername(any(Username.class))).thenReturn(null);

        // when & then
        assertThrows(DomainException.class, () -> authApplicationService.login(command));

        verify(passwordEncoder, never()).matches(any(), any());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("登录失败 - 密码错误")
    void login_wrongPassword_shouldThrowException() {
        // given
        LoginCommand command = new LoginCommand("testuser", "wrongpassword");
        when(userRepository.findByUsername(any(Username.class))).thenReturn(testUser);
        when(passwordEncoder.matches("wrongpassword", "$2a$10$hashedPassword")).thenReturn(false);

        // when & then
        assertThrows(DomainException.class, () -> authApplicationService.login(command));

        verify(tokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("登录失败 - 账号被禁用")
    void login_disabledUser_shouldThrowException() {
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

        LoginCommand command = new LoginCommand("testuser", "password123");
        when(userRepository.findByUsername(any(Username.class))).thenReturn(disabledUser);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        // when & then
        assertThrows(DomainException.class, () -> authApplicationService.login(command));
    }

    @Test
    @DisplayName("刷新Token成功")
    void refreshToken_success_shouldReturnNewTokenPair() {
        // given
        RefreshTokenCommand command = new RefreshTokenCommand("old-refresh-token");

        RefreshToken oldToken = mock(RefreshToken.class);
        when(oldToken.getUserId()).thenReturn(new UserId(1L));
        doNothing().when(oldToken).validate();

        AccessToken accessToken = mock(AccessToken.class);
        when(accessToken.token()).thenReturn("new-access-token");

        when(tokenRepository.findByToken("old-refresh-token")).thenReturn(oldToken);
        when(userRepository.findById(any(UserId.class))).thenReturn(testUser);
        when(tokenGenerator.generateAccessToken(any(), any())).thenReturn(accessToken);
        when(tokenGenerator.generateRefreshToken(any())).thenReturn("new-refresh-token");
        doNothing().when(tokenRepository).revoke(any(RefreshToken.class));
        doNothing().when(tokenRepository).save(any(RefreshToken.class));

        // when
        TokenPair result = authApplicationService.refreshToken(command);

        // then
        assertNotNull(result);
        assertEquals("new-access-token", result.accessToken().token());

        verify(tokenRepository).revoke(oldToken);
        verify(tokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("刷新Token失败 - Token不存在")
    void refreshToken_notFound_shouldThrowException() {
        // given
        RefreshTokenCommand command = new RefreshTokenCommand("invalid-token");
        when(tokenRepository.findByToken("invalid-token")).thenReturn(null);

        // when & then
        assertThrows(DomainException.class, () -> authApplicationService.refreshToken(command));

        verify(userRepository, never()).findById(any());
    }

    @Test
    @DisplayName("刷新Token失败 - Token已过期")
    void refreshToken_expired_shouldThrowException() {
        // given
        RefreshTokenCommand command = new RefreshTokenCommand("expired-token");

        RefreshToken expiredToken = mock(RefreshToken.class);
        doThrow(new DomainException("Token已过期")).when(expiredToken).validate();

        when(tokenRepository.findByToken("expired-token")).thenReturn(expiredToken);

        // when & then
        assertThrows(DomainException.class, () -> authApplicationService.refreshToken(command));
    }

    @Test
    @DisplayName("刷新Token失败 - 用户不存在")
    void refreshToken_userNotFound_shouldThrowException() {
        // given
        RefreshTokenCommand command = new RefreshTokenCommand("valid-token");

        RefreshToken validToken = mock(RefreshToken.class);
        when(validToken.getUserId()).thenReturn(new UserId(999L));
        doNothing().when(validToken).validate();

        when(tokenRepository.findByToken("valid-token")).thenReturn(validToken);
        when(userRepository.findById(new UserId(999L))).thenReturn(null);

        // when & then
        assertThrows(DomainException.class, () -> authApplicationService.refreshToken(command));
    }

    @Test
    @DisplayName("登出成功 - 撤销Token")
    void logout_success_shouldRevokeToken() {
        // given
        RefreshToken token = mock(RefreshToken.class);
        when(tokenRepository.findByToken("refresh-token")).thenReturn(token);
        doNothing().when(tokenRepository).revoke(token);

        // when
        authApplicationService.logout("refresh-token");

        // then
        verify(tokenRepository).findByToken("refresh-token");
        verify(tokenRepository).revoke(token);
    }

    @Test
    @DisplayName("登出 - Token不存在时不报错")
    void logout_tokenNotFound_shouldNotThrow() {
        // given
        when(tokenRepository.findByToken("nonexistent-token")).thenReturn(null);

        // when
        authApplicationService.logout("nonexistent-token");

        // then
        verify(tokenRepository).findByToken("nonexistent-token");
        verify(tokenRepository, never()).revoke(any());
    }

    @Test
    @DisplayName("登出 - null Token不报错")
    void logout_nullToken_shouldNotThrow() {
        // when
        authApplicationService.logout(null);

        // then
        verify(tokenRepository, never()).findByToken(any());
        verify(tokenRepository, never()).revoke(any());
    }
}