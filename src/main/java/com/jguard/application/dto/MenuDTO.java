package com.jguard.application.dto;

import java.util.List;

/**
 * 菜单 DTO
 * 用于前端动态路由生成
 */
public record MenuDTO(
    Long id,
    String code,
    String name,
    String path,
    String icon,
    String component,
    Integer sort,
    Long parentId,
    List<MenuDTO> children
) {}