-- V4__RBAC_Preset_Permissions.sql
-- 预置系统菜单资源和权限配置

-- =============================================
-- 预置系统菜单资源（一级菜单）
-- =============================================

-- 系统管理菜单
INSERT INTO resource (id, code, name, parent_id, type, path, icon, component, sort, status, is_deleted, create_time)
VALUES (1, 'SYSTEM', '系统管理', NULL, 'MENU', '/system', 'Setting', 'Layout', 1, 1, 0, NOW())
ON DUPLICATE KEY UPDATE name = '系统管理', path = '/system';

-- =============================================
-- 预置系统菜单资源（二级菜单）
-- =============================================

-- 用户管理
INSERT INTO resource (id, code, name, parent_id, type, path, icon, component, sort, status, is_deleted, create_time)
VALUES (2, 'USER_MANAGE', '用户管理', 1, 'MENU', '/system/users', 'User', 'system/UserList', 1, 1, 0, NOW())
ON DUPLICATE KEY UPDATE name = '用户管理', parent_id = 1;

-- 角色管理
INSERT INTO resource (id, code, name, parent_id, type, path, icon, component, sort, status, is_deleted, create_time)
VALUES (3, 'ROLE_MANAGE', '角色管理', 1, 'MENU', '/system/roles', 'Peoples', 'system/RoleList', 2, 1, 0, NOW())
ON DUPLICATE KEY UPDATE name = '角色管理', parent_id = 1;

-- 资源管理
INSERT INTO resource (id, code, name, parent_id, type, path, icon, component, sort, status, is_deleted, create_time)
VALUES (4, 'RESOURCE_MANAGE', '资源管理', 1, 'MENU', '/system/resources', 'Tree', 'system/ResourceList', 3, 1, 0, NOW())
ON DUPLICATE KEY UPDATE name = '资源管理', parent_id = 1;

-- 权限管理
INSERT INTO resource (id, code, name, parent_id, type, path, icon, component, sort, status, is_deleted, create_time)
VALUES (5, 'PERMISSION', '权限管理', 1, 'MENU', '/system/permissions', 'Lock', 'system/PermissionList', 4, 1, 0, NOW())
ON DUPLICATE KEY UPDATE name = '权限管理', parent_id = 1;

-- =============================================
-- 预置操作资源（三级操作）
-- =============================================

-- 用户管理操作
INSERT INTO resource (id, code, name, parent_id, type, sort, status, is_deleted, create_time)
VALUES (6, 'USER_VIEW', '查看用户', 2, 'OPERATION', 1, 1, 0, NOW())
ON DUPLICATE KEY UPDATE name = '查看用户', parent_id = 2;

INSERT INTO resource (id, code, name, parent_id, type, sort, status, is_deleted, create_time)
VALUES (7, 'USER_CREATE', '创建用户', 2, 'OPERATION', 2, 1, 0, NOW())
ON DUPLICATE KEY UPDATE name = '创建用户', parent_id = 2;

INSERT INTO resource (id, code, name, parent_id, type, sort, status, is_deleted, create_time)
VALUES (8, 'USER_UPDATE', '编辑用户', 2, 'OPERATION', 3, 1, 0, NOW())
ON DUPLICATE KEY UPDATE name = '编辑用户', parent_id = 2;

INSERT INTO resource (id, code, name, parent_id, type, sort, status, is_deleted, create_time)
VALUES (9, 'USER_DELETE', '删除用户', 2, 'OPERATION', 4, 1, 0, NOW())
ON DUPLICATE KEY UPDATE name = '删除用户', parent_id = 2;

-- 角色管理操作
INSERT INTO resource (id, code, name, parent_id, type, sort, status, is_deleted, create_time)
VALUES (10, 'ROLE_VIEW', '查看角色', 3, 'OPERATION', 1, 1, 0, NOW())
ON DUPLICATE KEY UPDATE name = '查看角色', parent_id = 3;

INSERT INTO resource (id, code, name, parent_id, type, sort, status, is_deleted, create_time)
VALUES (11, 'ROLE_CREATE', '创建角色', 3, 'OPERATION', 2, 1, 0, NOW())
ON DUPLICATE KEY UPDATE name = '创建角色', parent_id = 3;

INSERT INTO resource (id, code, name, parent_id, type, sort, status, is_deleted, create_time)
VALUES (12, 'ROLE_UPDATE', '编辑角色', 3, 'OPERATION', 3, 1, 0, NOW())
ON DUPLICATE KEY UPDATE name = '编辑角色', parent_id = 3;

INSERT INTO resource (id, code, name, parent_id, type, sort, status, is_deleted, create_time)
VALUES (13, 'ROLE_DELETE', '删除角色', 3, 'OPERATION', 4, 1, 0, NOW())
ON DUPLICATE KEY UPDATE name = '删除角色', parent_id = 3;

INSERT INTO resource (id, code, name, parent_id, type, sort, status, is_deleted, create_time)
VALUES (14, 'ROLE_ASSIGN_PERMISSION', '分配权限', 3, 'OPERATION', 5, 1, 0, NOW())
ON DUPLICATE KEY UPDATE name = '分配权限', parent_id = 3;

-- 资源管理操作
INSERT INTO resource (id, code, name, parent_id, type, sort, status, is_deleted, create_time)
VALUES (15, 'RESOURCE_VIEW', '查看资源', 4, 'OPERATION', 1, 1, 0, NOW())
ON DUPLICATE KEY UPDATE name = '查看资源', parent_id = 4;

INSERT INTO resource (id, code, name, parent_id, type, sort, status, is_deleted, create_time)
VALUES (16, 'RESOURCE_CREATE', '创建资源', 4, 'OPERATION', 2, 1, 0, NOW())
ON DUPLICATE KEY UPDATE name = '创建资源', parent_id = 4;

INSERT INTO resource (id, code, name, parent_id, type, sort, status, is_deleted, create_time)
VALUES (17, 'RESOURCE_UPDATE', '编辑资源', 4, 'OPERATION', 3, 1, 0, NOW())
ON DUPLICATE KEY UPDATE name = '编辑资源', parent_id = 4;

INSERT INTO resource (id, code, name, parent_id, type, sort, status, is_deleted, create_time)
VALUES (18, 'RESOURCE_DELETE', '删除资源', 4, 'OPERATION', 4, 1, 0, NOW())
ON DUPLICATE KEY UPDATE name = '删除资源', parent_id = 4;

-- 权限管理操作
INSERT INTO resource (id, code, name, parent_id, type, sort, status, is_deleted, create_time)
VALUES (19, 'PERMISSION_VIEW', '查看权限', 5, 'OPERATION', 1, 1, 0, NOW())
ON DUPLICATE KEY UPDATE name = '查看权限', parent_id = 5;

INSERT INTO resource (id, code, name, parent_id, type, sort, status, is_deleted, create_time)
VALUES (20, 'PERMISSION_ASSIGN', '分配权限', 5, 'OPERATION', 2, 1, 0, NOW())
ON DUPLICATE KEY UPDATE name = '分配权限', parent_id = 5;

-- =============================================
-- 预置API资源
-- =============================================

-- 用户API
INSERT INTO resource (id, code, name, parent_id, type, path_pattern, method, sort, status, is_deleted, create_time)
VALUES (21, 'API_USER_LIST', '用户列表API', NULL, 'API', '/api/users/**', 'GET', 1, 1, 0, NOW())
ON DUPLICATE KEY UPDATE path_pattern = '/api/users/**', method = 'GET';

INSERT INTO resource (id, code, name, parent_id, type, path_pattern, method, sort, status, is_deleted, create_time)
VALUES (22, 'API_USER_CREATE', '创建用户API', NULL, 'API', '/api/users', 'POST', 2, 1, 0, NOW())
ON DUPLICATE KEY UPDATE path_pattern = '/api/users', method = 'POST';

INSERT INTO resource (id, code, name, parent_id, type, path_pattern, method, sort, status, is_deleted, create_time)
VALUES (23, 'API_USER_UPDATE', '更新用户API', NULL, 'API', '/api/users/**', 'PUT', 3, 1, 0, NOW())
ON DUPLICATE KEY UPDATE path_pattern = '/api/users/**', method = 'PUT';

INSERT INTO resource (id, code, name, parent_id, type, path_pattern, method, sort, status, is_deleted, create_time)
VALUES (24, 'API_USER_DELETE', '删除用户API', NULL, 'API', '/api/users/**', 'DELETE', 4, 1, 0, NOW())
ON DUPLICATE KEY UPDATE path_pattern = '/api/users/**', method = 'DELETE';

-- 角色API
INSERT INTO resource (id, code, name, parent_id, type, path_pattern, method, sort, status, is_deleted, create_time)
VALUES (25, 'API_ROLE_LIST', '角色列表API', NULL, 'API', '/api/roles/**', 'GET', 5, 1, 0, NOW())
ON DUPLICATE KEY UPDATE path_pattern = '/api/roles/**', method = 'GET';

INSERT INTO resource (id, code, name, parent_id, type, path_pattern, method, sort, status, is_deleted, create_time)
VALUES (26, 'API_ROLE_CREATE', '创建角色API', NULL, 'API', '/api/roles', 'POST', 6, 1, 0, NOW())
ON DUPLICATE KEY UPDATE path_pattern = '/api/roles', method = 'POST';

INSERT INTO resource (id, code, name, parent_id, type, path_pattern, method, sort, status, is_deleted, create_time)
VALUES (27, 'API_ROLE_UPDATE', '更新角色API', NULL, 'API', '/api/roles/**', 'PUT', 7, 1, 0, NOW())
ON DUPLICATE KEY UPDATE path_pattern = '/api/roles/**', method = 'PUT';

INSERT INTO resource (id, code, name, parent_id, type, path_pattern, method, sort, status, is_deleted, create_time)
VALUES (28, 'API_ROLE_DELETE', '删除角色API', NULL, 'API', '/api/roles/**', 'DELETE', 8, 1, 0, NOW())
ON DUPLICATE KEY UPDATE path_pattern = '/api/roles/**', method = 'DELETE';

-- 资源API
INSERT INTO resource (id, code, name, parent_id, type, path_pattern, method, sort, status, is_deleted, create_time)
VALUES (29, 'API_RESOURCE_LIST', '资源列表API', NULL, 'API', '/api/resources/**', 'GET', 9, 1, 0, NOW())
ON DUPLICATE KEY UPDATE path_pattern = '/api/resources/**', method = 'GET';

INSERT INTO resource (id, code, name, parent_id, type, path_pattern, method, sort, status, is_deleted, create_time)
VALUES (30, 'API_RESOURCE_CREATE', '创建资源API', NULL, 'API', '/api/resources', 'POST', 10, 1, 0, NOW())
ON DUPLICATE KEY UPDATE path_pattern = '/api/resources', method = 'POST';

INSERT INTO resource (id, code, name, parent_id, type, path_pattern, method, sort, status, is_deleted, create_time)
VALUES (31, 'API_RESOURCE_UPDATE', '更新资源API', NULL, 'API', '/api/resources/**', 'PUT', 11, 1, 0, NOW())
ON DUPLICATE KEY UPDATE path_pattern = '/api/resources/**', method = 'PUT';

INSERT INTO resource (id, code, name, parent_id, type, path_pattern, method, sort, status, is_deleted, create_time)
VALUES (32, 'API_RESOURCE_DELETE', '删除资源API', NULL, 'API', '/api/resources/**', 'DELETE', 12, 1, 0, NOW())
ON DUPLICATE KEY UPDATE path_pattern = '/api/resources/**', method = 'DELETE';

-- =============================================
-- 预置权限分配
-- =============================================

-- 超级管理员：拥有所有资源权限
INSERT INTO permission (id, role_id, resource_id, effect, create_time)
SELECT NULL, 1, id, 'ALLOW', NOW() FROM resource WHERE is_deleted = 0
ON DUPLICATE KEY UPDATE effect = 'ALLOW';

-- 系统管理员：拥有大部分权限（除删除操作）
INSERT INTO permission (id, role_id, resource_id, effect, create_time)
VALUES (100, 2, 1, 'ALLOW', NOW())  -- 系统管理菜单
ON DUPLICATE KEY UPDATE effect = 'ALLOW';

INSERT INTO permission (id, role_id, resource_id, effect, create_time)
VALUES (101, 2, 2, 'ALLOW', NOW())  -- 用户管理菜单
ON DUPLICATE KEY UPDATE effect = 'ALLOW';

INSERT INTO permission (id, role_id, resource_id, effect, create_time)
VALUES (102, 2, 6, 'ALLOW', NOW())  -- 查看用户
ON DUPLICATE KEY UPDATE effect = 'ALLOW';

INSERT INTO permission (id, role_id, resource_id, effect, create_time)
VALUES (103, 2, 7, 'ALLOW', NOW())  -- 创建用户
ON DUPLICATE KEY UPDATE effect = 'ALLOW';

INSERT INTO permission (id, role_id, resource_id, effect, create_time)
VALUES (104, 2, 8, 'ALLOW', NOW())  -- 编辑用户
ON DUPLICATE KEY UPDATE effect = 'ALLOW';

INSERT INTO permission (id, role_id, resource_id, effect, create_time)
VALUES (105, 2, 3, 'ALLOW', NOW())  -- 角色管理菜单
ON DUPLICATE KEY UPDATE effect = 'ALLOW';

INSERT INTO permission (id, role_id, resource_id, effect, create_time)
VALUES (106, 2, 10, 'ALLOW', NOW())  -- 查看角色
ON DUPLICATE KEY UPDATE effect = 'ALLOW';

INSERT INTO permission (id, role_id, resource_id, effect, create_time)
VALUES (107, 2, 11, 'ALLOW', NOW())  -- 创建角色
ON DUPLICATE KEY UPDATE effect = 'ALLOW';

INSERT INTO permission (id, role_id, resource_id, effect, create_time)
VALUES (108, 2, 12, 'ALLOW', NOW())  -- 编辑角色
ON DUPLICATE KEY UPDATE effect = 'ALLOW';

INSERT INTO permission (id, role_id, resource_id, effect, create_time)
VALUES (109, 2, 14, 'ALLOW', NOW())  -- 分配权限
ON DUPLICATE KEY UPDATE effect = 'ALLOW';

INSERT INTO permission (id, role_id, resource_id, effect, create_time)
VALUES (110, 2, 4, 'ALLOW', NOW())  -- 资源管理菜单
ON DUPLICATE KEY UPDATE effect = 'ALLOW';

INSERT INTO permission (id, role_id, resource_id, effect, create_time)
VALUES (111, 2, 15, 'ALLOW', NOW())  -- 查看资源
ON DUPLICATE KEY UPDATE effect = 'ALLOW';

INSERT INTO permission (id, role_id, resource_id, effect, create_time)
VALUES (112, 2, 5, 'ALLOW', NOW())  -- 权限管理菜单
ON DUPLICATE KEY UPDATE effect = 'ALLOW';

INSERT INTO permission (id, role_id, resource_id, effect, create_time)
VALUES (113, 2, 19, 'ALLOW', NOW())  -- 查看权限
ON DUPLICATE KEY UPDATE effect = 'ALLOW';

INSERT INTO permission (id, role_id, resource_id, effect, create_time)
VALUES (114, 2, 20, 'ALLOW', NOW())  -- 分配权限
ON DUPLICATE KEY UPDATE effect = 'ALLOW';

-- API权限
INSERT INTO permission (id, role_id, resource_id, effect, create_time)
VALUES (115, 2, 21, 'ALLOW', NOW())  -- 用户列表API
ON DUPLICATE KEY UPDATE effect = 'ALLOW';

INSERT INTO permission (id, role_id, resource_id, effect, create_time)
VALUES (116, 2, 22, 'ALLOW', NOW())  -- 创建用户API
ON DUPLICATE KEY UPDATE effect = 'ALLOW';

INSERT INTO permission (id, role_id, resource_id, effect, create_time)
VALUES (117, 2, 23, 'ALLOW', NOW())  -- 更新用户API
ON DUPLICATE KEY UPDATE effect = 'ALLOW';

INSERT INTO permission (id, role_id, resource_id, effect, create_time)
VALUES (118, 2, 25, 'ALLOW', NOW())  -- 角色列表API
ON DUPLICATE KEY UPDATE effect = 'ALLOW';

INSERT INTO permission (id, role_id, resource_id, effect, create_time)
VALUES (119, 2, 26, 'ALLOW', NOW())  -- 创建角色API
ON DUPLICATE KEY UPDATE effect = 'ALLOW';

INSERT INTO permission (id, role_id, resource_id, effect, create_time)
VALUES (120, 2, 27, 'ALLOW', NOW())  -- 更新角色API
ON DUPLICATE KEY UPDATE effect = 'ALLOW';

INSERT INTO permission (id, role_id, resource_id, effect, create_time)
VALUES (121, 2, 29, 'ALLOW', NOW())  -- 资源列表API
ON DUPLICATE KEY UPDATE effect = 'ALLOW';

-- 部门管理员：拥有查看和编辑权限
INSERT INTO permission (id, role_id, resource_id, effect, create_time)
VALUES (200, 3, 1, 'ALLOW', NOW())  -- 系统管理菜单
ON DUPLICATE KEY UPDATE effect = 'ALLOW';

INSERT INTO permission (id, role_id, resource_id, effect, create_time)
VALUES (201, 3, 2, 'ALLOW', NOW())  -- 用户管理菜单
ON DUPLICATE KEY UPDATE effect = 'ALLOW';

INSERT INTO permission (id, role_id, resource_id, effect, create_time)
VALUES (202, 3, 6, 'ALLOW', NOW())  -- 查看用户
ON DUPLICATE KEY UPDATE effect = 'ALLOW';

INSERT INTO permission (id, role_id, resource_id, effect, create_time)
VALUES (203, 3, 8, 'ALLOW', NOW())  -- 编辑用户
ON DUPLICATE KEY UPDATE effect = 'ALLOW';

INSERT INTO permission (id, role_id, resource_id, effect, create_time)
VALUES (204, 3, 3, 'ALLOW', NOW())  -- 角色管理菜单
ON DUPLICATE KEY UPDATE effect = 'ALLOW';

INSERT INTO permission (id, role_id, resource_id, effect, create_time)
VALUES (205, 3, 10, 'ALLOW', NOW())  -- 查看角色
ON DUPLICATE KEY UPDATE effect = 'ALLOW';

-- API权限
INSERT INTO permission (id, role_id, resource_id, effect, create_time)
VALUES (206, 3, 21, 'ALLOW', NOW())  -- 用户列表API
ON DUPLICATE KEY UPDATE effect = 'ALLOW';

INSERT INTO permission (id, role_id, resource_id, effect, create_time)
VALUES (207, 3, 23, 'ALLOW', NOW())  -- 更新用户API
ON DUPLICATE KEY UPDATE effect = 'ALLOW';

INSERT INTO permission (id, role_id, resource_id, effect, create_time)
VALUES (208, 3, 25, 'ALLOW', NOW())  -- 角色列表API
ON DUPLICATE KEY UPDATE effect = 'ALLOW';

-- 普通用户：仅拥有查看权限
INSERT INTO permission (id, role_id, resource_id, effect, create_time)
VALUES (300, 4, 1, 'ALLOW', NOW())  -- 系统管理菜单
ON DUPLICATE KEY UPDATE effect = 'ALLOW';

INSERT INTO permission (id, role_id, resource_id, effect, create_time)
VALUES (301, 4, 2, 'ALLOW', NOW())  -- 用户管理菜单
ON DUPLICATE KEY UPDATE effect = 'ALLOW';

INSERT INTO permission (id, role_id, resource_id, effect, create_time)
VALUES (302, 4, 6, 'ALLOW', NOW())  -- 查看用户
ON DUPLICATE KEY UPDATE effect = 'ALLOW';

INSERT INTO permission (id, role_id, resource_id, effect, create_time)
VALUES (303, 4, 3, 'ALLOW', NOW())  -- 角色管理菜单
ON DUPLICATE KEY UPDATE effect = 'ALLOW';

INSERT INTO permission (id, role_id, resource_id, effect, create_time)
VALUES (304, 4, 10, 'ALLOW', NOW())  -- 查看角色
ON DUPLICATE KEY UPDATE effect = 'ALLOW';

-- API权限
INSERT INTO permission (id, role_id, resource_id, effect, create_time)
VALUES (305, 4, 21, 'ALLOW', NOW())  -- 用户列表API
ON DUPLICATE KEY UPDATE effect = 'ALLOW';

INSERT INTO permission (id, role_id, resource_id, effect, create_time)
VALUES (306, 4, 25, 'ALLOW', NOW())  -- 角色列表API
ON DUPLICATE KEY UPDATE effect = 'ALLOW';

-- =============================================
-- 预置权限操作
-- =============================================

-- 为超级管理员的权限添加所有操作
INSERT INTO permission_action (permission_id, action)
SELECT p.id, 'VIEW' FROM permission p WHERE p.role_id = 1 AND p.effect = 'ALLOW'
ON DUPLICATE KEY UPDATE action = 'VIEW';

INSERT INTO permission_action (permission_id, action)
SELECT p.id, 'CREATE' FROM permission p WHERE p.role_id = 1 AND p.effect = 'ALLOW'
ON DUPLICATE KEY UPDATE action = 'CREATE';

INSERT INTO permission_action (permission_id, action)
SELECT p.id, 'UPDATE' FROM permission p WHERE p.role_id = 1 AND p.effect = 'ALLOW'
ON DUPLICATE KEY UPDATE action = 'UPDATE';

INSERT INTO permission_action (permission_id, action)
SELECT p.id, 'DELETE' FROM permission p WHERE p.role_id = 1 AND p.effect = 'ALLOW'
ON DUPLICATE KEY UPDATE action = 'DELETE';

INSERT INTO permission_action (permission_id, action)
SELECT p.id, 'EXECUTE' FROM permission p WHERE p.role_id = 1 AND p.effect = 'ALLOW'
ON DUPLICATE KEY UPDATE action = 'EXECUTE';