package com.jguard.application.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新用户命令
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserCommand {
    private Long userId;
    private String username;
    private String email;
    private Integer status;
}