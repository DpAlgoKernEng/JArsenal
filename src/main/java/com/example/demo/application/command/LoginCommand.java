package com.example.demo.application.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录命令
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginCommand {
    private String username;
    private String password;
}