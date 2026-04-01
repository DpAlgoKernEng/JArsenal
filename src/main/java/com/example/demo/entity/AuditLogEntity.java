package com.example.demo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 审计日志实体
 */
@Data
public class AuditLogEntity {

    private Long id;
    private Long userId;
    private String username;
    private String operation;
    private String module;
    private String description;
    private Long targetId;
    private String ip;
    private String traceId;
    private Integer status;  // 1-成功, 0-失败
    private String errorMsg;
    private Long duration;
    private LocalDateTime createdAt;
}