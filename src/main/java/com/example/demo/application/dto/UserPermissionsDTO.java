package com.example.demo.application.dto;

import java.util.List;

/**
 * 用户权限 DTO
 * 包含菜单、操作权限、字段权限和版本号
 */
public record UserPermissionsDTO(
    List<MenuDTO> menus,
    List<ActionPermissionDTO> actions,
    List<FieldPermissionDTO> fields,
    long version
) {}