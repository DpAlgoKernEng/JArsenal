package com.example.demo.interfaces.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分配权限请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignPermissionRequest {
    @NotNull(message = "资源ID不能为空")
    @Positive(message = "资源ID必须为正数")
    private Long resourceId;

    @NotNull(message = "操作列表不能为空")
    private List<String> actions;

    @NotNull(message = "权限效果不能为空")
    private String effect;
}