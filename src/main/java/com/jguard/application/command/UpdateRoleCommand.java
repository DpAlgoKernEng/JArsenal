package com.jguard.application.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新角色命令
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoleCommand {
    private Long roleId;
    private String name;
    private Long parentId;
    private String inheritMode;
    private String status;
    private Integer sort;
}