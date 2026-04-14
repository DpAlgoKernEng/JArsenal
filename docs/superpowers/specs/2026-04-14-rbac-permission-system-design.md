# RBAC 权限管理系统设计规范

> 文档版本：1.0
> 创建日期：2026-04-14
> 适用项目：JArsenal (Spring Boot 3.x + Vue 3)

---

## 一、概述

### 1.1 设计目标

为 JArsenal 项目设计完整的企业级 RBAC 权限管理系统，支持：

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
| 缓存 | Redis | 权限缓存存储 |
| 前端 | Vue 3 + Element Plus + Pinia | 权限状态管理 + 路由守卫 |

### 1.3 设计方案

采用**标准扩展 RBAC**方案：
- Role-Permission-Resource 核心模型
- 数据权限用独立维度表实现
- 字段权限用字段配置表实现
- 自然融入现有 DDD 领域模型

---

## 二、领域模型设计

### 2.1 核心聚合根

```
┌─────────────────────────────────────────────────────────────┐
│                    Permission Domain                         │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────┐     ┌─────────────────┐               │
│  │      Role       │     │    Resource     │               │
│  │  (Aggregate)    │     │  (Aggregate)    │               │
│  ├─────────────────┤     ├─────────────────┤               │
│  │ - roleId        │     │ - resourceId    │               │
│  │ - roleCode      │     │ - resourceCode  │               │
│  │ - roleName      │     │ - resourceName  │               │
│  │ - parentId      │────▶│ - parentId      │               │
│  │ - status        │     │ - type          │               │
│  │ - permissions[] │     │ - path          │               │
│  │ - dataScopes[]  │     │ - method        │               │
│  └─────────────────┘     │ - sort          │               │
│          │               └─────────────────┘               │
│          │                        │                        │
│          │               ┌─────────────────┐               │
│          │               │  ResourceField  │               │
│          │               │    (Entity)     │               │
│          │               ├─────────────────┤               │
│          │               │ - fieldId       │               │
│          └──────────────▶│ - fieldCode     │               │
│                          │ - fieldName     │               │
│                          │ - sensitive     │               │
│                          └─────────────────┘               │
│                                                             │
│  ┌─────────────────┐     ┌─────────────────┐               │
│  │   Permission    │     │   DataScope     │               │
│  │    (Entity)     │     │    (Entity)     │               │
│  ├─────────────────┤     ├─────────────────┤               │
│  │ - permId        │     │ - scopeId       │               │
│  │ - permCode      │     │ - dimensionType │               │
│  │ - resourceId    │     │ - dimensionId   │               │
│  │ - actions[]     │     │ - scopeValue    │               │
│  └─────────────────┘     └─────────────────┘               │
│                                                             │
│  ┌─────────────────┐                                       │
│  │    UserRole     │                                       │
│  │    (Entity)     │                                       │
│  ├─────────────────┤                                       │
│  │ - userId        │                                       │
│  │ - roleIds[]     │                                       │
│  └─────────────────┘                                       │
│                                                             │
└─────────────────────────────────────────────────────────────┘
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
| `SensitiveLevel` | 字段敏感级别 | NORMAL(普通)、HIDDEN(隐藏)、ENCRYPTED(加密) |
| `ScopeType` | 数据范围类型 | ALL(全部)、SELF(仅本人)、SELF_DEPT(本部门)、DEPT_TREE(本部门及下级)、CUSTOM(自定义) |

### 2.3 聚合根业务行为

**Role 聚合根：**

```java
public class Role extends BaseEntity<RoleId> {
    private RoleCode code;
    private String name;
    private RoleId parentId;        // 父角色（继承）
    private RoleStatus status;
    private List<Permission> permissions;    // 功能权限
    private List<RoleDataScope> dataScopes;  // 数据权限
    private PermissionEffect inheritEffect;  // 继承效果：ALLOW/DENY
    
    // 工厂方法
    public static Role create(RoleCode code, String name, RoleId parentId);
    
    // 业务行为
    public void assignPermission(ResourceId resource, Set<ActionType> actions);
    public void removePermission(ResourceId resource);
    public void assignDataScope(DimensionType dimension, Long dimensionId, String scopeValue);
    public void enable();
    public void disable();
    public void setInheritEffect(PermissionEffect effect);  // 设置继承效果
    
    // 计算有效权限（含继承）
    public Set<Permission> computeEffectivePermissions(RoleRepository repo);
}
```

**Resource 聚合根：**

```java
public class Resource extends BaseEntity<ResourceId> {
    private ResourceCode code;
    private String name;
    private ResourceId parentId;
    private ResourceType type;      // MENU/OPERATION/API
    private String path;            // 菜单路由或API路径
    private String method;          // API方法（GET/POST/PUT/DELETE）
    private Integer sort;
    private List<ResourceField> sensitiveFields;  // 敏感字段
    
    // 工厂方法
    public static Resource createMenu(...);
    public static Resource createOperation(...);
    public static Resource createApi(...);
    
    // 业务行为
    public void addSensitiveField(String fieldCode, String fieldName);
    public void updatePath(String newPath);
}
```

### 2.4 领域服务

| 服务 | 职责 |
|------|------|
| `PermissionDomainService` | 计算用户有效权限（多角色合并 + 继承计算） |
| `DataScopeDomainService` | 计算用户数据权限范围（多维度合并） |
| `RoleHierarchyService` | 角色继承树遍历，计算继承链权限 |

### 2.5 领域事件

| 事件 | 触发时机 | 处理逻辑 |
|------|----------|----------|
| `RoleCreated` | 创建角色 | 记录审计日志 |
| `RolePermissionChanged` | 角色权限变更 | 清除相关用户权限缓存 |
| `UserRoleAssigned` | 用户分配角色 | 清除用户权限缓存 |
| `ResourceCreated` | 创建资源 | 初始化权限树 |

---

## 三、数据库表结构设计

### 3.1 核心表设计

```sql
-- 角色表（支持继承树）
CREATE TABLE role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    name VARCHAR(100) NOT NULL COMMENT '角色名称',
    parent_id BIGINT COMMENT '父角色ID（继承）',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    inherit_effect VARCHAR(10) DEFAULT 'ALLOW' COMMENT '继承效果：ALLOW/DENY',
    is_builtin TINYINT DEFAULT 0 COMMENT '是否内置角色',
    sort INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_parent_id (parent_id),
    INDEX idx_code (code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 资源表（菜单/操作/API三级）
CREATE TABLE resource (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(100) NOT NULL UNIQUE COMMENT '资源编码',
    name VARCHAR(100) NOT NULL COMMENT '资源名称',
    parent_id BIGINT COMMENT '父资源ID',
    type VARCHAR(20) NOT NULL COMMENT '类型：MENU/OPERATION/API',
    path VARCHAR(200) COMMENT '路径（菜单路由或API路径）',
    method VARCHAR(10) COMMENT 'API方法：GET/POST/PUT/DELETE',
    icon VARCHAR(50) COMMENT '菜单图标',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_parent_id (parent_id),
    INDEX idx_type (type),
    INDEX idx_path_method (path, method)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源表';

-- 权限表（角色-资源-操作关联）
CREATE TABLE permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL COMMENT '角色ID',
    resource_id BIGINT NOT NULL COMMENT '资源ID',
    actions VARCHAR(100) NOT NULL COMMENT '操作列表：VIEW,CREATE,UPDATE,DELETE',
    effect VARCHAR(10) DEFAULT 'ALLOW' COMMENT '效果：ALLOW/DENY',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_resource (role_id, resource_id),
    INDEX idx_role_id (role_id),
    INDEX idx_resource_id (resource_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

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
    field_code VARCHAR(50) NOT NULL COMMENT '字段编码',
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

### 3.2 数据权限表设计

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

-- 角色数据权限表
CREATE TABLE role_data_scope (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL COMMENT '角色ID',
    dimension_code VARCHAR(50) NOT NULL COMMENT '维度编码',
    scope_type VARCHAR(20) NOT NULL COMMENT '范围类型：ALL/SELF/SELF_DEPT/DEPT_TREE/CUSTOM',
    scope_values VARCHAR(500) COMMENT '自定义范围值（逗号分隔ID）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_dimension (role_id, dimension_code),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色数据权限表';

-- 用户维度关联表（用户跨维度场景）
CREATE TABLE user_dimension (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    dimension_code VARCHAR(50) NOT NULL COMMENT '维度编码',
    dimension_value_id BIGINT NOT NULL COMMENT '维度值ID（如部门ID、项目ID）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_dimension (user_id, dimension_code, dimension_value_id),
    INDEX idx_user_id (user_id),
    INDEX idx_dimension (dimension_code, dimension_value_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户维度关联表';
```

### 3.3 预设数据

```sql
-- 内置角色
INSERT INTO role (code, name, parent_id, is_builtin, sort) VALUES
('SUPER_ADMIN', '超级管理员', NULL, 1, 1),
('ADMIN', '系统管理员', 1, 1, 2),
('DEPT_MANAGER', '部门管理员', 2, 1, 3),
('USER', '普通用户', 3, 1, 4);

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

### 3.4 表关系图

```
user ────────────┐
                 │
                 ▼
           user_role ──────────────▶ role
                                        │
                                        │ parent_id (自关联继承)
                                        │
                                        ├──▶ permission ──▶ resource
                                        │                         │
                                        │                         ▼
                                        │                   resource_field
                                        │                         │
                                        │                         ▼
                                        └──▶ field_permission ◀───┘
                                        │
                                        └──▶ role_data_scope ──▶ data_dimension
                                                                  │
                                                                  ▼
                                                            user_dimension
```

---

## 四、权限检查流程设计

### 4.1 功能权限检查流程

```
┌─────────────────────────────────────────────────────────────────┐
│                     功能权限检查流程                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  用户请求 ──▶ AuthInterceptor ──▶ PermissionInterceptor        │
│                                      │                          │
│                                      ▼                          │
│                         ┌─────────────────────┐                 │
│                         │ 从JWT获取userId     │                 │
│                         │ 从缓存加载用户权限   │                 │
│                         └─────────────────────┘                 │
│                                      │                          │
│                                      ▼                          │
│                         ┌─────────────────────┐                 │
│                         │ 匹配请求资源        │                 │
│                         │ (API路径+方法)      │                 │
│                         └─────────────────────┘                 │
│                                      │                          │
│                           ┌──────────┴──────────┐               │
│                           │                     │               │
│                           ▼                     ▼               │
│                      ┌─────────┐           ┌─────────┐          │
│                      │ 匹配成功 │           │ 匹配失败 │          │
│                      │ ALLOW   │           │ DENY    │          │
│                      └────┬────┘           └────┬────┘          │
│                           │                     │               │
│                           ▼                     ▼               │
│                      继续执行              返回403              │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 4.2 权限计算核心逻辑

**多角色权限合并（取并集）：**

```java
public class PermissionDomainService {
    
    /**
     * 计算用户有效权限
     * 规则：多角色取并集 + 继承链计算 + DENY优先
     */
    public UserPermission computeUserPermissions(Long userId) {
        // 1. 获取用户所有角色
        List<Role> roles = userRoleRepository.findRolesByUserId(userId);
        
        // 2. 计算每个角色的有效权限（含继承）
        Set<Permission> allPermissions = new HashSet<>();
        for (Role role : roles) {
            Set<Permission> rolePerms = computeRoleEffectivePermissions(role);
            allPermissions.addAll(rolePerms);
        }
        
        // 3. 处理冲突：DENY优先
        Map<ResourceId, PermissionEffect> effective = resolveConflicts(allPermissions);
        
        // 4. 构建权限缓存
        return UserPermission.build(userId, effective);
    }
    
    /**
     * 计算角色有效权限（含继承链）
     */
    private Set<Permission> computeRoleEffectivePermissions(Role role) {
        Set<Permission> permissions = new HashSet<>();
        
        // 递归获取父角色权限
        if (role.getParentId() != null) {
            Role parent = roleRepository.findById(role.getParentId());
            Set<Permission> parentPerms = computeRoleEffectivePermissions(parent);
            
            // 根据inheritEffect决定继承方式
            if (role.getInheritEffect() == PermissionEffect.ALLOW) {
                permissions.addAll(parentPerms);
            } else {
                permissions.addAll(parentPerms);
                permissions.removeAll(role.getDeniedPermissions());
            }
        }
        
        permissions.addAll(role.getPermissions());
        return permissions;
    }
}
```

### 4.3 权限拦截器设计

**PermissionInterceptor：**

```java
@Component
public class PermissionInterceptor implements HandlerInterceptor {
    
    private final PermissionCache permissionCache;
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                             HttpServletResponse response,
                             Object handler) throws Exception {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return true;
        }
        
        UserPermission userPerm = permissionCache.get(userId);
        if (userPerm == null) {
            userPerm = permissionDomainService.computeUserPermissions(userId);
            permissionCache.put(userId, userPerm);
        }
        
        String path = request.getRequestURI();
        String method = request.getMethod();
        ResourceId resourceId = resourceMatcher.match(path, method);
        
        if (resourceId == null) {
            return true;
        }
        
        PermissionEffect effect = userPerm.getEffect(resourceId);
        if (effect == PermissionEffect.DENY) {
            sendError(response, 403, "无访问权限");
            return false;
        }
        
        return true;
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
```

**PermissionAspect 切面：**

```java
@Aspect
@Component
public class PermissionAspect {
    
    @Around("@annotation(requirePermission)")
    public Object checkPermission(ProceedingJoinPoint pjp, 
                                  RequirePermission requirePermission) {
        Long userId = UserContext.getCurrentUserId();
        UserPermission userPerm = permissionCache.get(userId);
        
        ResourceId resourceId = resourceRepository.findByCode(
            requirePermission.resourceCode()
        ).getId();
        
        for (ActionType action : requirePermission.actions()) {
            if (!userPerm.hasAction(resourceId, action)) {
                throw new BusinessException(403, requirePermission.message());
            }
        }
        
        return pjp.proceed();
    }
}
```

### 4.5 前端权限检查

**Pinia 权限状态管理：**

```javascript
export const usePermissionStore = defineStore('permission', {
  state: () => ({
    permissions: [],
    menus: [],
    actions: {},
    fields: {}
  }),
  
  actions: {
    async loadPermissions() {
      const res = await api.get('/api/auth/permissions');
      this.permissions = res.data.permissions;
      this.menus = buildMenuTree(res.data.menus);
      this.actions = res.data.actions;
      this.fields = res.data.fields;
    },
    
    hasMenu(menuCode) {
      return this.menus.some(m => m.code === menuCode);
    },
    
    hasAction(resourceCode, action) {
      const actions = this.actions[resourceCode] || [];
      return actions.includes(action);
    },
    
    canViewField(resourceCode, fieldCode) {
      const fields = this.fields[resourceCode] || {};
      return fields[fieldCode]?.canView !== false;
    }
  }
});
```

---

## 五、数据权限实现设计

### 5.1 数据权限范围类型

| 范围类型 | 说明 | SQL条件示例 |
|----------|------|-------------|
| `ALL` | 全部数据 | 无过滤条件 |
| `SELF` | 仅本人数据 | `creator_id = current_user` |
| `SELF_DEPT` | 本部门数据 | `dept_id = user.dept_id` |
| `DEPT_TREE` | 本部门及下级 | `dept_id IN (子部门ID列表)` |
| `CUSTOM` | 自定义范围 | `target_id IN (scope_values)` |

### 5.2 数据权限计算流程

```
┌─────────────────────────────────────────────────────────────────┐
│                    数据权限计算流程                               │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  用户请求 ──▶ DataScopeInterceptor ──▶ MyBatis拦截器            │
│                                        │                        │
│                                        ▼                        │
│                        ┌─────────────────────┐                  │
│                        │ 获取用户所有角色     │                  │
│                        │ 加载各维度数据权限   │                  │
│                        └─────────────────────┘                  │
│                                        │                        │
│                                        ▼                        │
│                        ┌─────────────────────┐                  │
│                        │ 合并多维度权限范围   │                  │
│                        │ (取并集)            │                  │
│                        └─────────────────────┘                  │
│                                        │                        │
│                                        ▼                        │
│                        ┌─────────────────────┐                  │
│                        │ 生成SQL过滤条件      │                  │
│                        │ 拼接到查询语句       │                  │
│                        └─────────────────────┘                  │
│                                        │                        │
│                                        ▼                        │
│                                   执行带过滤的查询               │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 5.3 DataScope 值对象

```java
public class DataScope {
    private DimensionType dimension;
    private ScopeType scopeType;
    private Set<Long> scopeValues;
    
    public String toSqlCondition(String columnName, Long userId) {
        switch (scopeType) {
            case ALL:
                return null;
            case SELF:
                return columnName + " = " + userId;
            case SELF_DEPT:
                return columnName + " = " + getUserDeptId(userId);
            case DEPT_TREE:
                return columnName + " IN (" + join(getSubDeptIds(userId)) + ")";
            case CUSTOM:
                return columnName + " IN (" + join(scopeValues) + ")";
        }
    }
}
```

### 5.4 UserDataScope 聚合

```java
public class UserDataScope {
    private Long userId;
    private Map<DimensionType, DataScope> scopes;
    
    public static UserDataScope merge(List<Role> roles) {
        UserDataScope result = new UserDataScope();
        for (Role role : roles) {
            for (RoleDataScope rds : role.getDataScopes()) {
                DimensionType dim = rds.getDimension();
                DataScope existing = result.scopes.get(dim);
                
                if (existing == null) {
                    result.scopes.put(dim, rds.toDataScope());
                } else {
                    result.scopes.put(dim, mergeScopes(existing, rds.toDataScope()));
                }
            }
        }
        return result;
    }
    
    private static DataScope mergeScopes(DataScope a, DataScope b) {
        if (a.getScopeType() == ScopeType.ALL || b.getScopeType() == ScopeType.ALL) {
            return DataScope.all(a.getDimension());
        }
        Set<Long> merged = new HashSet<>(a.getScopeValues());
        merged.addAll(b.getScopeValues());
        return DataScope.custom(a.getDimension(), merged);
    }
}
```

### 5.5 MyBatis拦截器

**DataScopeInterceptor：**

```java
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
        String condition = scope.toSqlCondition(config.getColumn(), userId);
        String newSql = "SELECT * FROM (" + originalSql + ") temp WHERE " + condition;
        
        resetSql(ms, boundSql, newSql);
        return invocation.proceed();
    }
}
```

### 5.6 Mapper注解配置

**@DataScopeConfig 注解：**

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataScopeConfig {
    DimensionType dimension();
    String column();
    String tableAlias() default "";
}
```

**使用示例：**

```java
public interface UserMapper {
    
    @DataScopeConfig(dimension = DimensionType.DEPARTMENT, column = "dept_id")
    List<User> selectByDepartment(@Param("deptId") Long deptId);
    
    @DataScopeConfig(dimension = DimensionType.PROJECT, column = "project_id")
    List<Task> selectProjectTasks(@Param("projectId") Long projectId);
    
    User selectById(@Param("id") Long id);
}
```

### 5.7 多维度组合示例

```
用户角色：部门经理 + 项目负责人

角色1(部门经理)数据权限：
  - DEPARTMENT: DEPT_TREE (本部门及下级)
  
角色2(项目负责人)数据权限：
  - PROJECT: CUSTOM (项目ID: 101,102,103)

合并后用户数据权限：
  - DEPARTMENT: DEPT_TREE → 可见部门：[1,2,3,4]
  - PROJECT: CUSTOM → 可见项目：[101,102,103]

查询任务列表时：
  - 按部门维度过滤：WHERE dept_id IN (1,2,3,4)
  - 按项目维度过滤：WHERE project_id IN (101,102,103)
  - 组合过滤：WHERE dept_id IN (...) AND project_id IN (...)
```

---

## 六、字段权限实现设计

### 6.1 字段敏感级别

| 级别 | 说明 | 处理方式 |
|------|------|----------|
| `NORMAL` | 普通字段 | 原值返回 |
| `HIDDEN` | 隐藏字段 | 无权限返回 `null` |
| `ENCRYPTED` | 加密字段 | 无权限返回脱敏值 |

### 6.2 字段权限检查流程

```
┌─────────────────────────────────────────────────────────────────┐
│                    字段权限处理流程                               │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  查询返回数据 ──▶ FieldPermissionHandler                        │
│                            │                                    │
│                            ▼                                    │
│                 ┌─────────────────────┐                          │
│                 │ 加载用户字段权限     │                          │
│                 └─────────────────────┘                          │
│                            │                                    │
│                            ▼                                    │
│                 ┌─────────────────────┐                          │
│                 │ 遍历响应字段        │                          │
│                 │ 检查敏感级别        │                          │
│                 └─────────────────────┘                          │
│                            │                                    │
│              ┌─────────────┴─────────────┐                      │
│              │                           │                      │
│              ▼                           ▼                      │
│         ┌─────────┐                 ┌─────────┐                 │
│         │ NORMAL  │                 │ 敏感字段 │                 │
│         │ 直接返回 │                 │          │                 │
│         └─────────┘                 └─────────┘                 │
│                                          │                      │
│                          ┌───────────────┴───────────────┐      │
│                          │                               │      │
│                          ▼                               ▼      │
│                    ┌───────────┐                   ┌───────────┐ │
│                    │ 有权限    │                   │ 无权限    │ │
│                    │ 原值返回  │                   │           │ │
│                    └───────────┘                   └───────────┘ │
│                                                          │      │
│                                         ┌────────────────┴────┐│
│                                         │                     ││
│                                         ▼                     ▼│
│                                  ┌───────────┐         ┌───────────┐│
│                                  │ HIDDEN    │         │ ENCRYPTED ││
│                                  │ 设置null  │         │ 脱敏处理  ││
│                                  └───────────┘         └───────────┘│
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 6.3 ResourceField 实体

```java
public class ResourceField {
    private Long fieldId;
    private Long resourceId;
    private String fieldCode;
    private String fieldName;
    private SensitiveLevel level;
    private String maskPattern;
    
    public Object maskValue(Object value) {
        if (value == null) return null;
        
        String strValue = String.valueOf(value);
        
        if (level == SensitiveLevel.HIDDEN) {
            return null;
        }
        
        if (level == SensitiveLevel.ENCRYPTED) {
            return applyMaskPattern(strValue, maskPattern);
        }
        
        return value;
    }
    
    private String applyMaskPattern(String value, String pattern) {
        switch (pattern) {
            case "ID_CARD": 
                return value.substring(0, 3) + "***" + value.substring(value.length() - 4);
            case "PHONE": 
                return value.substring(0, 3) + "****" + value.substring(value.length() - 4);
            case "SALARY": 
                return "****";
            default: 
                return "****";
        }
    }
}
```

### 6.4 FieldPermissionService

```java
public class FieldPermissionService {
    
    public <T> T processFieldPermissions(T response, Long userId) {
        String resourceCode = getResourceCode(response.getClass());
        Map<String, FieldPermission> userFieldPerms = loadUserFieldPermissions(userId, resourceCode);
        List<ResourceField> sensitiveFields = loadSensitiveFields(resourceCode);
        
        for (ResourceField field : sensitiveFields) {
            FieldPermission perm = userFieldPerms.get(field.getFieldCode());
            
            if (perm == null || !perm.canView()) {
                Object maskedValue = field.maskValue(getFieldValue(response, field));
                setFieldValue(response, field, maskedValue);
            }
        }
        
        return response;
    }
    
    public void validateEditPermission(Object request, Long userId, String resourceCode) {
        Map<String, FieldPermission> userFieldPerms = loadUserFieldPermissions(userId, resourceCode);
        List<ResourceField> sensitiveFields = loadSensitiveFields(resourceCode);
        
        for (ResourceField field : sensitiveFields) {
            FieldPermission perm = userFieldPerms.get(field.getFieldCode());
            Object newValue = getFieldValue(request, field);
            
            if (newValue != null && (perm == null || !perm.canEdit())) {
                throw new BusinessException(403, "无权限编辑字段: " + field.getFieldName());
            }
        }
    }
}
```

### 6.5 ResponseBodyAdvice 整合

```java
@RestControllerAdvice
public class FieldPermissionResponseAdvice implements ResponseBodyAdvice<Object> {
    
    private final FieldPermissionService fieldPermissionService;
    
    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return returnType.hasMethodAnnotation(FieldPermission.class);
    }
    
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType,
                                   MediaType selectedContentType,
                                   Class selectedConverterType,
                                   ServerHttpRequest request,
                                   ServerHttpResponse response) {
        if (body == null) return null;
        
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) return body;
        
        return fieldPermissionService.processFieldPermissions(body, userId);
    }
}
```

### 6.6 Controller 使用示例

```java
@RestController
@RequestMapping("/api/users")
public class UserQueryController {
    
    @FieldPermission(resource = "USER")
    @GetMapping("/{id}")
    public Result<UserResponse> getUser(@PathVariable Long id) {
        UserResponse user = userApplicationService.getUserById(id);
        return Result.success(user);
    }
    
    @PostMapping
    public Result<Void> createUser(@RequestBody UserCreateRequest request) {
        fieldPermissionService.validateEditPermission(request, 
            UserContext.getCurrentUserId(), "USER");
        userApplicationService.createUser(request);
        return Result.success();
    }
}
```

### 6.7 前端字段权限控制

```vue
<template>
  <el-table :data="users">
    <el-table-column prop="username" label="用户名" />
    <el-table-column prop="email" label="邮箱" />
    
    <!-- 薪资字段 - 根据权限显示 -->
    <el-table-column 
      v-if="canViewField('USER', 'salary')"
      prop="salary" 
      label="薪资"
    />
    
    <!-- 身份证字段 - 显示脱敏值 -->
    <el-table-column prop="idCard" label="身份证">
      <template #default="{ row }">
        {{ row.idCard || '***无权限查看***' }}
      </template>
    </el-table-column>
    
    <!-- 编辑按钮 - 根据字段编辑权限 -->
    <el-table-column label="操作">
      <template #default="{ row }">
        <el-button 
          v-if="canEditField('USER', 'salary')"
          @click="editSalary(row)"
        >
          编辑薪资
        </el-button>
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup>
import { usePermissionStore } from '@/stores/permission'

const permissionStore = usePermissionStore()

const canViewField = (resource, field) => {
  return permissionStore.canViewField(resource, field)
}

const canEditField = (resource, field) => {
  const fields = permissionStore.fields[resource] || {}
  return fields[field]?.canEdit !== false
}
</script>
```

### 6.8 字段权限配置示例

```sql
-- 定义用户资源的敏感字段
INSERT INTO resource_field (resource_id, field_code, field_name, sensitive_level, mask_pattern) VALUES
(1, 'salary', '薪资', 'HIDDEN', NULL),
(1, 'id_card', '身份证号', 'ENCRYPTED', 'ID_CARD'),
(1, 'phone', '手机号', 'ENCRYPTED', 'PHONE');

-- 超级管理员可查看编辑所有字段
INSERT INTO field_permission (role_id, field_id, can_view, can_edit) VALUES
(1, 1, 1, 1),
(1, 2, 1, 1),
(1, 3, 1, 1);

-- 部门经理可查看但不能编辑薪资
INSERT INTO field_permission (role_id, field_id, can_view, can_edit) VALUES
(3, 1, 1, 0);
```

---

## 七、API接口设计

### 7.1 后端API列表

```yaml
# 角色管理
POST   /api/roles              # 创建角色
PUT    /api/roles/{id}         # 更新角色
DELETE /api/roles/{id}         # 删除角色
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
POST   /api/resources              # 创建资源（菜单/操作/API）
PUT    /api/resources/{id}         # 更新资源
DELETE /api/resources/{id}         # 删除资源
GET    /api/resources              # 资源列表（树形）
GET    /api/resources/tree         # 资源树（用于权限配置UI）
GET    /api/resources/{id}/fields  # 获取资源敏感字段

# 用户角色分配
POST   /api/users/{id}/roles       # 为用户分配角色
DELETE /api/users/{id}/roles/{roleId} # 移除用户角色
GET    /api/users/{id}/roles       # 获取用户角色列表

# 用户数据维度
POST   /api/users/{id}/dimensions      # 分配用户维度值
DELETE /api/users/{id}/dimensions/{dimId} # 移除维度值
GET    /api/users/{id}/dimensions      # 获取用户维度值列表

# 权限查询（前端初始化用）
GET    /api/auth/permissions         # 获取当前用户所有权限
GET    /api/auth/menus              # 获取当前用户菜单树
GET    /api/auth/actions            # 获取当前用户操作权限映射
GET    /api/auth/fields             # 获取当前用户字段权限映射

# 数据维度管理
GET    /api/data-dimensions         # 获取维度列表
POST   /api/data-dimensions         # 创建维度
PUT    /api/data-dimensions/{id}    # 更新维度
```

### 7.2 DTO设计

**RoleCreateRequest：**

```java
public class RoleCreateRequest {
    @NotBlank
    private String code;
    @NotBlank
    private String name;
    private Long parentId;
    private PermissionEffect inheritEffect;
}
```

**AssignPermissionRequest：**

```java
public class AssignPermissionRequest {
    @NotNull
    private Long resourceId;
    @NotEmpty
    private Set<ActionType> actions;
}
```

**AssignDataScopeRequest：**

```java
public class AssignDataScopeRequest {
    @NotBlank
    private String dimensionCode;
    @NotBlank
    private String scopeType;
    private Set<Long> scopeValues;
}
```

**UserPermissionResponse：**

```java
public class UserPermissionResponse {
    private List<MenuVO> menus;
    private Map<String, Set<String>> actions;
    private Map<String, Map<String, FieldPermVO>> fields;
}
```

---

## 八、前端页面设计

### 8.1 页面结构

```
views/
├── permission/
│   ├── RoleList.vue          # 角色管理列表
│   ├── RoleEdit.vue          # 角色编辑（含权限配置）
│   ├── RoleTree.vue          # 角色继承树可视化
│   ├── ResourceList.vue      # 资源管理列表
│   ├── ResourceTree.vue      # 资源树配置
│   ├── FieldConfig.vue       # 敏感字段配置
│   ├── UserRoleAssign.vue    # 用户角色分配
│   ├── DataScopeConfig.vue   # 数据权限配置
│   └── DimensionManage.vue   # 数据维度管理
└── system/
    └── MenuManage.vue        # 菜单管理（与Resource整合）
```

### 8.2 路由配置

```javascript
const permissionRoutes = [
  {
    path: '/permission',
    component: Layout,
    meta: { title: '权限管理', icon: 'lock' },
    children: [
      {
        path: 'roles',
        component: () => import('@/views/permission/RoleList.vue'),
        meta: { title: '角色管理', resource: 'ROLE', action: 'VIEW' }
      },
      {
        path: 'roles/:id/edit',
        component: () => import('@/views/permission/RoleEdit.vue'),
        meta: { title: '编辑角色', resource: 'ROLE', action: 'UPDATE', hidden: true }
      },
      {
        path: 'resources',
        component: () => import('@/views/permission/ResourceList.vue'),
        meta: { title: '资源管理', resource: 'RESOURCE', action: 'VIEW' }
      },
      {
        path: 'user-roles',
        component: () => import('@/views/permission/UserRoleAssign.vue'),
        meta: { title: '用户角色', resource: 'USER_ROLE', action: 'VIEW' }
      }
    ]
  }
]

router.beforeEach(async (to, from, next) => {
  const permissionStore = usePermissionStore()
  
  if (to.meta.resource) {
    if (!permissionStore.hasMenu(to.meta.resource)) {
      next('/403')
      return
    }
  }
  
  if (to.meta.action) {
    if (!permissionStore.hasAction(to.meta.resource, to.meta.action)) {
      next('/403')
      return
    }
  }
  
  next()
})
```

### 8.3 RoleEdit.vue 核心设计

```vue
<template>
  <el-tabs v-model="activeTab">
    <!-- 基本信息 -->
    <el-tab-pane label="基本信息" name="basic">
      <el-form :model="roleForm">
        <el-form-item label="角色编码">
          <el-input v-model="roleForm.code" :disabled="roleForm.isBuiltin" />
        </el-form-item>
        <el-form-item label="角色名称">
          <el-input v-model="roleForm.name" />
        </el-form-item>
        <el-form-item label="父角色">
          <el-tree-select
            v-model="roleForm.parentId"
            :data="roleTree"
            check-strictly
            placeholder="选择父角色（继承权限）"
          />
        </el-form-item>
        <el-form-item label="继承效果">
          <el-radio-group v-model="roleForm.inheritEffect">
            <el-radio label="ALLOW">继承并扩展</el-radio>
            <el-radio label="DENY">继承并限制</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
    </el-tab-pane>
    
    <!-- 功能权限配置 -->
    <el-tab-pane label="功能权限" name="permission">
      <el-tree
        ref="permTree"
        :data="resourceTree"
        show-checkbox
        node-key="id"
        :props="{ label: 'name', children: 'children' }"
      >
        <template #default="{ node, data }">
          <span>{{ data.name }}</span>
          <el-checkbox-group v-if="data.type !== 'MENU'" v-model="data.actions">
            <el-checkbox label="VIEW">查看</el-checkbox>
            <el-checkbox label="CREATE">新增</el-checkbox>
            <el-checkbox label="UPDATE">编辑</el-checkbox>
            <el-checkbox label="DELETE">删除</el-checkbox>
          </el-checkbox-group>
        </template>
      </el-tree>
    </el-tab-pane>
    
    <!-- 数据权限配置 -->
    <el-tab-pane label="数据权限" name="dataScope">
      <el-table :data="dataScopes">
        <el-table-column prop="dimensionName" label="维度" />
        <el-table-column label="范围类型">
          <template #default="{ row }">
            <el-select v-model="row.scopeType">
              <el-option label="全部数据" value="ALL" />
              <el-option label="仅本人" value="SELF" />
              <el-option label="本部门" value="SELF_DEPT" />
              <el-option label="本部门及下级" value="DEPT_TREE" />
              <el-option label="自定义" value="CUSTOM" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="自定义范围">
          <template #default="{ row }">
            <el-select
              v-if="row.scopeType === 'CUSTOM'"
              v-model="row.scopeValues"
              multiple
              :placeholder="`选择${row.dimensionName}`"
            />
          </template>
        </el-table-column>
      </el-table>
    </el-tab-pane>
    
    <!-- 字段权限配置 -->
    <el-tab-pane label="字段权限" name="fieldPerm">
      <el-table :data="sensitiveFields">
        <el-table-column prop="fieldName" label="字段" />
        <el-table-column prop="sensitiveLevel" label="敏感级别" />
        <el-table-column label="可查看">
          <template #default="{ row }">
            <el-switch v-model="row.canView" />
          </template>
        </el-table-column>
        <el-table-column label="可编辑">
          <template #default="{ row }">
            <el-switch v-model="row.canEdit" />
          </template>
        </el-table-column>
      </el-table>
    </el-tab-pane>
  </el-tabs>
</template>
```

---

## 九、权限缓存设计

### 9.1 缓存策略

| 缓存项 | Key 格式 | 过期时间 | 刷新时机 |
|--------|----------|----------|----------|
| 用户权限 | `perm:user:{userId}` | 1小时 | 用户登录、角色变更 |
| 用户菜单 | `perm:menu:{userId}` | 1小时 | 用户登录、角色变更 |
| 资源树 | `perm:resource:tree` | 永久（手动刷新） | 资源变更 |
| 角色树 | `perm:role:tree` | 永久（手动刷新） | 角色变更 |

### 9.2 PermissionCacheService

```java
public class PermissionCacheService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String USER_PERM_KEY = "perm:user:";
    private static final String USER_MENU_KEY = "perm:menu:";
    private static final long CACHE_EXPIRE = 3600;
    
    public void cacheUserPermissions(Long userId, UserPermissionResponse perms) {
        String key = USER_PERM_KEY + userId;
        redisTemplate.opsForValue().set(key, perms, CACHE_EXPIRE, TimeUnit.SECONDS);
    }
    
    public UserPermissionResponse getUserPermissions(Long userId) {
        String key = USER_PERM_KEY + userId;
        return (UserPermissionResponse) redisTemplate.opsForValue().get(key);
    }
    
    public void clearUserPermissions(Long userId) {
        redisTemplate.delete(USER_PERM_KEY + userId);
        redisTemplate.delete(USER_MENU_KEY + userId);
    }
    
    public void clearAllPermissions() {
        Set<String> keys = redisTemplate.keys("perm:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
```

### 9.3 权限变更事件处理

```java
public class PermissionChangedEvent implements DomainEvent {
    private ChangeType changeType;  // ROLE, RESOURCE, USER_ROLE
    private Long targetId;
}

@Component
public class PermissionChangeEventHandler {
    
    @KafkaListener(topics = "permission-events")
    public void handlePermissionChanged(PermissionChangedEvent event) {
        switch (event.getChangeType()) {
            case ROLE:
                List<Long> userIds = userRoleRepository.findUserIdsByRoleId(event.getTargetId());
                userIds.forEach(permissionCache::clearUserPermissions);
                break;
            case RESOURCE:
                permissionCache.clearAllPermissions();
                break;
            case USER_ROLE:
                permissionCache.clearUserPermissions(event.getTargetId());
                break;
        }
    }
}
```

---

## 十、实现计划分解

### 10.1 实现阶段

|阶段 | 内容 | 优先级 |
|------|------|--------|
| P1 | 核心表结构 + 基础领域模型 | 必须 |
| P2 | 功能权限（API拦截器 + 权限注解） | 必须 |
| P3 | 前端权限状态 + 路由守卫 | 必须 |
| P4 | 数据权限拦截器 | 高 |
| P5 | 字段权限处理器 | 高 |
| P6 | 角色管理 API + 前端页面 | 高 |
| P7 | 资源管理 API + 前端页面 | 中 |
| P8 | 数据维度管理 | 中 |
| P9 | 权限缓存优化 | 中 |
| P10 | 预设角色 + 初始化数据 | 低 |

### 10.2 技术依赖

| 组件 | 现有 | 新增 |
|------|------|------|
| 数据库 | MySQL | 新增 8 张表 |
| 缓存 | Redis | 新增权限缓存 Key |
| 消息队列 | Kafka | 新增 permission-events Topic |
| 后端拦截器 | AuthInterceptor | PermissionInterceptor, DataScopeInterceptor |
| 前端状态 | Pinia (user) | Pinia (permission) |

---

## 十一、风险与约束

### 11.1 技术风险

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 权限计算性能 | 多角色+继承链计算耗时 | Redis缓存 + 预计算 |
| 数据权限SQL注入 | 动态拼接SQL风险 | 参数化查询 + 白名单校验 |
| 权限缓存一致性 | 权限变更后缓存未刷新 | Kafka事件通知 + 版本号机制 |

### 11.2 业务约束

| 约束 | 说明 |
|------|------|
| 内置角色不可删除 | SUPER_ADMIN 等系统角色有保护 |
| 超级管理员权限不可限制 | 确保系统管理能力 |
| 数据维度需预定义 | 不支持运行时动态创建维度 |

### 11.3 兼容性

| 项 | 兼容方案 |
|------|----------|
| 现有 JWT认证 | 保持不变，扩展 UserInfo 添加角色信息 |
| 现有 AuthInterceptor | 保持不变，新增 PermissionInterceptor 在其后 |
| 现有 User聚合 | 保持不变，新增 UserRole 关联 |

---

## 十二、验收标准

### 12.1 功能验收

- [ ] 用户登录后可获取权限列表（菜单、操作、字段）
- [ ] 无权限访问 API 返回 403
- [ ] 无权限菜单不在导航显示
- [ ] 无权限按钮不渲染
- [ ] 数据权限正确过滤查询结果
- [ ] 敏感字段无权限时正确脱敏/隐藏
- [ ] 角色继承权限正确计算
- [ ] 多角色权限正确合并
- [ ] 权限变更后缓存正确刷新

### 12.2 性能验收

- [ ] 权限加载时间 < 100ms（有缓存）
- [ ] 权限检查不影响 API响应时间
- [ ] 数据权限 SQL过滤不影响查询性能（< 10% 增量）

### 12.3 安全验收

- [ ] 无 SQL注入风险
- [ ] 无越权访问漏洞
- [ ] 权限缓存不可被外部篡改

---

## 十三、附录

### 13.1 参考资料

- NIST RBAC Standard (INCITS 359-2004)
- Spring Security Architecture
- MyBatis Interceptor Documentation

### 13.2 术语表

| 术语 | 定义 |
|------|------|
| RBAC | Role-Based Access Control，基于角色的访问控制 |
| 聚合根 | DDD中一组相关对象的根，控制访问边界 |
| 值对象 | 无唯一标识、不可变、自验证的对象 |
| 数据维度 | 数据权限隔离的分类维度（如部门、项目） |
| 敏感字段 | 需要特殊权限才能查看/编辑的字段 |
| 权限效果 | ALLOW(允许) 或 DENY(拒绝) |