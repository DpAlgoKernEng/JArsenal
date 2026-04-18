package com.jguard.infrastructure.outbox;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 事件 Outbox 持久化对象
 * 用于 Outbox Pattern，保证事件发布与数据库事务的原子性
 */
@Data
public class OutboxMessage {
    private Long id;
    private String eventId;
    private String eventType;
    private String aggregateType;
    private String aggregateId;
    private String payload;
    private Integer status;  // 0-待发送，1-已发送，2-发送失败
    private Integer retryCount;
    private LocalDateTime createTime;  // 对应数据库 create_time
    private LocalDateTime sentTime;
    private String errorMsg;

    // 状态常量
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_SENT = 1;
    public static final int STATUS_FAILED = 2;
}