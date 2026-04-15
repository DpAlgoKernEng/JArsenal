-- V1__Init_RBAC_Tables.sql

-- 角色表（支持继承树 + 软删除）
CREATE TABLE role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    name VARCHAR(100) NOT NULL COMMENT '角色名称',
    parent_id BIGINT COMMENT '父角色ID（继承）',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    inherit_mode VARCHAR(10) DEFAULT 'EXTEND' COMMENT '继承模式：EXTEND/LIMIT',
    is_builtin TINYINT DEFAULT 0 COMMENT '是否内置角色',
    is_deleted TINYINT DEFAULT 0 COMMENT '软删除：0-正常，1-已删除',
    version INT DEFAULT 0 COMMENT '版本号（用于缓存校验）',
    sort INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    INDEX idx_parent_id (parent_id),
    INDEX idx_code (code),
    INDEX idx_status (status),
    INDEX idx_not_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 资源表（菜单/操作/API三级 + 路径模式）
CREATE TABLE resource (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(100) NOT NULL UNIQUE COMMENT '资源编码',
    name VARCHAR(100) NOT NULL COMMENT '资源名称',
    parent_id BIGINT COMMENT '父资源ID',
    type VARCHAR(20) NOT NULL COMMENT '类型：MENU/OPERATION/API',
    path VARCHAR(200) COMMENT '路径（菜单路由）',
    path_pattern VARCHAR(200) COMMENT '路径模式（Ant风格：/api/users/**）',
    method VARCHAR(10) COMMENT 'API方法：GET/POST/PUT/DELETE',
    icon VARCHAR(50) COMMENT '菜单图标',
    component VARCHAR(100) COMMENT '前端组件路径',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    is_deleted TINYINT DEFAULT 0 COMMENT '软删除标记',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_parent_id (parent_id),
    INDEX idx_type (type),
    INDEX idx_path_pattern (path_pattern),
    INDEX idx_not_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源表';

-- 权限表（角色-资源关联）
CREATE TABLE permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL COMMENT '角色ID',
    resource_id BIGINT NOT NULL COMMENT '资源ID',
    effect VARCHAR(10) DEFAULT 'ALLOW' COMMENT '效果：ALLOW/DENY',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_resource (role_id, resource_id),
    INDEX idx_role_id (role_id),
    INDEX idx_resource_id (resource_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 权限操作表（拆分，规范化设计）
CREATE TABLE permission_action (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    action VARCHAR(20) NOT NULL COMMENT '操作：VIEW/CREATE/UPDATE/DELETE/EXECUTE',
    INDEX idx_permission_id (permission_id),
    UNIQUE KEY uk_perm_action (permission_id, action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限操作表';

-- 用户角色关联表（多角色）
CREATE TABLE user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 资源字段表（敏感字段定义）
CREATE TABLE resource_field (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    resource_id BIGINT NOT NULL COMMENT '资源ID',
    field_code VARCHAR(50) NOT NULL COMMENT '字段编码（对应DTO属性名）',
    field_name VARCHAR(100) NOT NULL COMMENT '字段名称',
    sensitive_level VARCHAR(20) DEFAULT 'NORMAL' COMMENT '敏感级别：NORMAL/HIDDEN/ENCRYPTED',
    mask_pattern VARCHAR(50) COMMENT '脱敏规则：ID_CARD/PHONE/SALARY',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_resource_field (resource_id, field_code),
    INDEX idx_resource_id (resource_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源字段表';

-- 字段权限表
CREATE TABLE field_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL COMMENT '角色ID',
    field_id BIGINT NOT NULL COMMENT '字段ID',
    can_view TINYINT DEFAULT 1 COMMENT '是否可查看',
    can_edit TINYINT DEFAULT 1 COMMENT '是否可编辑',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_field (role_id, field_id),
    INDEX idx_role_id (role_id),
    INDEX idx_field_id (field_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字段权限表';

-- 数据维度定义表
CREATE TABLE data_dimension (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '维度编码：DEPARTMENT/PROJECT/CUSTOMER',
    name VARCHAR(100) NOT NULL COMMENT '维度名称',
    description VARCHAR(200) COMMENT '描述',
    source_table VARCHAR(100) COMMENT '数据来源表',
    source_column VARCHAR(100) COMMENT '关联字段',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据维度定义表';

-- 角色数据权限表（改进：范围值拆分为子表）
CREATE TABLE role_data_scope (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL COMMENT '角色ID',
    dimension_code VARCHAR(50) NOT NULL COMMENT '维度编码',
    scope_type VARCHAR(20) NOT NULL COMMENT '范围类型：ALL/SELF/SELF_DEPT/DEPT_TREE/CUSTOM',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_dimension (role_id, dimension_code),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色数据权限表';

-- 数据权限范围值表（拆分，规范化设计）
CREATE TABLE role_data_scope_value (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    scope_id BIGINT NOT NULL COMMENT '数据权限ID',
    value_id BIGINT NOT NULL COMMENT '范围值ID（如部门ID、项目ID）',
    INDEX idx_scope_id (scope_id),
    UNIQUE KEY uk_scope_value (scope_id, value_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据权限范围值表';

-- 用户维度关联表（用户跨维度场景）
CREATE TABLE user_dimension (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    dimension_code VARCHAR(50) NOT NULL COMMENT '维度编码',
    dimension_value_id BIGINT NOT NULL COMMENT '维度值ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_dimension (user_id, dimension_code, dimension_value_id),
    INDEX idx_user_id (user_id),
    INDEX idx_dimension (dimension_code, dimension_value_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户维度关联表';

-- 权限变更审计表
CREATE TABLE permission_audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    operator_name VARCHAR(50) COMMENT '操作人姓名',
    change_type VARCHAR(20) NOT NULL COMMENT '变更类型',
    target_type VARCHAR(20) NOT NULL COMMENT '目标类型：ROLE/RESOURCE/USER',
    target_id BIGINT NOT NULL COMMENT '目标ID',
    before_value TEXT COMMENT '变更前值（JSON）',
    after_value TEXT COMMENT '变更后值（JSON）',
    reason VARCHAR(200) COMMENT '变更原因',
    trace_id VARCHAR(50) COMMENT '链路追踪ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_operator (operator_id),
    INDEX idx_target (target_type, target_id),
    INDEX idx_change_type (change_type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限变更审计表';