package com.jguard.infrastructure.persistence.po;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 审计日志持久化对象
 */
@Data
public class AuditLogPO {
    private Long id;
    private Long userId;
    private String username;
    private String operation;
    private String module;
    private String description;
    private Long targetId;
    private String ip;
    private String traceId;
    private Integer status;  // 1-成功，0-失败
    private String errorMsg;
    private Long duration;
    private LocalDateTime createTime;  // 对应数据库 create_time
}