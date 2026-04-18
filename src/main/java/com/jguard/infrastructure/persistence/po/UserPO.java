package com.jguard.infrastructure.persistence.po;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户持久化对象
 * 与数据库表结构一一对应
 */
@Data
public class UserPO {
    private Long id;
    private String username;
    private String password;
    private String email;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}