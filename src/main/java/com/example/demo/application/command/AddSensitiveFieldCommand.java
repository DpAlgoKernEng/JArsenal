package com.example.demo.application.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 添加敏感字段命令
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddSensitiveFieldCommand {
    private Long resourceId;
    private String fieldCode;
    private String fieldName;
    private String sensitiveLevel;
    private String maskPattern;
}