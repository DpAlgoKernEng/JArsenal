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