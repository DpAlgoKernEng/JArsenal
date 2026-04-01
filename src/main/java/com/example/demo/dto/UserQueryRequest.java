package com.example.demo.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserQueryRequest extends PageRequest {
    private String username;
    private Integer status;
}