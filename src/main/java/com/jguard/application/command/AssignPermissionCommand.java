package com.jguard.application.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分配权限命令
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignPermissionCommand {
    private Long roleId;
    private Long resourceId;
    private List<String> actions;
    private String effect;
}