package com.jguard.interfaces.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色更新请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleUpdateRequest {
    @Size(min = 2, max = 50, message = "角色名称长度必须在2-50字符之间")
    private String name;

    private Long parentId;

    private String inheritMode;

    private String status;

    @Min(value = 0, message = "排序值必须大于等于0")
    private Integer sort;
}