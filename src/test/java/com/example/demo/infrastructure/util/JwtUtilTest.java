package com.example.demo.infrastructure.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtUtil 单元测试
 */
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // 设置测试用的 secret 和 expiration
        ReflectionTestUtils.setField(jwtUtil, "secret",
            "test-secret-key-at-least-256-bits-long-for-hs256-algorithm");
        ReflectionTestUtils.setField(jwtUtil, "accessExpiration", 1800000L); // 30分钟
        ReflectionTestUtils.setField(jwtUtil, "refreshExpiration", 604800000L); // 7天
    }

    @Test
    @DisplayName("生成 Access Token - 成功")
    void generateAccessToken_shouldSucceed() {
        // when
        String token = jwtUtil.generateAccessToken(1L, "testuser");

        // then
        assertNotNull(token);
        assertTrue(token.length() > 0);
        assertTrue(token.split("\\.").length == 3); // JWT 格式: header.payload.signature
    }

    @Test
    @DisplayName("生成 Refresh Token - 成功")
    void generateRefreshToken_shouldSucceed() {
        // when
        String token = jwtUtil.generateRefreshToken(1L);

        // then
        assertNotNull(token);
        assertTrue(token.length() > 0);
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    @DisplayName("验证 Access Token - 有效 Token")
    void validateAccessToken_validToken_shouldReturnTrue() {
        // given
        String token = jwtUtil.generateAccessToken(1L, "testuser");

        // when
        boolean isValid = jwtUtil.validateAccessToken(token);

        // then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("验证 Refresh Token - 有效 Token")
    void validateRefreshToken_validToken_shouldReturnTrue() {
        // given
        String token = jwtUtil.generateRefreshToken(1L);

        // when
        boolean isValid = jwtUtil.validateRefreshToken(token);

        // then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("验证 Token - 无效 Token")
    void validateToken_invalidToken_shouldReturnFalse() {
        // given
        String invalidToken = "invalid.token.here";

        // when & then
        assertFalse(jwtUtil.validateAccessToken(invalidToken));
        assertFalse(jwtUtil.validateRefreshToken(invalidToken));
    }

    @Test
    @DisplayName("验证 Token - 空 Token")
    void validateToken_emptyToken_shouldReturnFalse() {
        // when & then
        assertFalse(jwtUtil.validateAccessToken(""));
        assertFalse(jwtUtil.validateRefreshToken(""));
    }

    @Test
    @DisplayName("验证 Token - null Token")
    void validateToken_nullToken_shouldReturnFalse() {
        // when & then
        assertFalse(jwtUtil.validateAccessToken(null));
        assertFalse(jwtUtil.validateRefreshToken(null));
    }

    @Test
    @DisplayName("Access Token 不能通过 Refresh Token 验证")
    void validateAccessToken_withRefreshToken_shouldReturnFalse() {
        // given
        String refreshToken = jwtUtil.generateRefreshToken(1L);

        // when
        boolean isValid = jwtUtil.validateAccessToken(refreshToken);

        // then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Refresh Token 不能通过 Access Token 验证")
    void validateRefreshToken_withAccessToken_shouldReturnFalse() {
        // given
        String accessToken = jwtUtil.generateAccessToken(1L, "testuser");

        // when
        boolean isValid = jwtUtil.validateRefreshToken(accessToken);

        // then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("从 Token 获取用户 ID")
    void getUserIdFromToken_shouldReturnCorrectId() {
        // given
        Long userId = 12345L;
        String token = jwtUtil.generateAccessToken(userId, "testuser");

        // when
        Long extractedId = jwtUtil.getUserIdFromToken(token);

        // then
        assertEquals(userId, extractedId);
    }

    @Test
    @DisplayName("从 Token 获取用户名")
    void getUsernameFromToken_shouldReturnCorrectUsername() {
        // given
        String username = "testuser";
        String token = jwtUtil.generateAccessToken(1L, username);

        // when
        String extractedUsername = jwtUtil.getUsernameFromToken(token);

        // then
        assertEquals(username, extractedUsername);
    }

    @Test
    @DisplayName("不同用户生成不同 Token")
    void generateToken_differentUsers_shouldGenerateDifferentTokens() {
        // when
        String token1 = jwtUtil.generateAccessToken(1L, "user1");
        String token2 = jwtUtil.generateAccessToken(2L, "user2");

        // then
        assertNotEquals(token1, token2);
    }
}