-- 创建数据库
CREATE DATABASE IF NOT EXISTS demo DEFAULT CHARACTER SET utf8mb4;

USE demo;

-- 创建用户表
CREATE TABLE IF NOT EXISTS user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    email VARCHAR(100) COMMENT '邮箱',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 插入测试数据
INSERT INTO user (username, email, status) VALUES
('张三', 'zhangsan@example.com', 1),
('李四', 'lisi@example.com', 1),
('王五', 'wangwu@example.com', 0),
('赵六', 'zhaoliu@example.com', 1),
('钱七', 'qianqi@example.com', 1),
('孙八', 'sunba@example.com', 0),
('周九', 'zhoujiu@example.com', 1),
('吴十', 'wushi@example.com', 1),
('郑十一', 'zheng11@example.com', 1),
('王十二', 'wang12@example.com', 0),
('冯十三', 'feng13@example.com', 1),
('陈十四', 'chen14@example.com', 1),
('褚十五', 'chu15@example.com', 0),
('卫十六', 'wei16@example.com', 1),
('蒋十七', 'jiang17@example.com', 1);