package com.example.demo.security;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 用户上下文信息
 */
@Data
@AllArgsConstructor
public class UserInfo {
    private Long userId;
    private String username;
}