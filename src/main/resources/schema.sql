-- 创建数据库
CREATE DATABASE IF NOT EXISTS demo DEFAULT CHARACTER SET utf8mb4;

USE demo;

-- 创建用户表
CREATE TABLE IF NOT EXISTS user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    email VARCHAR(100) COMMENT '邮箱',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 创建 Refresh Token 表
CREATE TABLE IF NOT EXISTS refresh_token (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    token VARCHAR(500) NOT NULL COMMENT 'Refresh Token',
    expires_at DATETIME NOT NULL COMMENT '过期时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    revoked TINYINT DEFAULT 0 COMMENT '是否已撤销：0-有效，1-已撤销',
    INDEX idx_user_id (user_id),
    INDEX idx_token (token),
    INDEX idx_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Refresh Token表';

-- 创建审计日志表
CREATE TABLE IF NOT EXISTS audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT COMMENT '操作用户ID',
    username VARCHAR(50) COMMENT '操作用户名',
    operation VARCHAR(50) NOT NULL COMMENT '操作类型：LOGIN, REGISTER, CREATE, UPDATE, DELETE等',
    module VARCHAR(50) NOT NULL COMMENT '模块：AUTH, USER等',
    description VARCHAR(500) COMMENT '操作描述',
    target_id BIGINT COMMENT '操作目标ID',
    ip VARCHAR(50) COMMENT '客户端IP',
    trace_id VARCHAR(50) COMMENT '链路追踪ID',
    status TINYINT DEFAULT 1 COMMENT '状态：1-成功，0-失败',
    error_msg VARCHAR(500) COMMENT '错误信息',
    duration BIGINT COMMENT '操作耗时(ms)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_operation (operation),
    INDEX idx_module (module),
    INDEX idx_create_time (create_time),
    INDEX idx_trace_id (trace_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志表';

-- 创建事件 Outbox 表 (用于可靠的事件发布)
CREATE TABLE IF NOT EXISTS event_outbox (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    event_id VARCHAR(50) NOT NULL COMMENT '事件唯一标识',
    event_type VARCHAR(50) NOT NULL COMMENT '事件类型',
    payload TEXT NOT NULL COMMENT '事件内容(JSON)',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING-待发送, SENT-已发送, FAILED-失败',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    error_message TEXT COMMENT '错误信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    sent_time DATETIME COMMENT '发送时间',
    INDEX idx_status (status),
    INDEX idx_event_id (event_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='事件Outbox表';

-- 插入测试数据 (密码使用BCrypt加密，明文均为123456)
INSERT INTO user (username, password, email, status) VALUES
('张三', '$2a$10$3c61Gjb1U1EjuM8Zuy/pDOHH61ekT2thzyGqid/D1p2mmRKQniRcC', 'zhangsan@example.com', 1),
('李四', '$2a$10$3c61Gjb1U1EjuM8Zuy/pDOHH61ekT2thzyGqid/D1p2mmRKQniRcC', 'lisi@example.com', 1),
('王五', '$2a$10$3c61Gjb1U1EjuM8Zuy/pDOHH61ekT2thzyGqid/D1p2mmRKQniRcC', 'wangwu@example.com', 0),
('赵六', '$2a$10$3c61Gjb1U1EjuM8Zuy/pDOHH61ekT2thzyGqid/D1p2mmRKQniRcC', 'zhaoliu@example.com', 1),
('钱七', '$2a$10$3c61Gjb1U1EjuM8Zuy/pDOHH61ekT2thzyGqid/D1p2mmRKQniRcC', 'qianqi@example.com', 1),
('孙八', '$2a$10$3c61Gjb1U1EjuM8Zuy/pDOHH61ekT2thzyGqid/D1p2mmRKQniRcC', 'sunba@example.com', 0),
('周九', '$2a$10$3c61Gjb1U1EjuM8Zuy/pDOHH61ekT2thzyGqid/D1p2mmRKQniRcC', 'zhoujiu@example.com', 1),
('吴十', '$2a$10$3c61Gjb1U1EjuM8Zuy/pDOHH61ekT2thzyGqid/D1p2mmRKQniRcC', 'wushi@example.com', 1),
('郑十一', '$2a$10$3c61Gjb1U1EjuM8Zuy/pDOHH61ekT2thzyGqid/D1p2mmRKQniRcC', 'zheng11@example.com', 1),
('王十二', '$2a$10$3c61Gjb1U1EjuM8Zuy/pDOHH61ekT2thzyGqid/D1p2mmRKQniRcC', 'wang12@example.com', 0),
('冯十三', '$2a$10$3c61Gjb1U1EjuM8Zuy/pDOHH61ekT2thzyGqid/D1p2mmRKQniRcC', 'feng13@example.com', 1),
('陈十四', '$2a$10$3c61Gjb1U1EjuM8Zuy/pDOHH61ekT2thzyGqid/D1p2mmRKQniRcC', 'chen14@example.com', 1),
('褚十五', '$2a$10$3c61Gjb1U1EjuM8Zuy/pDOHH61ekT2thzyGqid/D1p2mmRKQniRcC', 'chu15@example.com', 0),
('卫十六', '$2a$10$3c61Gjb1U1EjuM8Zuy/pDOHH61ekT2thzyGqid/D1p2mmRKQniRcC', 'wei16@example.com', 1),
('蒋十七', '$2a$10$3c61Gjb1U1EjuM8Zuy/pDOHH61ekT2thzyGqid/D1p2mmRKQniRcC', 'jiang17@example.com', 1);