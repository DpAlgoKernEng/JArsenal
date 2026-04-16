-- =====================================================
-- V0_1__Core_Test_Data.sql
-- 测试用户数据 (仅用于开发/测试环境)
-- =====================================================

-- -----------------------------------------------------
-- 测试用户数据
-- 密码使用 BCrypt 加密，明文均为 "123456"
-- 注意：生产环境必须删除此脚本或使用唯一密码
-- -----------------------------------------------------
INSERT INTO user (username, password, email, status) VALUES
('张三', '$2a$10$y8zNMypQB94BZF4SMQNYEOt2L3nTqrSoQ8KoE559SAVDcNgJKcxxS', 'zhangsan@example.com', 1),
('李四', '$2a$10$y8zNMypQB94BZF4SMQNYEOt2L3nTqrSoQ8KoE559SAVDcNgJKcxxS', 'lisi@example.com', 1),
('王五', '$2a$10$y8zNMypQB94BZF4SMQNYEOt2L3nTqrSoQ8KoE559SAVDcNgJKcxxS', 'wangwu@example.com', 0),
('赵六', '$2a$10$y8zNMypQB94BZF4SMQNYEOt2L3nTqrSoQ8KoE559SAVDcNgJKcxxS', 'zhaoliu@example.com', 1),
('钱七', '$2a$10$y8zNMypQB94BZF4SMQNYEOt2L3nTqrSoQ8KoE559SAVDcNgJKcxxS', 'qianqi@example.com', 1),
('孙八', '$2a$10$y8zNMypQB94BZF4SMQNYEOt2L3nTqrSoQ8KoE559SAVDcNgJKcxxS', 'sunba@example.com', 0),
('周九', '$2a$10$y8zNMypQB94BZF4SMQNYEOt2L3nTqrSoQ8KoE559SAVDcNgJKcxxS', 'zhoujiu@example.com', 1),
('吴十', '$2a$10$y8zNMypQB94BZF4SMQNYEOt2L3nTqrSoQ8KoE559SAVDcNgJKcxxS', 'wushi@example.com', 1),
('郑十一', '$2a$10$y8zNMypQB94BZF4SMQNYEOt2L3nTqrSoQ8KoE559SAVDcNgJKcxxS', 'zheng11@example.com', 1),
('王十二', '$2a$10$y8zNMypQB94BZF4SMQNYEOt2L3nTqrSoQ8KoE559SAVDcNgJKcxxS', 'wang12@example.com', 0),
('冯十三', '$2a$10$y8zNMypQB94BZF4SMQNYEOt2L3nTqrSoQ8KoE559SAVDcNgJKcxxS', 'feng13@example.com', 1),
('陈十四', '$2a$10$y8zNMypQB94BZF4SMQNYEOt2L3nTqrSoQ8KoE559SAVDcNgJKcxxS', 'chen14@example.com', 1),
('褚十五', '$2a$10$y8zNMypQB94BZF4SMQNYEOt2L3nTqrSoQ8KoE559SAVDcNgJKcxxS', 'chu15@example.com', 0),
('卫十六', '$2a$10$y8zNMypQB94BZF4SMQNYEOt2L3nTqrSoQ8KoE559SAVDcNgJKcxxS', 'wei16@example.com', 1),
('蒋十七', '$2a$10$y8zNMypQB94BZF4SMQNYEOt2L3nTqrSoQ8KoE559SAVDcNgJKcxxS', 'jiang17@example.com', 1);