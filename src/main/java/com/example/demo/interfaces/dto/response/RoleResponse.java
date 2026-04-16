package com.example.demo.interfaces.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {
    private Long id;
    private String code;
    private String name;
    private Long parentId;
    private String status;
    private String inheritMode;
    private boolean builtin;
    private int sort;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /**
     * 子角色列表（用于树结构）
     */
    private List<RoleResponse> children;
}