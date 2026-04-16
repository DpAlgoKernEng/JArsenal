package com.example.demo.application.dto;

import java.util.List;

/**
 * 操作权限 DTO
 * 用于前端按钮权限控制
 */
public record ActionPermissionDTO(
    String resourceCode,
    List<String> actions
) {}