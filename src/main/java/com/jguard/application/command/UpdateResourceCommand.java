package com.jguard.application.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新资源命令
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateResourceCommand {
    private Long resourceId;
    private String name;
    private Long parentId;
    private String path;
    private String pathPattern;
    private String method;
    private String icon;
    private String component;
    private String status;
    private Integer sort;
    private String dataDimensionCode;  // 数据维度编码
}