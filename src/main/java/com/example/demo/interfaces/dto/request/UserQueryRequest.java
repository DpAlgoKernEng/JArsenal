package com.example.demo.interfaces.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户查询请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserQueryRequest {
    private String username;
    private Integer status;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}