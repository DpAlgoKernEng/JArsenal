package com.jguard.interfaces.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色创建请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleCreateRequest {
    @NotNull(message = "角色编码不能为空")
    @Pattern(regexp = "^[A-Z][A-Z0-9_]{1,49}$", message = "角色编码格式错误：需2-50字符，大写字母开头，仅含字母数字下划线")
    private String code;

    @NotNull(message = "角色名称不能为空")
    @Size(min = 2, max = 50, message = "角色名称长度必须在2-50字符之间")
    private String name;

    private Long parentId;

    private String inheritMode;

    @Min(value = 0, message = "排序值必须大于等于0")
    private Integer sort = 0;
}