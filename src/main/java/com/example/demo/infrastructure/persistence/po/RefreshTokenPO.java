package com.example.demo.infrastructure.persistence.po;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Refresh Token 持久化对象
 */
@Data
public class RefreshTokenPO {
    private Long id;
    private Long userId;
    private String token;
    private LocalDateTime expiresAt;
    private LocalDateTime createTime;  // 对应数据库 create_time
    private Integer revoked;  // 0-有效，1-已撤销
}