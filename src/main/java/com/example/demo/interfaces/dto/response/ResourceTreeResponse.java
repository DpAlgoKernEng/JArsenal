package com.example.demo.interfaces.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 资源树响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourceTreeResponse {
    private Long id;
    private String code;
    private String name;
    private String type;
    private Long parentId;
    private String path;
    private String pathPattern;
    private String method;
    private String icon;
    private String component;
    private int sort;
    private boolean status;
    private List<ResourceTreeResponse> children;
}