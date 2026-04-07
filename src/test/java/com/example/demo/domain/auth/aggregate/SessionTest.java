package com.example.demo.domain.auth.aggregate;

import com.example.demo.domain.auth.service.TokenGenerator;
import com.example.demo.domain.auth.valueobject.AccessToken;
import com.example.demo.domain.user.valueobject.UserId;
import com.example.demo.domain.user.valueobject.Username;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Session 聚合根单元测试
 */
class SessionTest {

    private TokenGenerator tokenGenerator;

    @BeforeEach
    void setUp() {
        tokenGenerator = mock(TokenGenerator.class);
        when(tokenGenerator.generateAccessToken(any(), any()))
            .thenReturn(new AccessToken("access-token", LocalDateTime.now().plusHours(1)));
        when(tokenGenerator.generateRefreshToken(any()))
            .thenReturn("refresh-token");
        when(tokenGenerator.refreshTokenExpiration())
            .thenReturn(LocalDateTime.now().plusDays(7));
    }

    @Test
    @DisplayName("创建会话应该生成 Access Token 和 Refresh Token")
    void create_shouldGenerateTokens() {
        // given
        UserId userId = new UserId(123L);
        Username username = new Username("testuser");

        // when
        Session session = Session.create(userId, username, tokenGenerator);

        // then
        assertNotNull(session.getId());
        assertEquals(userId, session.getUserId());
        assertEquals(username, session.getUsername());
        assertNotNull(session.getAccessToken());
        assertNotNull(session.getRefreshToken());
    }

    @Test
    @DisplayName("创建会话应该生成 UserLoggedIn 事件")
    void create_shouldGenerateLoggedInEvent() {
        // given
        UserId userId = new UserId(456L);
        Username username = new Username("testuser");

        // when
        Session session = Session.create(userId, username, tokenGenerator);

        // then
        assertEquals(1, session.pendingEvents().size());
        assertTrue(session.pendingEvents().get(0) instanceof com.example.demo.domain.auth.event.UserLoggedIn);
    }

    @Test
    @DisplayName("登出应该撤销 Refresh Token")
    void logout_shouldRevokeToken() {
        // given
        UserId userId = new UserId(789L);
        Username username = new Username("testuser");
        Session session = Session.create(userId, username, tokenGenerator);

        // when
        session.logout();

        // then
        assertTrue(session.getRefreshToken().isRevoked());
    }

    @Test
    @DisplayName("登出应该生成 UserLoggedOut 事件")
    void logout_shouldGenerateLoggedOutEvent() {
        // given
        UserId userId = new UserId(100L);
        Username username = new Username("testuser");
        Session session = Session.create(userId, username, tokenGenerator);
        session.clearPendingEvents();

        // when
        session.logout();

        // then
        assertEquals(1, session.pendingEvents().size());
        assertTrue(session.pendingEvents().get(0) instanceof com.example.demo.domain.auth.event.UserLoggedOut);
    }
}