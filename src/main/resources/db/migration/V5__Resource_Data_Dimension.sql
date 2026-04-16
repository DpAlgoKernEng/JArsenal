-- V5: 为资源添加数据维度编码字段
-- 用于建立资源与数据权限维度的绑定关系

ALTER TABLE resource
ADD COLUMN data_dimension_code VARCHAR(50) COMMENT '数据维度编码（如：DEPARTMENT）' AFTER is_deleted;

-- 为已有资源绑定数据维度（示例）
-- 用户管理、角色管理等需要部门数据权限
UPDATE resource SET data_dimension_code = 'DEPARTMENT' WHERE code = 'USER_MANAGE';
UPDATE resource SET data_dimension_code = 'DEPARTMENT' WHERE code = 'ROLE_MANAGE';

-- 添加索引便于查询
CREATE INDEX idx_data_dimension ON resource(data_dimension_code);