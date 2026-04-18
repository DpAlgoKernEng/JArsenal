# RBAC 权限管理系统设计规范

> 文档版本：2.1
> 创建日期：2026-04-14
> 更新日期：2026-04-14
> 适用项目：JGuard (Spring Boot 3.x + Vue 3)

---

## 一、概述

### 1.1 设计目标

为 JGuard 项目设计完整的企业级 RBAC 权限管理系统，支持：

- **功能权限**：菜单 + 操作 + API 三级控制，前后端双重校验
- **数据权限**：多维度（部门、项目、客户等）数据隔离，规则可组合
- **字段权限**：敏感字段动态脱敏或隐藏
- **角色管理**：预设角色 + 自定义角色 + 角色继承树
- **继承规则**：子角色继承父权限，可扩展或限制
- **用户角色**：多角色，权限取并集

### 1.2 技术栈适配

| 层级 | 现有技术 | 权限系统适配 |
|------|----------|--------------|
| 后端架构 | DDD 四层架构 | 权限领域模型融入 Domain 层 |
| 认证方式 | JWT Token + AuthInterceptor | 扩展 PermissionInterceptor |
| ORM | MyBatis | DataScopeInterceptor 拦截器 |
| 缓存 | Redis + Caffeine | 二级权限缓存 |
| 前端 | Vue 3 + Element Plus + Pinia | 权限状态管理 + 路由守卫 + 指令 |

### 1.3 设计方案

采用**标准扩展 RBAC**方案：
- Role-Permission-Resource 核心模型
- 数据权限用独立维度表实现
- 字段权限用字段配置表实现
- 自然融入现有 DDD 领域模型
- 权限位图预计算优化性能
- 参数化查询确保安全

---

## 二、领域模型设计

### 2.1 核心聚合根

```
┌─────────────────────────────────────────────────────────────────────┐
│                      Permission Domain                               │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  ┌─────────────────────┐         ┌─────────────────────┐            │
│  │        Role          │         │      Resource       │            │
│  │     (Aggregate)      │         │     (Aggregate)     │            │
│  ├─────────────────────┤         ├─────────────────────┤            │
│  │ - roleId            │         │ - resourceId        │            │
│  │ - roleCode          │         │ - resourceCode      │            │
│  │ - roleName          │         │ - resourceName      │            │
│  │ - parentId          │────────▶│ - parentId          │            │
│  │ - status            │         │ - type              │            │
│  │ - inheritMode       │         │ - path              │            │
│  │ - isDeleted         │         │ - pathPattern       │            │
│  │ - rolePermissions[] │         │ - method            │            │
│  │   (值对象集合)       │         │ - sort              │            │
│  │ - dataScopes[]      │         │ - status            │            │
│  │ - fieldPerms[]      │         │ - isDeleted         │            │
│  └─────────────────────┘         │ - sensitiveFields[] │            │
│            │                     └─────────────────────┘            │
│            │                              │                         │
│            │                     ┌─────────────────────┐            │
│            │                     │   ResourceField     │            │
│            │                     │     (Entity)        │            │
│            │                     ├─────────────────────┤            │
│            └────────────────────▶│ - fieldId           │            │
│                                  │ - fieldCode         │            │
│                                  │ - fieldName         │            │
│                                  │ - sensitiveLevel    │            │
│                                  │ - maskPattern       │            │
│                                  └─────────────────────┘            │
│                                                                       │
│  ┌─────────────────────┐         ┌─────────────────────┐            │
│  │   RolePermission    │         │    RoleDataScope    │            │
│  │    (值对象)         │         │      (Entity)       │            │
│  ├─────────────────────┤         ├─────────────────────┤            │
│  │ - resourceId        │         │ - scopeId           │            │
│  │ - actions (Set)     │         │ - dimensionCode     │            │
│  │ - effect            │         │ - scopeType         │            │
│  └─────────────────────┘         │ - scopeValueIds[]   │            │
│                                  │   (子表关联)        │            │
│                                  └─────────────────────┘            │
│                                                                       │
│  ┌─────────────────────┐                                             │
│  │   PermissionBitmap  │                                             │
│  │    (值对象)         │                                             │
│  ├─────────────────────┤                                             │
│  │ - actionBits        │                                             │
│  │   Map<ResourceId,   │                                             │
│  │   BitSet>           │                                             │
│  │ - version           │                                             │
│  └─────────────────────┘                                             │
│                                                                       │
└─────────────────────────────────────────────────────────────────────┘
```

### 2.2 值对象设计

| 值对象 | 用途 | 校验规则 |
|--------|------|----------|
| `RoleId` | 角色标识 | Long，不可为空 |
| `RoleCode` | 角色编码 | 2-50字符，唯一，如 `ADMIN`、`DEPT_MANAGER` |
| `ResourceType` | 资源类型枚举 | MENU(菜单)、OPERATION(操作)、API(接口) |
| `ActionType` | 操作类型枚举 | VIEW(查看)、CREATE(新增)、UPDATE(编辑)、DELETE(删除)、EXECUTE(执行) |
| `DimensionType` | 数据维度枚举 | DEPARTMENT(部门)、PROJECT(项目)、CUSTOMER(客户)、CUSTOM(自定义) |
| `PermissionEffect` | 权限效果枚举 | ALLOW(允许)、DENY(拒绝) |
| `InheritMode` | 继承模式枚举 | EXTEND(继承并扩展)、LIMIT(继承并限制) |
| `SensitiveLevel` | 字段敏感级别 | NORMAL(普通)、HIDDEN(隐藏)、ENCRYPTED(加密) |
| `ScopeType` | 数据范围类型 | ALL(全部)、SELF(仅本人)、SELF_DEPT(本部门)、DEPT_TREE(本部门及下级)、CUSTOM(自定义) |

### 2.3 聚合根业务行为

**Role 聚合根（改进版）：**

```java
public class Role extends BaseEntity<RoleId> {
    private RoleCode code;
    private String name;
    private RoleId parentId;        // 父角色（继承）
    private RoleStatus status;
    private InheritMode inheritMode; // EXTEND/LIMIT（语义更清晰）
    private boolean isDeleted;      // 软删除标记
    private Set<RolePermission> permissions;  // 值对象集合，不可变
    private Set<RoleDataScope> dataScopes;    // 改为Set，防止重复维度
    private List<FieldPermission> fieldPerms;
    
    // 工厂方法
    public static Role create(RoleCode code, String name, RoleId parentId, InheritMode mode);
    
    // 业务行为
    public void assignPermission(ResourceId resource, Set<ActionType> actions);
    public void removePermission(ResourceId resource);
    public void assignDataScope(DimensionType dimension, ScopeType type, Set<Long> values);
    public void removeDataScope(DimensionType dimension);
    public void enable();
    public void disable();
    public void softDelete();        // 软删除
    
    // 权限位图计算移至PermissionDomainService（符合DDD领域服务职责）
    // 此处仅提供角色自身权限数据
    public Set<RolePermission> getOwnPermissions();
    public Set<RoleDataScope> getOwnDataScopes();
}
```

**RolePermission 值对象（改进版）：**

```java
/**
 * 角色权限值对象 - 不可变，无独立生命周期
 */
public class RolePermission {
    private final ResourceId resource;      // 资源ID
    private final Set<ActionType> actions;  // 操作集合
    private final PermissionEffect effect;  // ALLOW/DENY
    
    // 构造即验证
    public RolePermission(ResourceId resource, Set<ActionType> actions, PermissionEffect effect) {
        if (resource == null) throw new DomainException("资源ID不能为空");
        if (actions == null || actions.isEmpty()) throw new DomainException("操作集合不能为空");
        this.resource = resource;
        this.actions = Collections.unmodifiableSet(new HashSet<>(actions)); // 不可变
        this.effect = effect;
    }
    
    // 无setter，保证不可变性
}
```

**PermissionBitmap 值对象（性能优化）：**

```java
/**
 * 权限位图 - 用于快速权限合并计算
 * O(n)复杂度而非O(n²)
 */
public class PermissionBitmap {
    private final Map<ResourceId, BitSet> actionBits;  // 每个资源的操作位图
    private final long version;                        // 版本号，用于缓存校验
    
    /**
     * 位图合并（高效O(n)操作）
     */
    public PermissionBitmap merge(PermissionBitmap other) {
        Map<ResourceId, BitSet> merged = new HashMap<>(this.actionBits);
        
        other.actionBits.forEach((resource, bits) -> {
            BitSet existing = merged.get(resource);
            if (existing != null) {
                existing.or(bits);  // 位运算合并，高效
            } else {
                merged.put(resource, (BitSet) bits.clone());
            }
        });
        
        return new PermissionBitmap(merged, System.currentTimeMillis());
    }
    
    /**
     * 检查是否有指定操作权限
     */
    public boolean hasAction(ResourceId resource, ActionType action) {
        BitSet bits = actionBits.get(resource);
        return bits != null && bits.get(action.ordinal());
    }
    
    /**
     * 处理DENY冲突（DENY优先）
     */
    public PermissionBitmap applyDeny(PermissionBitmap denyBitmap) {
        denyBitmap.actionBits.forEach((resource, denyBits) -> {
            BitSet existing = this.actionBits.get(resource);
            if (existing != null) {
                existing.andNot(denyBits);  // 移除DENY的操作
            }
        });
        return this;
    }
}
```

### 2.4 领域服务

| 服务 | 职责 |
|------|------|
| `PermissionDomainService` | 计算用户有效权限（权限位图预计算 + 多角色合并） |
| `DataScopeDomainService` | 计算用户数据权限范围（多维度合并） |
| `RoleHierarchyService` | 角色继承树遍历，防止循环引用，计算继承链权限 |
| `PermissionAuditService` | 权限变更审计日志记录 |

### 2.5 领域事件

| 事件 | 触发时机 | 处理逻辑 |
|------|----------|----------|
| `RoleCreated` | 创建角色 | 记录审计日志 |
| `RolePermissionChanged` | 角色权限变更 | 清除相关用户权限缓存 + 记录审计日志 |
| `UserRoleAssigned` | 用户分配角色 | 清除用户权限缓存 + 记录审计日志 |
| `RoleDeleted` | 角色软删除 | 清除相关用户权限缓存 |

---

## 三、数据库表结构设计

### 3.1 核心表设计（改进版）

```sql
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
```

### 3.2 数据权限表设计（改进版）

```sql
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
```

### 3.3 权限审计表（新增）

```sql
-- 权限变更审计表
CREATE TABLE permission_audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    operator_name VARCHAR(50) COMMENT '操作人姓名',
    change_type VARCHAR(20) NOT NULL COMMENT '变更类型：ROLE_CREATE/ROLE_UPDATE/ROLE_DELETE/PERM_ASSIGN/PERM_REMOVE/USER_ROLE_ASSIGN',
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

-- =====================================================
-- 数据库触发器：防止角色循环继承（数据库层面兜底）
-- =====================================================

DELIMITER //

/**
 * 触发器：角色parent_id更新前校验循环继承
 * 在数据库层面确保数据完整性，防止代码层遗漏
 */
CREATE TRIGGER trg_role_before_update
BEFORE UPDATE ON role
FOR EACH ROW
BEGIN
    DECLARE is_circular INT DEFAULT 0;
    DECLARE current_parent BIGINT;
    
    -- 仅当parent_id变更时校验
    IF NEW.parent_id IS NOT NULL AND (OLD.parent_id IS NULL OR NEW.parent_id != OLD.parent_id) THEN
        -- 不能将自己设为父角色
        IF NEW.parent_id = NEW.id THEN
            SIGNAL SQLSTATE '45000' 
            SET MESSAGE_TEXT = '角色不能以自己为父角色（循环继承）';
        END IF;
        
        -- 检查继承链是否形成循环
        SET current_parent = NEW.parent_id;
        SET is_circular = 0;
        
        WHILE current_parent IS NOT NULL AND is_circular = 0 DO
            -- 如果继承链中出现当前角色ID，则形成循环
            IF current_parent = NEW.id THEN
                SET is_circular = 1;
            END IF;
            
            -- 继续向上查找父角色
            SELECT parent_id INTO current_parent 
            FROM role 
            WHERE id = current_parent AND is_deleted = 0;
        END WHILE;
        
        -- 发现循环继承，拒绝更新
        IF is_circular = 1 THEN
            SIGNAL SQLSTATE '45000' 
            SET MESSAGE_TEXT = '角色继承链形成循环，更新被拒绝';
        END IF;
    END IF;
END//

/**
 * 触发器：角色插入前校验parent_id有效性
 */
CREATE TRIGGER trg_role_before_insert
BEFORE INSERT ON role
FOR EACH ROW
BEGIN
    DECLARE parent_exists INT DEFAULT 0;
    
    IF NEW.parent_id IS NOT NULL THEN
        -- 不能将自己设为父角色
        IF NEW.parent_id = NEW.id THEN
            SIGNAL SQLSTATE '45000' 
            SET MESSAGE_TEXT = '角色不能以自己为父角色（循环继承）';
        END IF;
        
        -- 检查父角色是否存在且未删除
        SELECT COUNT(*) INTO parent_exists 
        FROM role 
        WHERE id = NEW.parent_id AND is_deleted = 0;
        
        IF parent_exists = 0 THEN
            SIGNAL SQLSTATE '45000' 
            SET MESSAGE_TEXT = '父角色不存在或已被删除';
        END IF;
    END IF;
END//

DELIMITER ;
```

### 3.4 预设数据

```sql
-- 内置角色
INSERT INTO role (code, name, parent_id, is_builtin, sort, inherit_mode) VALUES
('SUPER_ADMIN', '超级管理员', NULL, 1, 1, 'EXTEND'),
('ADMIN', '系统管理员', 1, 1, 2, 'EXTEND'),
('DEPT_MANAGER', '部门管理员', 2, 1, 3, 'LIMIT'),
('USER', '普通用户', 3, 1, 4, 'LIMIT');

-- 数据维度
INSERT INTO data_dimension (code, name, source_table, source_column) VALUES
('DEPARTMENT', '部门', 'department', 'dept_id'),
('PROJECT', '项目', 'project', 'project_id'),
('CUSTOMER', '客户', 'customer', 'customer_id');

-- 超级管理员数据权限（全部数据）
INSERT INTO role_data_scope (role_id, dimension_code, scope_type) VALUES
(1, 'DEPARTMENT', 'ALL'),
(1, 'PROJECT', 'ALL'),
(1, 'CUSTOMER', 'ALL');

-- 普通用户数据权限（仅本人）
INSERT INTO role_data_scope (role_id, dimension_code, scope_type) VALUES
(4, 'DEPARTMENT', 'SELF'),
(4, 'PROJECT', 'SELF'),
(4, 'CUSTOMER', 'SELF');
```

### 3.5 表关系图

```
user ──────────────┐
                   │
                   ▼
             user_role ──────────────▶ role
                                          │
                                          │ parent_id (自关联继承)
                                          │
                                          ├──▶ permission ──▶ permission_action
                                          │        │
                                          │        └──────────▶ resource
                                          │                         │
                                          │                         ▼
                                          │                   resource_field
                                          │                         │
                                          │                         ▼
                                          └──▶ field_permission ◀───┘
                                          │
                                          └──▶ role_data_scope ──▶ role_data_scope_value
                                          │                              │
                                          └─────────────────────────────▶ data_dimension
                                                                        │
                                                                        ▼
                                                                  user_dimension

permission_audit_log ──▶ 记录所有权限变更
```

---

## 四、权限检查流程设计

### 4.1 功能权限检查流程

```
┌─────────────────────────────────────────────────────────────────────┐
│                       功能权限检查流程                                │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  用户请求 ──▶ AuthInterceptor ──▶ PermissionInterceptor             │
│                                      │                                │
│                                      ▼                                │
│                         ┌─────────────────────┐                      │
│                         │ 从JWT获取userId     │                      │
│                         │ 加载权限位图缓存     │                      │
│                         │ 校验版本号一致性     │                      │
│                         └─────────────────────┘                      │
│                                      │                                │
│                                      ▼                                │
│                         ┌─────────────────────┐                      │
│                         │ Ant路径模式匹配     │                      │
│                         │ (/api/users/**)     │                      │
│                         └─────────────────────┘                      │
│                                      │                                │
│                           ┌──────────┴──────────┐                    │
│                           │                     │                    │
│                           ▼                     ▼                    │
│                      ┌─────────┐           ┌─────────┐               │
│                      │ 匹配成功 │           │ 匹配失败 │               │
│                      │ 检查位图 │           │ 默认放行 │               │
│                      └────┬────┘           │ 或拒绝   │               │
│                           │                └────┬────┘               │
│                           ▼                     │                    │
│                      ┌─────────┐                │                    │
│                      │ ALLOW   │                ▼                    │
│                      │ 继续执行 │           返回403                  │
│                      └─────────┘                                     │
│                                                                       │
└─────────────────────────────────────────────────────────────────────┘
```

### 4.2 权限位图计算（性能优化）

```java
public class PermissionDomainService {
    
    /**
     * 计算用户权限位图（预计算优化）
     * 性能从O(n²)优化到O(n)
     */
    public PermissionBitmap computeUserPermissionBitmap(Long userId) {
        // 1. 获取用户所有角色
        List<Role> roles = userRoleRepository.findRolesByUserId(userId);
        
        // 2. 获取角色版本号，用于缓存校验
        long version = computeVersion(roles);
        
        // 3. 计算每个角色的权限位图（含继承）
        PermissionBitmap result = PermissionBitmap.empty(version);
        PermissionBitmap denyBitmap = PermissionBitmap.empty(version);
        
        for (Role role : roles) {
            PermissionBitmap roleBitmap = computeRolePermissionBitmap(role);
            
            // 分离ALLOW和DENY
            if (role.hasDenyPermissions()) {
                denyBitmap.merge(role.getDenyBitmap());
            }
            result.merge(roleBitmap);
        }
        
        // 4. 应用DENY优先规则
        result.applyDeny(denyBitmap);
        
        return result;
    }
    
    /**
     * 计算角色权限位图（含继承链）
     */
    private PermissionBitmap computeRolePermissionBitmap(Role role) {
        PermissionBitmap bitmap = PermissionBitmap.empty();
        
        // 递归获取父角色权限位图
        if (role.getParentId() != null && !role.getParentId().equals(role.getId())) {
            // 防止循环引用
            validateNoCircularInheritance(role);
            
            Role parent = roleRepository.findById(role.getParentId());
            PermissionBitmap parentBitmap = computeRolePermissionBitmap(parent);
            
            // 根据inheritMode决定继承方式
            if (role.getInheritMode() == InheritMode.EXTEND) {
                bitmap.merge(parentBitmap);  // 继承并扩展
            } else {
                bitmap.merge(parentBitmap);   // 继承
                bitmap.applyDeny(role.getLimitBitmap());  // 限制部分权限
            }
        }
        
        // 加上角色自身权限
        bitmap.merge(role.getOwnPermissionBitmap());
        
        return bitmap;
    }
    
    /**
     * 验证无循环继承
     */
    private void validateNoCircularInheritance(Role role) {
        Set<Long> visited = new HashSet<>();
        Long current = role.getParentId();
        
        while (current != null) {
            if (visited.contains(current)) {
                throw new DomainException("角色继承存在循环引用: " + role.getCode());
            }
            visited.add(current);
            Role parent = roleRepository.findById(current);
            current = parent.getParentId();
        }
    }
}
```

### 4.3 权限拦截器设计（改进版）

**PermissionInterceptor：**

```java
@Component
public class PermissionInterceptor implements HandlerInterceptor {
    
    private final PermissionCacheService permissionCache;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                             HttpServletResponse response,
                             Object handler) throws Exception {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return true;  // 未登录由AuthInterceptor处理
        }
        
        // 加载权限位图缓存（带版本校验）
        PermissionBitmap bitmap = permissionCache.getPermissionBitmap(userId);
        
        // Ant路径模式匹配
        String requestPath = request.getRequestURI();
        String method = request.getMethod();
        
        ResourceId resourceId = matchResource(requestPath, method);
        if (resourceId == null) {
            // 未配置的资源，检查是否需要权限
            return checkDefaultAccess(requestPath, response);
        }
        
        // 位图检查（高效）
        ActionType action = determineAction(method, handler);
        if (!bitmap.hasAction(resourceId, action)) {
            sendError(response, 403, "无访问权限");
            return false;
        }
        
        return true;
    }
    
    /**
     * Ant路径模式匹配
     */
    private ResourceId matchResource(String path, String method) {
        List<Resource> apis = resourceCache.getAllApis();
        
        for (Resource api : apis) {
            if (pathMatcher.match(api.getPathPattern(), path) 
                && api.getMethod().equalsIgnoreCase(method)) {
                return api.getId();
            }
        }
        return null;
    }
    
    /**
     * 异步线程权限传递（使用SecurityContextHolder）
     */
    public static void setupAsyncContext(Long userId) {
        SecurityContextHolder.getContext().setAuthentication(
            new PermissionAuthentication(userId)
        );
    }
}
```

### 4.4 权限注解设计

**@RequirePermission 注解：**

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    String resourceCode();
    ActionType[] actions();
    String message() default "无操作权限";
}

/**
 * 批量操作权限注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireBatchPermission {
    String resourceCode();
    ActionType action();
    String idParam() default "ids";  // 批量ID参数名
    String message() default "无批量操作权限";
}
```

### 4.5 前端权限检查（改进版）

**Pinia 权限状态管理：**

```javascript
export const usePermissionStore = defineStore('permission', {
  state: () => ({
    permissions: [],
    menus: [],
    actions: {},
    fields: {},
    loaded: false,
    version: 0// 版本号
  }),
  
  actions: {
    // 登录后首屏加载权限
    async loadPermissions() {
      const res = await api.get('/api/auth/permissions')
      this.permissions = res.data.permissions
      this.menus = buildMenuTree(res.data.menus)
      this.actions = res.data.actions
      this.fields = res.data.fields
      this.version = res.data.version
      this.loaded = true
      
      // 动态添加路由
      addDynamicRoutes(this.menus)
    },
    
    hasMenu(menuCode) {
      return this.menus.some(m => m.code === menuCode)
    },
    
    hasAction(resourceCode, action) {
      const actions = this.actions[resourceCode] || []
      return actions.includes(action)
    },
    
    canViewField(resourceCode, fieldCode) {
      const fields = this.fields[resourceCode] || {}
      return fields[fieldCode]?.canView !== false
    },
    
    canEditField(resourceCode, fieldCode) {
      const fields = this.fields[resourceCode] || {}
      return fields[fieldCode]?.canEdit !== false
    }
  }
})
```

**权限指令 v-permission：**

```javascript
// directives/permission.js
export const permissionDirective = {
  mounted(el, binding) {
    const { resource, action } = binding.value
    const permissionStore = usePermissionStore()
    
    if (!permissionStore.hasAction(resource, action)) {
      el.parentNode?.removeChild(el)
    }
  }
}

// 注册指令
app.directive('permission', permissionDirective)

// 使用示例
<el-button v-permission="{ resource: 'USER', action: 'DELETE' }">
  删除
</el-button>
```

**动态路由生成：**

```javascript
// router/dynamicRoutes.js
export function addDynamicRoutes(menus) {
  const routes = menus.flatMap(menu => {
    if (menu.type === 'MENU' && menu.path) {
      return {
        path: menu.path,
        component: () => import(`@/views/${menu.component}`),
        meta: {
          title: menu.name,
          resource: menu.code,
          icon: menu.icon
        }
      }
    }
    return []
  })
  
  router.addRoute({
    path: '/',
    component: Layout,
    children: routes
  })
}

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const permissionStore = usePermissionStore()
  
  // 首屏加载权限
  if (!permissionStore.loaded) {
    await permissionStore.loadPermissions()
  }
  
  // 检查菜单权限
  if (to.meta.resource) {
    if (!permissionStore.hasMenu(to.meta.resource)) {
      next('/403')
      return
    }
  }
  
  // 检查操作权限
  if (to.meta.action) {
    if (!permissionStore.hasAction(to.meta.resource, to.meta.action)) {
      next('/403')
      return
    }
  }
  
  next()
})
```

---

## 五、数据权限实现设计

### 5.1 数据权限范围类型

| 范围类型 | 说明 | SQL条件示例 |
|----------|------|-------------|
| `ALL` | 全部数据 | 无过滤条件 |
| `SELF` | 仅本人数据 | `creator_id = :userId` |
| `SELF_DEPT` | 本部门数据 | `dept_id = :userDeptId` |
| `DEPT_TREE` | 本部门及下级 | `dept_id IN (:deptIds)` |
| `CUSTOM` | 自定义范围 | `target_id IN (:scopeValues)` |

### 5.2 DataScopeInterceptor（安全改进版）

```java
/**
 * 数据权限拦截器 - 参数化查询，防止SQL注入
 */
@Intercepts({
    @Signature(type = Executor.class, method = "query", 
               args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
@Component
public class DataScopeInterceptor implements Interceptor {
    
    private final DataScopeDomainService dataScopeService;
    
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
        Object parameter = invocation.getArgs()[1];
        
        DataScopeConfig config = getDataScopeConfig(ms);
        if (config == null) {
            return invocation.proceed();
        }
        
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return invocation.proceed();
        }
        
        UserDataScope userScope = dataScopeService.getUserDataScope(userId);
        DataScope scope = userScope.getScope(config.getDimension());
        
        if (scope == null || scope.getScopeType() == ScopeType.ALL) {
            return invocation.proceed();
        }
        
        BoundSql boundSql = ms.getBoundSql(parameter);
        String originalSql = boundSql.getSql();
        
        // 安全：构建参数化条件
        String condition = buildParameterizedCondition(scope, config);
        Map<String, Object> newParams = buildScopeParameters(scope, userId);
        
        // SQL改写（而非包装子查询，避免破坏索引）
        String newSql = rewriteSql(originalSql, condition, config);
        
        // 合并参数
        mergeParameters(boundSql, newParams);
        
        resetSql(ms, boundSql, newSql);
        return invocation.proceed();
    }
    
    /**
     * 构建参数化条件（防止SQL注入）
     */
    private String buildParameterizedCondition(DataScope scope, DataScopeConfig config) {
        String column = config.getTableAlias() != null 
            ? config.getTableAlias() + "." + config.getColumn()
            : config.getColumn();
        
        switch (scope.getScopeType()) {
            case SELF:
                return column + " = :_dataScopeUserId";
            case SELF_DEPT:
                return column + " = :_dataScopeDeptId";
            case DEPT_TREE:
                return column + " IN (:_dataScopeDeptIds)";
            case CUSTOM:
                return column + " IN (:_dataScopeValues)";
            default:
                return null;
        }
    }
    
    /**
     * 构建安全参数
     */
    private Map<String, Object> buildScopeParameters(DataScope scope, Long userId) {
        Map<String, Object> params = new HashMap<>();
        
        switch (scope.getScopeType()) {
            case SELF:
                params.put("_dataScopeUserId", userId);
                break;
            case SELF_DEPT:
                params.put("_dataScopeDeptId", getUserDeptId(userId));
                break;
            case DEPT_TREE:
                params.put("_dataScopeDeptIds", getSubDeptIds(userId));
                break;
            case CUSTOM:
                // 白名单校验scope值（传入维度编码进行存在性检查）
                params.put("_dataScopeValues", validateScopeValues(scope.getScopeValues(), config.getDimension()));
                break;
        }
        
        return params;
    }
    
    /**
     * SQL改写（避免子查询包装破坏索引）
     */
    private String rewriteSql(String originalSql, String condition, DataScopeConfig config) {
        // 检查是否已有WHERE
        if (originalSql.toUpperCase().contains("WHERE")) {
            return originalSql + " AND " + condition;
        } else {
            // 找到ORDER BY或LIMIT位置，在其前插入WHERE
            int orderByPos = originalSql.toUpperCase().indexOf("ORDER BY");
            int limitPos = originalSql.toUpperCase().indexOf("LIMIT");
            int insertPos = Math.min(
                orderByPos > 0 ? orderByPos : originalSql.length(),
                limitPos > 0 ? limitPos : originalSql.length()
            );
            
            return originalSql.substring(0, insertPos) 
                + " WHERE " + condition + " "
                + originalSql.substring(insertPos);
        }
    }
    
    /**
     * 白名单校验范围值（加强版）
     * 校验值是否存在于对应维度表中，防止非法值注入
     */
    private Set<Long> validateScopeValues(Set<Long> values, String dimensionCode) {
        if (values == null || values.isEmpty()) {
            return Collections.emptySet();
        }
        
        // 1. 基础校验：正整数
        Set<Long> validValues = values.stream()
            .filter(v -> v != null && v > 0)
            .collect(Collectors.toSet());
        
        if (validValues.isEmpty()) {
            return Collections.emptySet();
        }
        
        // 2. 维度表存在性校验（根据dimensionCode查询对应表）
        DataDimension dimension = dataDimensionRepository.findByCode(dimensionCode);
        if (dimension == null) {
            log.warn("数据维度不存在: {}", dimensionCode);
            return Collections.emptySet();
        }
        
        // 3. 根据维度source_table校验值是否存在
        String sourceTable = dimension.getSourceTable();
        String sourceColumn = dimension.getSourceColumn();
        
        // 执行存在性检查（参数化查询，防止SQL注入）
        String checkSql = String.format(
            "SELECT %s FROM %s WHERE %s IN (:values) AND is_deleted = 0",
            sourceColumn, sourceTable, sourceColumn
        );
        
        Set<Long> existingIds = jdbcTemplate.queryForList(
            checkSql, 
            Map.of("values", validValues),
            Long.class
        );
        
        // 4. 仅保留存在于维度表中的值
        Set<Long> result = new HashSet<>(existingIds);
        
        // 记录被过滤的非法值（用于审计）
        Set<Long> filtered = new HashSet<>(validValues);
        filtered.removeAll(result);
        if (!filtered.isEmpty()) {
            log.warn("数据权限范围值被过滤（不存在于维度表）: dimension={}, filtered={}", 
                dimensionCode, filtered);
            auditService.logFilteredScopeValues(dimensionCode, filtered);
        }
        
        return result;
    }
    
    /**
     * 获取用户部门ID（用于SELF_DEPT/DEPT_TREE类型）
     */
    private Long getUserDeptId(Long userId) {
        return userDimensionRepository.getValueByDimension(userId, "DEPARTMENT");
    }
    
    /**
     * 获取用户子部门ID列表（用于DEPT_TREE类型）
     */
    private Set<Long> getSubDeptIds(Long userId) {
        Long deptId = getUserDeptId(userId);
        if (deptId == null) {
            return Collections.emptySet();
        }
        return departmentRepository.findAllSubDeptIds(deptId);
    }
}
```

### 5.3 拦截器顺序控制（兼容PageHelper）

```java
@Configuration
public class MyBatisConfig {
    
    @Bean
    public DataScopeInterceptor dataScopeInterceptor() {
        return new DataScopeInterceptor();
    }
    
    /**
     * 控制拦截器顺序：PageHelper -> DataScopeInterceptor
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // PageHelper分页插件（优先）
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        
        // 数据权限拦截器（其次）
        interceptor.addInnerInterceptor(new DataScopeInnerInterceptor());
        
        return interceptor;
    }
}
```

---

## 六、字段权限实现设计

### 6.1 字段敏感级别

| 级别 | 说明 | 处理方式 |
|------|------|----------|
| `NORMAL` | 普通字段 | 原值返回 |
| `HIDDEN` | 隐藏字段 | 无权限返回 `null` |
| `ENCRYPTED` | 加密字段 | 无权限返回脱敏值 |

### 6.2 FieldPermissionService（改进版）

```java
/**
 * 字段权限服务 - 使用缓存优化反射性能
 */
public class FieldPermissionService {
    
    // 字段访问器缓存（避免每次反射）
    private final Map<String, FieldAccessor> accessorCache = new ConcurrentHashMap<>();
    
    /**
     * 处理响应数据字段权限
     */
    public <T> T processFieldPermissions(T response, Long userId) {
        String resourceCode = getResourceCode(response.getClass());
        
        // 加载用户字段权限（带缓存）
        Map<String, FieldPermission> userFieldPerms = loadUserFieldPermissions(userId, resourceCode);
        List<ResourceField> sensitiveFields = loadSensitiveFields(resourceCode);
        
        for (ResourceField field : sensitiveFields) {
            FieldPermission perm = userFieldPerms.get(field.getFieldCode());
            
            if (perm == null || !perm.canView()) {
                // 使用缓存 accessor 获取/设置字段值
                FieldAccessor accessor = getAccessor(response.getClass(), field.getFieldCode());
                Object originalValue = accessor.getValue(response);
                Object maskedValue = field.maskValue(originalValue);
                accessor.setValue(response, maskedValue);
            }
        }
        
        return response;
    }
    
    /**
     * 编辑权限校验（移至Service层）
     */
    public void validateEditPermission(Object request, Long userId, String resourceCode) {
        Map<String, FieldPermission> userFieldPerms = loadUserFieldPermissions(userId, resourceCode);
        List<ResourceField> sensitiveFields = loadSensitiveFields(resourceCode);
        
        for (ResourceField field : sensitiveFields) {
            FieldPermission perm = userFieldPerms.get(field.getFieldCode());
            FieldAccessor accessor = getAccessor(request.getClass(), field.getFieldCode());
            Object newValue = accessor.getValue(request);
            
            if (newValue != null && (perm == null || !perm.canEdit())) {
                throw new BusinessException(403, "无权限编辑字段: " + field.getFieldName());
            }
        }
    }
    
    /**
     * 获取字段访问器（缓存）
     */
    private FieldAccessor getAccessor(Class<?> clazz, String fieldCode) {
        String key = clazz.getName() + "." + fieldCode;
        return accessorCache.computeIfAbsent(key, k -> createAccessor(clazz, fieldCode));
    }
}
```

### 6.3 字段权限继承

```java
/**
 * 计算角色字段权限（含继承）
 */
public Map<String, FieldPermission> computeFieldPermissions(Role role) {
    Map<String, FieldPermission> result = new HashMap<>();
    
    // 继承父角色字段权限
    if (role.getParentId() != null) {
        Role parent = roleRepository.findById(role.getParentId());
        Map<String, FieldPermission> parentPerms = computeFieldPermissions(parent);
        result.putAll(parentPerms);
    }
    
    // 角色 own 字段权限覆盖继承
    for (FieldPermission fp : role.getFieldPerms()) {
        result.put(fp.getFieldCode(), fp);
    }
    
    return result;
}
```

---

## 七、权限缓存设计（安全改进版）

### 7.1 二级缓存架构

```
┌─────────────────────────────────────────────────────────────────────┐
│                       二级缓存架构                                    │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│                     ┌─────────────────────┐                          │
│                     │   请求检查权限      │                          │
│                     └─────────────────────┘                          │
│                              │                                       │
│                              ▼                                       │
│                     ┌─────────────────────┐                          │
│                     │  L1: Caffeine缓存   │                          │
│                     │  本地内存，秒级响应 │                          │
│                     │  TTL: 5分钟         │                          │
│                     │  容量: 1000用户     │                          │
│                     └─────────────────────┘                          │
│                              │                                       │
│                              │ 未命中                                 │
│                              ▼                                       │
│                     ┌─────────────────────┐                          │
│                     │  L2: Redis缓存      │                          │
│                     │  分布式，分钟级响应 │                          │
│                     │  TTL: 1小时         │                          │
│                     │  Key: 安全哈希      │                          │
│                     └─────────────────────┘                          │
│                              │                                       │
│                              │ 未命中                                 │
│                              ▼                                       │
│                     ┌─────────────────────┐                          │
│                     │  计算权限位图       │                          │
│                     │  写入两级缓存       │                          │
│                     └─────────────────────┘                          │
│                                                                       │
└─────────────────────────────────────────────────────────────────────┘
```

### 7.2 PermissionCacheService（安全改进版）

```java
/**
 * 权限缓存服务 - 二级缓存 + 安全Key + 版本校验
 */
public class PermissionCacheService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final Cache<Long, PermissionBitmap> localCache;  // Caffeine
    
    private static final String CACHE_SALT = "perm_salt_2026";
    private static final long LOCAL_EXPIRE = 300;   // 5分钟
    private static final long REDIS_EXPIRE = 3600;  // 1小时
    
    public PermissionCacheService() {
        this.localCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(LOCAL_EXPIRE, TimeUnit.SECONDS)
            .build();
    }
    
    /**
     * 安全Key生成（加盐哈希，防止枚举攻击）
     */
    private String safeKey(Long userId) {
        String raw = userId + ":" + CACHE_SALT;
        return "perm:bitmap:" + DigestUtils.sha256Hex(raw).substring(0, 16);
    }
    
    /**
     * 获取权限位图（二级缓存）
     */
    public PermissionBitmap getPermissionBitmap(Long userId) {
        // L1: 本地缓存
        PermissionBitmap cached = localCache.getIfPresent(userId);
        if (cached != null && !cached.isExpired()) {
            return cached;
        }
        
        // L2: Redis缓存
        String key = safeKey(userId);
        PermissionBitmap redisCached = (PermissionBitmap) redisTemplate.opsForValue().get(key);
        if (redisCached != null) {
            // 校验版本号
            if (validateVersion(redisCached.getVersion(), userId)) {
                localCache.put(userId, redisCached);
                return redisCached;
            }
            // 版本不匹配，重新计算
            redisTemplate.delete(key);
        }
        
        // 计算 + 写入缓存
        PermissionBitmap fresh = permissionDomainService.computeUserPermissionBitmap(userId);
        redisTemplate.opsForValue().set(key, fresh, REDIS_EXPIRE, TimeUnit.SECONDS);
        localCache.put(userId, fresh);
        
        return fresh;
    }
    
    /**
     * 版本号校验
     */
    private boolean validateVersion(long cachedVersion, Long userId) {
        long currentVersion = computeUserVersion(userId);
        return cachedVersion >= currentVersion;
    }
    
    /**
     * 计算用户权限版本（角色版本之和）
     */
    private long computeUserVersion(Long userId) {
        List<Role> roles = userRoleRepository.findRolesByUserId(userId);
        return roles.stream()
            .mapToLong(r -> r.getVersion())
            .sum();
    }
    
    /**
     * 清除用户权限缓存
     */
    public void clearUserPermissions(Long userId) {
        localCache.invalidate(userId);
        redisTemplate.delete(safeKey(userId));
    }
    
    /**
     * 清除角色相关用户缓存
     */
    public void clearRoleRelatedPermissions(Long roleId) {
        List<Long> userIds = userRoleRepository.findUserIdsByRoleId(roleId);
        userIds.forEach(this::clearUserPermissions);
        
        // 增加角色版本号
        roleRepository.incrementVersion(roleId);
    }
    
    /**
     * 缓存空权限（防止缓存穿透）
     */
    public PermissionBitmap getEmptyPermissionBitmap(Long userId) {
        PermissionBitmap empty = PermissionBitmap.empty(System.currentTimeMillis());
        String key = safeKey(userId);
        redisTemplate.opsForValue().set(key, empty, REDIS_EXPIRE, TimeUnit.SECONDS);
        localCache.put(userId, empty);
        return empty;
    }
}
```

---

## 八、API接口设计

### 8.1 后端API列表

```yaml
# 角色管理
POST   /api/roles              # 创建角色
PUT    /api/roles/{id}         # 更新角色
DELETE /api/roles/{id}         # 软删除角色
GET    /api/roles              # 角色列表（带继承树）
GET    /api/roles/{id}         # 角色详情（含权限配置）
GET    /api/roles/tree         # 角色继承树结构

# 角色权限配置
POST   /api/roles/{id}/permissions        # 分配功能权限
DELETE /api/roles/{id}/permissions/{permId} # 移除权限
POST   /api/roles/{id}/data-scopes        # 分配数据权限
DELETE /api/roles/{id}/data-scopes/{scopeId} # 移除数据权限
POST   /api/roles/{id}/field-permissions  # 分配字段权限

# 资源管理
POST   /api/resources              # 创建资源
PUT    /api/resources/{id}         # 更新资源
DELETE /api/resources/{id}         # 软删除资源
GET    /api/resources              # 资源列表（树形）
GET    /api/resources/tree         # 资源树（用于权限配置）
GET    /api/resources/{id}/fields  # 获取资源敏感字段

# 用户角色分配
POST   /api/users/{id}/roles       # 为用户分配角色
DELETE /api/users/{id}/roles/{roleId} # 移除用户角色
GET    /api/users/{id}/roles       # 获取用户角色列表

# 用户数据维度
POST   /api/users/{id}/dimensions      # 分配用户维度值
DELETE /api/users/{id}/dimensions/{dimId} # 移除维度值
GET    /api/users/{id}/dimensions      # 获取用户维度值列表

# 权限查询
GET    /api/auth/permissions         # 获取当前用户所有权限
GET    /api/auth/menus              # 获取当前用户菜单树
GET    /api/auth/actions            # 获取当前用户操作权限映射
GET    /api/auth/fields             # 获取当前用户字段权限映射

# 数据维度管理
GET    /api/data-dimensions         # 获取维度列表
POST   /api/data-dimensions         # 创建维度
PUT    /api/data-dimensions/{id}    # 更新维度

# 权限审计日志
GET    /api/permission-audit-logs   # 查询权限变更审计日志
```

---

## 九、测试策略设计

### 9.1 单元测试

| 测试类 | 测试内容 |
|--------|----------|
| `PermissionBitmapTest` | 权限位图合并、DENY冲突处理、位运算正确性 |
| `RoleTest` | 角色聚合根业务行为：assignPermission、继承计算 |
| `RolePermissionTest` | 值对象自验证、不可变性测试 |
| `DataScopeTest` | 数据范围值对象、参数化条件生成 |
| `PermissionCacheServiceTest` | 二级缓存、版本校验、缓存穿透防护 |

### 9.2 集成测试

| 测试场景 | 测试内容 |
|----------|----------|
| `PermissionInterceptorIT` | API权限拦截、403返回、Ant路径匹配 |
| `DataScopeInterceptorIT` | 数据权限SQL过滤、参数化查询、PageHelper兼容 |
| `FieldPermissionIT` | 字段脱敏、编辑权限校验 |
| `RoleInheritanceIT` | 角色继承链权限计算、循环继承检测 |

### 9.3 安全测试

| 测试场景 | 测试内容 |
|----------|----------|
| `SqlInjectionTest` | 数据权限参数化查询、特殊字符输入 |
| `CacheKeySecurityTest` | 缓存Key枚举攻击防护 |
| `CircularInheritanceTest` | 角色继承循环引用检测 |
| `PermissionStormTest` | 权限查询限流、并发压力测试 |

### 9.4 测试用例示例

```java
/**
 * SQL注入测试
 */
@Test
public void testSqlInjection() {
    // 模拟恶意输入
    Set<Long> maliciousValues = Set.of(1L, 2L, -1L);  // 包含非法值
    
    DataScope scope = DataScope.custom(DimensionType.DEPARTMENT, maliciousValues);
    
    // 应被白名单过滤
    Set<Long> validated = interceptor.validateScopeValues(scope.getScopeValues());
    assertThat(validated).containsOnly(1L, 2L);
    
    // 参数化查询不应拼接字符串
    String condition = interceptor.buildParameterizedCondition(scope, config);
    assertThat(condition).doesNotContain("1,2,3");
    assertThat(condition).contains(":_dataScopeValues");
}

/**
 * 权限位图合并测试
 */
@Test
public void testPermissionBitmapMerge() {
    PermissionBitmap a = PermissionBitmap.of(resource1, Set.of(VIEW, CREATE));
    PermissionBitmap b = PermissionBitmap.of(resource1, Set.of(UPDATE, DELETE));
    
    PermissionBitmap merged = a.merge(b);
    
    assertThat(merged.hasAction(resource1, VIEW)).isTrue();
    assertThat(merged.hasAction(resource1, CREATE)).isTrue();
    assertThat(merged.hasAction(resource1, UPDATE)).isTrue();
    assertThat(merged.hasAction(resource1, DELETE)).isTrue();
}

/**
 * DENY优先测试
 */
@Test
public void testDenyPriority() {
    PermissionBitmap allow = PermissionBitmap.of(resource1, Set.of(VIEW, CREATE, UPDATE));
    PermissionBitmap deny = PermissionBitmap.of(resource1, Set.of(UPDATE));
    
    PermissionBitmap result = allow.applyDeny(deny);
    
    assertThat(result.hasAction(resource1, VIEW)).isTrue();
    assertThat(result.hasAction(resource1, CREATE)).isTrue();
    assertThat(result.hasAction(resource1, UPDATE)).isFalse();  // DENY生效
}
```

---

## 十、实现计划分解

### 10.1 实现阶段（含验收测试）

|阶段 | 内容 | 优先级 | 验收测试 |
|------|------|--------|----------|
| P1 | 核心表结构 + 领域模型 | 必须 | 数据库迁移测试、聚合根单元测试 |
| P2 | 功能权限（拦截器 + 位图） | 必须 | 权限拦截器测试、403场景测试、位图合并测试 |
| P3 | 前端权限 + 动态路由 | 必须 | 路由守卫测试、按钮权限测试、指令测试 |
| P4 | 数据权限（参数化查询） | 高 | MyBatis拦截器测试、SQL注入测试 |
| P5 | 字段权限处理器 | 高 | 脱敏测试、编辑权限测试 |
| P6 | 二级缓存 + 安全Key | 高 | 缓存测试、版本校验测试 |
| P7 | 角色管理 API + 前端 | 中 | CRUD测试、继承测试 |
| P8 | 资源管理 API + 前端 | 中 | CRUD测试、树形结构测试 |
| P9 | 权限审计日志 | 中 | 审计记录测试 |
| P10 | 预设数据 + 初始化 | 低 | 数据完整性测试 |

### 10.2 数据库迁移版本

```sql
-- V1__Init_RBAC_Tables.sql
-- 创建所有权限相关表

-- V2__Add_Soft_Delete.sql
-- 添加软删除字段、版本号字段

-- V3__Split_Action_Table.sql
-- 拆分permission_action表

-- V4__Add_Audit_Log.sql
-- 创建权限审计表

-- Rollback策略：每个版本对应回滚脚本
-- V1__Rollback.sql、V2__Rollback.sql...
```

---

## 十一、风险评估与缓解

### 11.1 技术风险

| 风险 | 影响等级 | 缓解措施 |
|------|----------|----------|
| SQL注入 | 高 | 参数化查询 + 白名单校验 |
| 权限计算性能 | 高 | 权限位图预计算 + 二级缓存 |
| 缓存Key枚举 | 高 | 加盐哈希Key |
| 权限风暴攻击 | 高 | 权限查询限流（RateLimit） |
| 角色继承循环 | 中 | 代码校验 + 数据库约束 |
| 缓存穿透 | 中 | 空权限用户也缓存 |
| 缓存一致性延迟 | 中 | 版本号校验 + Kafka事件 |
| 权限表膨胀 | 低 | 定期归档 + 权限压缩策略 |

### 11.2 业务风险

| 风险 | 影响等级 | 缓解措施 |
|------|----------|----------|
| 权限委托缺失 | 中 | 后续迭代添加PermissionDelegation表 |
| 超级管理员误删 | 高 | is_builtin保护 + 软删除 |
| 权限配置错误 | 中 | 权限审计日志 + 配置变更确认 |

---

## 十二、验收标准

### 12.1 功能验收

- [ ] 用户登录后可获取权限列表（菜单、操作、字段）
- [ ] 无权限访问 API 返回 403
- [ ] 无权限菜单不在导航显示
- [ ] 无权限按钮不渲染（v-permission指令）
- [ ] 数据权限正确过滤查询结果
- [ ] 敏感字段无权限时正确脱敏/隐藏
- [ ] 角色继承权限正确计算
- [ ] 多角色权限正确合并
- [ ] 权限变更后缓存正确刷新
- [ ] 权限审计日志正确记录

### 12.2 性能验收

- [ ] 权限加载时间 < 50ms（本地缓存）
- [ ] 权限加载时间 < 100ms（Redis缓存）
- [ ] 权限检查不影响 API响应时间（< 5ms增量）
- [ ] 数据权限 SQL过滤不影响查询性能（< 10%增量）
- [ ] 100并发权限查询成功率 > 99%

### 12.3 安全验收

- [ ] 无SQL注入漏洞（安全测试通过）
- [ ] 缓存Key不可枚举
- [ ] 角色继承循环检测生效
- [ ] 权限查询限流生效
- [ ] 软删除数据不可访问

---

## 十三、附录

### 13.1 参考资料

- NIST RBAC Standard (INCITS 359-2004)
- Spring Security Architecture
- MyBatis Interceptor Documentation
- Caffeine Cache Documentation

### 13.2 术语表

| 术语 | 定义 |
|------|------|
| RBAC | Role-Based Access Control，基于角色的访问控制 |
| 聚合根 | DDD中一组相关对象的根，控制访问边界 |
| 值对象 | 无唯一标识、不可变、自验证的对象 |
| 权限位图 | 使用BitSet高效存储权限状态的数据结构 |
| 数据维度 | 数据权限隔离的分类维度（如部门、项目） |
| 敏感字段 | 需要特殊权限才能查看/编辑的字段 |
| 继承模式 | EXTEND(继承并扩展) 或 LIMIT(继承并限制) |
| 二级缓存 | L1本地缓存 + L2分布式缓存的组合策略 |

### 13.3 版本历史

| 版本 | 日期 | 变更内容 |
|------|------|----------|
| 1.0 | 2026-04-14 | 初版设计 |
| 2.0 | 2026-04-14 | 根据评审意见修订：修复SQL注入、添加权限位图、二级缓存、测试策略等 |
| 2.1 | 2026-04-14 | P1优化：数据库触发器防止循环继承、白名单校验加强（维度表存在性检查）、Role聚合根微调（dataScopes改Set、权限位图计算移至领域服务） |