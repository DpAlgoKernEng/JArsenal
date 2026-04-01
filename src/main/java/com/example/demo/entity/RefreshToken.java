package com.example.demo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Refresh Token 实体
 */
@Data
public class RefreshToken {

    private Long id;
    private Long userId;
    private String token;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private Integer revoked;  // 0-有效, 1-已撤销
}