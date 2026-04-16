# 前端RBAC系统设计文档

## 概述

为前端实现完整的RBAC（基于角色的访问控制）管理界面，包括角色管理、资源管理、权限配置三个模块。后端API已完整实现，前端需要补充API封装和管理页面。

## 后端API依赖

| 模块 | API路径 | 功能 |
|------|---------|------|
| 角色 | `/api/roles` | CRUD、树结构、用户角色分配 |
| 资源 | `/api/resources` | CRUD、树结构、敏感字段 |
| 权限分配 | `/api/roles/{id}/permissions` | 给角色分配资源权限 |

## 文件结构

```
ui/src/
├── api/
│   ├── role.js              # 角色管理API
│   ├── resource.js          # 资源管理API
│   └── permission.js        # 已有，权限查询API
├── views/
│   ├── RoleList.vue         # 角色管理页面
│   ├── ResourceList.vue     # 资源管理页面
│   └── PermissionList.vue   # 权限管理页面
├── components/
│   ├── role/
│   │   ├── RoleTree.vue     # 角色树组件
│   │   ├── RoleForm.vue     # 角色编辑表单
│   │   ├── RoleDetail.vue   # 角色详情面板
│   │   └── RoleUserList.vue # 角色关联用户列表
│   ├── resource/
│   │   ├── ResourceTree.vue # 资源树表格组件
│   │   ├── ResourceForm.vue # 资源编辑表单
│   │   └── FieldManager.vue # 敏感字段管理
│   ├── permission/
│   │   ├── RolePermissionPanel.vue # 角色权限配置面板
│   │   ├── PermissionConfigDialog.vue # 权限配置弹窗
│   │   └── UserPermissionView.vue   # 用户权限查看
```

## 模块一：角色管理

### 页面布局

**RoleList.vue**: 左侧角色树（300px） + 右侧详情面板

### 左侧角色树 (RoleTree.vue)

- el-tree组件，显示角色层级结构
- 支持搜索筛选（输入框过滤）
- 选中角色触发右侧详情更新
- 顶部按钮：新增根角色、刷新
- 节点操作：新增子角色、删除

### 右侧详情面板 (RoleDetail.vue)

- 基本信息卡片：
  - ID、编码、名称、状态、继承模式、排序、创建/更新时间
  - 编辑按钮（打开RoleForm弹窗）
  - 删除按钮（确认后删除）
  - 启用/禁用按钮

- 统计信息卡片：
  - 关联用户数（调用删除角色时会返回关联用户数，可在详情页显示）
  - 子角色数

- 权限配置入口：
  - 按钮"配置权限"，打开权限配置弹窗

### 角色编辑表单 (RoleForm.vue)

弹窗表单，字段：
- 编码（code）：必填，格式校验 `[A-Z][A-Z0-9_]{1,49}`
- 名称（name）：必填，2-50字符
- 父角色（parentId）：下拉选择，可选
- 继承模式（inheritMode）：ALL/NONE/CUSTOM
- 排序（sort）：数字输入

### API封装 (api/role.js)

```javascript
export const roleApi = {
  // 获取角色树
  tree: () => api.get('/roles'),
  // 获取角色详情
  get: (id) => api.get(`/roles/${id}`),
  // 创建角色
  create: (data) => api.post('/roles', data),
  // 更新角色
  update: (id, data) => api.put(`/roles/${id}`, data),
  // 删除角色
  delete: (id) => api.delete(`/roles/${id}`),
  // 获取用户角色
  getUserRoles: (userId) => api.get(`/roles/user/${userId}`),
  // 分配角色给用户
  assignToUser: (userId, roleIds) => api.post(`/roles/user/${userId}/roles`, { roleIds }),
  // 移除用户角色
  removeFromUser: (userId, roleId) => api.delete(`/roles/user/${userId}/roles/${roleId}`),
  // 分配权限
  assignPermission: (roleId, data) => api.post(`/roles/${roleId}/permissions`, data)
}
```

## 模块二：资源管理

### 页面布局

**ResourceList.vue**: 顶部Tab切换 + 资源树表格

### Tab分类

- 菜单资源（MENU）
- 操作资源（OPERATION）
- API资源（API）

### 资源树表格 (ResourceTree.vue)

- el-table带树形数据（row-key="id", tree-props）
- 列：名称、编码、路径、状态、排序、操作
- 操作按钮：编辑、删除、启用/禁用、新增子资源
- 顶部按钮：新增根资源、刷新

### 资源编辑表单 (ResourceForm.vue)

弹窗表单，字段：
- 编码（code）：必填
- 名称（name）：必填
- 父资源（parentId）：下拉选择
- 类型（type）：MENU/OPERATION/API，根据Tab自动设置
- 路径（path）：菜单/API必填
- 路径模式（pathPattern）：API资源可选
- 方法（method）：API资源必填（GET/POST/PUT/DELETE）
- 图标（icon）：菜单资源可选
- 组件（component）：菜单资源可选
- 排序（sort）：数字输入
- 状态（status）：启用/禁用

### API封装 (api/resource.js)

```javascript
export const resourceApi = {
  // 获取资源树
  tree: () => api.get('/resources'),
  // 获取资源树V2（ResourceTreeResponse格式）
  treeV2: () => api.get('/resources/tree'),
  // 获取详情
  get: (id) => api.get(`/resources/${id}`),
  // 创建
  create: (data) => api.post('/resources', data),
  // 更新
  update: (id, data) => api.put(`/resources/${id}`, data),
  // 删除
  delete: (id) => api.delete(`/resources/${id}`),
  // 启用
  enable: (id) => api.post(`/resources/${id}/enable`),
  // 禁用
  disable: (id) => api.post(`/resources/${id}/disable`),
  // 获取敏感字段
  getFields: (id) => api.get(`/resources/${id}/fields`),
  // 添加敏感字段
  addField: (id, data) => api.post(`/resources/${id}/fields`, data),
  // 删除敏感字段
  deleteField: (id, fieldId) => api.delete(`/resources/${id}/fields/${fieldId}`)
}
```

## 模块三：权限管理

### 页面布局

**PermissionList.vue**: 顶部Tab（角色权限配置 / 用户权限查看）

### Tab 1: 角色权限配置 (RolePermissionPanel.vue)

**左侧**：角色树选择器（单选）

**右侧**：资源权限配置区
- 资源树表格（来自resourceApi.treeV2()）
- 列：名称、编码、类型、操作按钮
- 点击行打开PermissionConfigDialog配置权限
- 注：当前版本不显示角色已有权限状态（因后端未提供查询接口），用户每次配置会覆盖该资源的权限设置

**PermissionConfigDialog.vue**:
- 显示资源名称、编码
- 操作权限复选框组：VIEW、CREATE、UPDATE、DELETE、EXECUTE
- 权限效果选择：ALLOW/DENY
- 确定后调用 `roleApi.assignPermission`

### Tab 2: 用户权限查看 (UserPermissionView.vue)

- 用户搜索框（输入用户名搜索，调用userApi.list）
- 搜索结果列表，选择用户后显示：
  - 用户基本信息
  - 关联角色列表：
    - 显示角色名称、编码、状态
    - 支持移除角色（调用roleApi.removeFromUser）
    - 支持添加角色（弹窗选择角色，调用roleApi.assignToUser）
  - 注：完整权限汇总（操作权限、字段权限）需后端扩展API，当前版本仅显示角色列表

## 路由配置

更新 `router/index.js`:

```javascript
{
  path: 'system/roles',
  name: 'RoleList',
  component: () => import('../views/RoleList.vue'),
  meta: { title: '角色管理', resource: 'ROLE_MANAGE' }
},
{
  path: 'system/resources',
  name: 'ResourceList',
  component: () => import('../views/ResourceList.vue'),
  meta: { title: '资源管理', resource: 'RESOURCE_MANAGE' }
},
{
  path: 'system/permissions',
  name: 'PermissionList',
  component: () => import('../views/PermissionList.vue'),
  meta: { title: '权限管理', resource: 'PERMISSION' }
}
```

## API导出

更新 `api/index.js`:

```javascript
export { roleApi } from './role'
export { resourceApi } from './resource'
```

## 样式规范

沿用现有样式：
- glass-card 卡片效果
- gradient-button 按钮样式
- 左侧树固定宽度300px
- 统一的padding和布局

## 实现顺序

1. API封装层（role.js, resource.js）
2. 角色管理页面（RoleTree + RoleDetail + RoleForm）
3. 资源管理页面（ResourceTree + ResourceForm）
4. 权限管理页面（RolePermissionPanel + UserPermissionView）
5. 路由更新
6. 测试验证