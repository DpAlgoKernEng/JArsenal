package com.example.demo.application.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 刷新 Token 命令
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenCommand {
    private String refreshToken;
}