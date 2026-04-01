package com.example.demo.dto;

import lombok.Data;

/**
 * 分页请求参数
 */
@Data
public class PageRequest {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}