package com.jguard.interfaces.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分配角色请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignRoleRequest {
    @NotNull(message = "角色ID列表不能为空")
    @Size(min = 1, message = "至少需要分配一个角色")
    private List<@Positive(message = "角色ID必须为正数") Long> roleIds;
}