package com.example.demo.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类 - 支持 Access Token 和 Refresh Token
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-expiration}")
    private Long accessExpiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";

    /**
     * 启动时校验密钥安全性
     * HS256 算法要求密钥长度 >= 256 bits (32 字符)
     */
    @PostConstruct
    public void validateSecretKey() {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException(
                "JWT Secret 密钥长度不足！HS256 算法要求密钥长度 >= 32 字符。" +
                "当前密钥长度: " + (secret != null ? secret.length() : 0) + "。" +
                "请通过 JWT_SECRET 环境变量设置强密钥，或检查 application.yml 配置。"
            );
        }
        log.info("JWT Secret 密钥校验通过，长度: {} 字符", secret.length());
    }

    /**
     * 获取签名密钥
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 Access Token (短期)
     */
    public String generateAccessToken(Long userId, String username) {
        return generateToken(userId, username, TOKEN_TYPE_ACCESS, accessExpiration);
    }

    /**
     * 生成 Refresh Token (长期)
     */
    public String generateRefreshToken(Long userId) {
        return generateToken(userId, null, TOKEN_TYPE_REFRESH, refreshExpiration);
    }

    /**
     * 生成 Token
     */
    private String generateToken(Long userId, String username, String tokenType, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        var builder = Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("type", tokenType)
                .issuedAt(now)
                .expiration(expiryDate);

        if (username != null) {
            builder.claim("username", username);
        }

        return builder.signWith(getSigningKey()).compact();
    }

    /**
     * 验证 Access Token 是否有效
     */
    public boolean validateAccessToken(String token) {
        return validateToken(token, TOKEN_TYPE_ACCESS);
    }

    /**
     * 验证 Refresh Token 是否有效
     */
    public boolean validateRefreshToken(String token) {
        return validateToken(token, TOKEN_TYPE_REFRESH);
    }

    /**
     * 验证 Token
     */
    private boolean validateToken(String token, String expectedType) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String tokenType = claims.get("type", String.class);
            if (!expectedType.equals(tokenType)) {
                log.warn("Token类型不匹配: expected={}, actual={}", expectedType, tokenType);
                return false;
            }
            return true;
        } catch (JwtException e) {
            log.warn("JWT验证失败: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.warn("JWT格式错误: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从 Token 中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 从 Token 中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("username", String.class);
    }

    /**
     * 获取 Access Token 过期时间 (毫秒)
     */
    public Long getAccessExpiration() {
        return accessExpiration;
    }

    /**
     * 获取 Refresh Token 过期时间 (毫秒)
     */
    public Long getRefreshExpiration() {
        return refreshExpiration;
    }
}