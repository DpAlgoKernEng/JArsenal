package com.example.demo.application.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建资源命令
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateResourceCommand {
    private String code;
    private String name;
    private Long parentId;
    private String type;
    private String path;
    private String pathPattern;
    private String method;
    private String icon;
    private String component;
    private Integer sort;
}