package com.example.demo.util;

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
        ReflectionTestUtils.setField(jwtUtil, "expiration", 3600000L); // 1小时
    }

    @Test
    @DisplayName("生成 Token - 成功")
    void generateToken_shouldSucceed() {
        // when
        String token = jwtUtil.generateToken(1L, "testuser");

        // then
        assertNotNull(token);
        assertTrue(token.length() > 0);
        assertTrue(token.split("\\.").length == 3); // JWT 格式: header.payload.signature
    }

    @Test
    @DisplayName("验证 Token - 有效 Token")
    void validateToken_validToken_shouldReturnTrue() {
        // given
        String token = jwtUtil.generateToken(1L, "testuser");

        // when
        boolean isValid = jwtUtil.validateToken(token);

        // then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("验证 Token - 无效 Token")
    void validateToken_invalidToken_shouldReturnFalse() {
        // given
        String invalidToken = "invalid.token.here";

        // when
        boolean isValid = jwtUtil.validateToken(invalidToken);

        // then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("验证 Token - 空 Token")
    void validateToken_emptyToken_shouldReturnFalse() {
        // when
        boolean isValid = jwtUtil.validateToken("");

        // then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("验证 Token - null Token")
    void validateToken_nullToken_shouldReturnFalse() {
        // when
        boolean isValid = jwtUtil.validateToken(null);

        // then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("从 Token 获取用户 ID")
    void getUserIdFromToken_shouldReturnCorrectId() {
        // given
        Long userId = 12345L;
        String token = jwtUtil.generateToken(userId, "testuser");

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
        String token = jwtUtil.generateToken(1L, username);

        // when
        String extractedUsername = jwtUtil.getUsernameFromToken(token);

        // then
        assertEquals(username, extractedUsername);
    }

    @Test
    @DisplayName("不同用户生成不同 Token")
    void generateToken_differentUsers_shouldGenerateDifferentTokens() {
        // when
        String token1 = jwtUtil.generateToken(1L, "user1");
        String token2 = jwtUtil.generateToken(2L, "user2");

        // then
        assertNotEquals(token1, token2);
    }
}