package com.jguard.interfaces.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 资源更新请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourceUpdateRequest {
    @Size(min = 2, max = 100, message = "资源名称长度必须在2-100字符之间")
    private String name;

    private Long parentId;

    private String path;

    private String pathPattern;

    private String method;

    private String icon;

    private String component;

    private String status;

    @Min(value = 0, message = "排序值必须大于等于0")
    @Max(value = 999, message = "排序值必须小于等于999")
    private Integer sort;

    private String dataDimensionCode;  // 数据维度编码
}