package com.example.demo.interfaces.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 敏感字段响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SensitiveFieldResponse {
    private Long id;
    private String fieldCode;
    private String fieldName;
    private String sensitiveLevel;
    private String maskPattern;
}