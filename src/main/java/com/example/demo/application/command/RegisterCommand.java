package com.example.demo.application.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注册命令
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterCommand {
    private String username;
    private String password;
    private String email;
}