package com.example.demo.interfaces.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 资源响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourceResponse {
    private Long id;
    private String code;
    private String name;
    private Long parentId;
    private String type;
    private String path;
    private String pathPattern;
    private String method;
    private String icon;
    private String component;
    private int sort;
    private boolean status;
    private List<SensitiveFieldResponse> sensitiveFields;

    /**
     * 子资源列表（用于树结构）
     */
    private List<ResourceResponse> children;
}