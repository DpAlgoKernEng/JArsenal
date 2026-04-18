package com.jguard.application.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分配角色给用户命令
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignRoleCommand {
    private Long userId;
    private List<Long> roleIds;
}