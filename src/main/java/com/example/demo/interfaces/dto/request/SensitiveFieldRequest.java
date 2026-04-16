package com.example.demo.interfaces.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 敏感字段请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SensitiveFieldRequest {
    @NotBlank(message = "字段编码不能为空")
    @Pattern(regexp = "^[a-z][a-zA-Z0-9_]{0,49}$", message = "字段编码格式错误：小写字母开头，仅含字母数字下划线")
    private String fieldCode;

    @NotBlank(message = "字段名称不能为空")
    @Size(min = 2, max = 100, message = "字段名称长度必须在2-100字符之间")
    private String fieldName;

    @NotBlank(message = "敏感级别不能为空")
    @Pattern(regexp = "^(NORMAL|HIDDEN|ENCRYPTED)$", message = "敏感级别必须是 NORMAL、HIDDEN 或 ENCRYPTED")
    private String sensitiveLevel;

    private String maskPattern;
}