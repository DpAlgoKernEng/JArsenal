package com.example.demo.infrastructure.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PasswordEncoder 配置测试")
class PasswordEncoderConfigTest {

    @Test
    @DisplayName("BCrypt strength 应为 12")
    void passwordEncoder_shouldUseStrength12() {
        PasswordEncoderConfig config = new PasswordEncoderConfig();
        BCryptPasswordEncoder encoder = (BCryptPasswordEncoder) config.passwordEncoder();

        // BCrypt 默认 strength=10，我们要求 12
        // 验证编码后的密码格式（$2a$12$...）
        String encoded = encoder.encode("test");
        assertTrue(encoded.startsWith("$2a$12$") || encoded.startsWith("$2b$12$"),
            "BCrypt strength should be 12, got: " + encoded.substring(0, 7));
    }
}