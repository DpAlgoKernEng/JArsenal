package com.example.demo.interfaces.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 资源创建请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourceCreateRequest {
    @NotBlank(message = "资源编码不能为空")
    @Pattern(regexp = "^[A-Z][A-Z0-9_]{1,49}$", message = "资源编码格式错误：需2-50字符，大写字母开头，仅含字母数字下划线")
    private String code;

    @NotBlank(message = "资源名称不能为空")
    @Size(min = 2, max = 100, message = "资源名称长度必须在2-100字符之间")
    private String name;

    private Long parentId;

    @NotBlank(message = "资源类型不能为空")
    @Pattern(regexp = "^(MENU|OPERATION|API)$", message = "资源类型必须是 MENU、OPERATION 或 API")
    private String type;

    private String path;

    private String pathPattern;

    private String method;

    private String icon;

    private String component;

    @Min(value = 0, message = "排序值必须大于等于0")
    @Max(value = 999, message = "排序值必须小于等于999")
    private Integer sort = 0;
}