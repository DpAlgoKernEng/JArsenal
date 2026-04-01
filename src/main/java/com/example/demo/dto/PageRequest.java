package com.example.demo.dto;

import lombok.Data;

/**
 * 分页请求参数
 */
@Data
public class PageRequest {

    /**
     * 最大每页条数限制
     */
    public static final int MAX_PAGE_SIZE = 100;

    private Integer pageNum = 1;
    private Integer pageSize = 10;

    /**
     * 设置每页条数，自动限制上限
     */
    public void setPageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            this.pageSize = 10;
        } else if (pageSize > MAX_PAGE_SIZE) {
            this.pageSize = MAX_PAGE_SIZE;
        } else {
            this.pageSize = pageSize;
        }
    }
}