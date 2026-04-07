package com.example.demo.infrastructure.security.jwt;

import com.example.demo.domain.auth.service.TokenGenerator;
import com.example.demo.domain.auth.valueobject.AccessToken;
import com.example.demo.domain.user.valueobject.UserId;
import com.example.demo.domain.user.valueobject.Username;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * JWT Token 生成器实现
 */
@Component
public class JwtTokenGenerator implements TokenGenerator {

    private final SecretKey secretKey;
    private final long accessExpiration;
    private final long refreshExpiration;

    public JwtTokenGenerator(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long accessExpiration,
            @Value("${jwt.refresh-expiration:604800000}") long refreshExpiration) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    @Override
    public AccessToken generateAccessToken(UserId userId, Username username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessExpiration);

        String token = Jwts.builder()
                .subject(String.valueOf(userId.value()))
                .claim("type", "access")
                .claim("username", username.value())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();

        return new AccessToken(token, LocalDateTime.ofInstant(expiryDate.toInstant(), ZoneId.systemDefault()));
    }

    @Override
    public String generateRefreshToken(UserId userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpiration);

        return Jwts.builder()
                .subject(String.valueOf(userId.value()))
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    @Override
    public LocalDateTime accessTokenExpiration() {
        return LocalDateTime.now().plusSeconds(accessExpiration / 1000);
    }

    @Override
    public LocalDateTime refreshTokenExpiration() {
        return LocalDateTime.now().plusSeconds(refreshExpiration / 1000);
    }

    @Override
    public UserId parseAccessToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String type = claims.get("type", String.class);
        if (!"access".equals(type)) {
            throw new IllegalArgumentException("Invalid token type");
        }

        return new UserId(Long.parseLong(claims.getSubject()));
    }

    @Override
    public UserId parseRefreshToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String type = claims.get("type", String.class);
        if (!"refresh".equals(type)) {
            throw new IllegalArgumentException("Invalid token type");
        }

        return new UserId(Long.parseLong(claims.getSubject()));
    }
}