package com.example.demo.application.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建角色命令
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoleCommand {
    private String code;
    private String name;
    private Long parentId;
    private String inheritMode;
    private Integer sort;
}