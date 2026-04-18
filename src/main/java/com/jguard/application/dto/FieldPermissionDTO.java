package com.jguard.application.dto;

/**
 * 字段权限 DTO
 * 用于前端字段权限控制
 */
public record FieldPermissionDTO(
    String resourceCode,
    String fieldCode,
    boolean canView,
    boolean canEdit
) {}