# 企业级 RBAC 权限管理系统 UI 设计文档

## 目录结构

```
docs/ui-rbac/
├── README.md                    # 总览和索引
├── 01-design-system.md          # 设计系统规范
├── 02-pages/                    # 各页面详细设计
│   ├── 01-login.md              # 登录页
│   ├── 02-dashboard.md          # 首页/仪表盘
│   ├── 03-user-list.md          # 用户列表
│   ├── 04-user-create.md        # 新建/编辑用户
│   ├── 05-user-detail.md        # 用户详情
│   ├── 06-role-list.md          # 角色列表
│   ├── 07-role-permission.md    # 角色详情/权限配置
│   ├── 08-permission-matrix.md  # 权限矩阵视图
│   ├── 09-menu-management.md    # 菜单管理
│   ├── 10-login-log.md          # 登录日志
│   ├── 11-operation-log.md      # 操作日志
│   ├── 12-system-settings.md    # 系统设置
│   ├── 13-dialogs.md            # 弹窗组件
│   └── 14-error-pages.md        # 错误页面
├── 03-components.md             # 组件库规范
├── 04-responsive.md             # 响应式布局
└── 05-interaction.md            # 交互设计规范
```

## 设计概览

### 系统定位

企业级 RBAC（Role-Based Access Control）权限管理系统，面向企业内部 IT 管理员、系统管理员、部门负责人等用户群体，提供完整的用户、角色、权限管理功能。

### 核心功能模块

| 模块 | 功能 |
|------|-----|
| **用户管理** | 用户 CRUD、批量分配角色、启用/禁用、重置密码 |
| **角色管理** | 角色 CRUD、配置权限、复制角色、角色继承 |
| **权限管理** | 模块-功能-操作三级权限树、权限导入导出 |
| **菜单管理** | 动态菜单配置、按角色显示菜单 |
| **审计日志** | 登录日志、操作审计、按用户/时间筛选 |
| **数据权限** | 行级数据权限（如部门数据隔离） |

### 页面清单

| 编号 | 页面名称 | 路径 | 功能描述 |
|------|---------|------|---------|
| 01 | 登录页 | `/login` | 用户登录入口 |
| 02 | 首页仪表盘 | `/home` | 系统概览、快捷入口 |
| 03 | 用户列表 | `/user/list` | 用户管理主界面 |
| 04 | 新建/编辑用户 | `/user/create`, `/user/edit/:id` | 用户表单 |
| 05 | 用户详情 | `/user/detail/:id` | 用户详细信息 |
| 06 | 角色列表 | `/user/role` | 角色管理主界面 |
| 07 | 权限配置 | `/user/role/permission/:id` | 角色权限树配置 |
| 08 | 权限矩阵 | `/user/permission/matrix` | 批量权限对比视图 |
| 09 | 菜单管理 | `/system/menu` | 动态菜单配置 |
| 10 | 登录日志 | `/log/login` | 登录记录查询 |
| 11 | 操作日志 | `/log/operation` | 操作审计记录 |
| 12 | 系统设置 | `/system/settings` | 系统参数配置 |
| 13 | 弹窗组件 | - | 批量操作、确认弹窗等 |
| 14 | 错误页面 | `/403`, `/404`, `/500` | 错误提示页 |

## 快速导航

- [设计系统规范](./01-design-system.md)
- [组件库规范](./03-components.md)
- [响应式布局](./04-responsive.md)
- [交互设计规范](./05-interaction.md)