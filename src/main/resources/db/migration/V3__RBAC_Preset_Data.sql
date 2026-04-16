-- V3__RBAC_Preset_Data.sql
-- 预置角色和数据维度

-- =============================================
-- 预置角色（内置角色，不可删除）
-- =============================================

-- 超级管理员：拥有所有权限
INSERT INTO role (id, code, name, parent_id, status, inherit_mode, is_builtin, is_deleted, sort, create_time)
VALUES (1, 'SUPER_ADMIN', '超级管理员', NULL, 1, 'EXTEND', 1, 0, 1, NOW())
ON DUPLICATE KEY UPDATE name = '超级管理员';

-- 系统管理员：拥有大部分权限
INSERT INTO role (id, code, name, parent_id, status, inherit_mode, is_builtin, is_deleted, sort, create_time)
VALUES (2, 'ADMIN', '系统管理员', NULL, 1, 'EXTEND', 1, 0, 2, NOW())
ON DUPLICATE KEY UPDATE name = '系统管理员';

-- 部门管理员：拥有部门范围内权限
INSERT INTO role (id, code, name, parent_id, status, inherit_mode, is_builtin, is_deleted, sort, create_time)
VALUES (3, 'DEPT_MANAGER', '部门管理员', NULL, 1, 'EXTEND', 1, 0, 3, NOW())
ON DUPLICATE KEY UPDATE name = '部门管理员';

-- 普通用户：拥有基础权限
INSERT INTO role (id, code, name, parent_id, status, inherit_mode, is_builtin, is_deleted, sort, create_time)
VALUES (4, 'USER', '普通用户', NULL, 1, 'EXTEND', 1, 0, 4, NOW())
ON DUPLICATE KEY UPDATE name = '普通用户';

-- =============================================
-- 预置数据维度
-- =============================================

-- 部门维度
INSERT INTO data_dimension (id, code, name, description, source_table, source_column, status, create_time)
VALUES (1, 'DEPARTMENT', '部门维度', '按部门划分数据权限范围', 'department', 'dept_id', 1, NOW())
ON DUPLICATE KEY UPDATE name = '部门维度', description = '按部门划分数据权限范围';

-- 项目维度
INSERT INTO data_dimension (id, code, name, description, source_table, source_column, status, create_time)
VALUES (2, 'PROJECT', '项目维度', '按项目划分数据权限范围', 'project', 'project_id', 1, NOW())
ON DUPLICATE KEY UPDATE name = '项目维度', description = '按项目划分数据权限范围';

-- 客户维度
INSERT INTO data_dimension (id, code, name, description, source_table, source_column, status, create_time)
VALUES (3, 'CUSTOMER', '客户维度', '按客户划分数据权限范围', 'customer', 'customer_id', 1, NOW())
ON DUPLICATE KEY UPDATE name = '客户维度', description = '按客户划分数据权限范围';

-- =============================================
-- 预置角色数据范围
-- =============================================

-- 超级管理员：所有维度均有全部权限
INSERT INTO role_data_scope (id, role_id, dimension_code, scope_type, create_time)
VALUES (1, 1, 'DEPARTMENT', 'ALL', NOW())
ON DUPLICATE KEY UPDATE scope_type = 'ALL';

INSERT INTO role_data_scope (id, role_id, dimension_code, scope_type, create_time)
VALUES (2, 1, 'PROJECT', 'ALL', NOW())
ON DUPLICATE KEY UPDATE scope_type = 'ALL';

INSERT INTO role_data_scope (id, role_id, dimension_code, scope_type, create_time)
VALUES (3, 1, 'CUSTOMER', 'ALL', NOW())
ON DUPLICATE KEY UPDATE scope_type = 'ALL';

-- 系统管理员：部门维度为全部，其他为自定义（需配置）
INSERT INTO role_data_scope (id, role_id, dimension_code, scope_type, create_time)
VALUES (4, 2, 'DEPARTMENT', 'ALL', NOW())
ON DUPLICATE KEY UPDATE scope_type = 'ALL';

-- 部门管理员：部门维度为本部门及下属部门
INSERT INTO role_data_scope (id, role_id, dimension_code, scope_type, create_time)
VALUES (5, 3, 'DEPARTMENT', 'DEPT_TREE', NOW())
ON DUPLICATE KEY UPDATE scope_type = 'DEPT_TREE';

-- 普通用户：部门维度为本部门
INSERT INTO role_data_scope (id, role_id, dimension_code, scope_type, create_time)
VALUES (6, 4, 'DEPARTMENT', 'SELF_DEPT', NOW())
ON DUPLICATE KEY UPDATE scope_type = 'SELF_DEPT';