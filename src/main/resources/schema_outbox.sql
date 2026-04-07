-- 事件 Outbox 表 (Outbox Pattern)
CREATE TABLE IF NOT EXISTS event_outbox (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    event_id VARCHAR(50) NOT NULL COMMENT '事件唯一ID',
    event_type VARCHAR(100) NOT NULL COMMENT '事件类型',
    aggregate_type VARCHAR(50) NOT NULL COMMENT '聚合类型',
    aggregate_id VARCHAR(50) NOT NULL COMMENT '聚合ID',
    payload TEXT NOT NULL COMMENT '事件载荷(JSON)',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待发送，1-已发送，2-发送失败',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    sent_at DATETIME COMMENT '发送时间',
    error_msg VARCHAR(500) COMMENT '错误信息',
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_event_type (event_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='事件Outbox表';