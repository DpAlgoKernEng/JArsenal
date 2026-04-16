# 前端RBAC系统设计文档

## 概述

为前端实现完整的RBAC（基于角色的访问控制）管理界面，包括角色管理、资源管理、权限配置三个模块。后端API已完整实现，前端需要补充API封装和管理页面。

## 后端API依赖

| 模块 | API路径 | 功能 |
|------|---------|------|
| 角色 | `/api/roles` | CRUD、树结构、用户角色分配 |
| 资源 | `/api/resources` | CRUD、树结构、敏感字段 |
| 权限分配 | `/api/roles/{id}/permissions` | 给角色分配资源权限 |

**注**：前端 `axios.create({ baseURL: '/api' })` 已配置，API封装使用相对路径如 `api.get('/roles')`，实际请求为 `/api/roles`。

## 现有权限基础设施复用

项目已有完善的权限系统，设计需复用：

| 组件 | 路径 | 用途 |
|------|------|------|
| Permission Store | `stores/permission.js` | 权限数据管理、30秒轮询刷新 |
| usePermission | `composables/usePermission.js` | 权限检查方法（hasPermission等） |
| v-permission指令 | `directives/permission.js` | 按钮级权限控制 |

### 按钮级权限控制示例

```vue
<script setup>
import { usePermission } from '@/composables/usePermission'
const { hasPermission } = usePermission()

const canCreate = hasPermission('ROLE_MANAGE', 'CREATE')
const canUpdate = hasPermission('ROLE_MANAGE', 'UPDATE')
const canDelete = hasPermission('ROLE_MANAGE', 'DELETE')
</script>

<template>
  <el-button v-if="canCreate.value" @click="handleCreate">新增</el-button>
  <el-button v-if="canUpdate.value" @click="handleEdit">编辑</el-button>
</template>
```

### 权限变更后刷新

修改角色权限后需刷新 permissionStore：

```javascript
import { usePermissionStore } from '@/stores/permission'

// assignPermission 成功后
await roleApi.assignPermission(roleId, data)
await usePermissionStore().loadPermissions() // 刷新当前用户权限
```

## 文件结构

```
ui/src/
├── api/
│   ├── role.js              # 角色管理API
│   ├── resource.js          # 资源管理API
│   └── index.js             # 已有，需添加role/resource导出
├── stores/
│   ├── permission.js        # 已有，权限Store
│   └── role.js              # 新增，角色Store（跨组件共享）
├── views/
│   ├── RoleList.vue         # 角色管理页面
│   ├── ResourceList.vue     # 资源管理页面
│   └── PermissionList.vue   # 权限管理页面
├── components/
│   ├── role/
│   │   ├── RoleTree.vue     # 角色树组件
│   │   ├── RoleForm.vue     # 角色编辑表单
│   │   └── RoleDetail.vue   # 角色详情面板
│   ├── resource/
│   │   ├── ResourceTree.vue # 资源树表格组件
│   │   ├── ResourceForm.vue # 资源编辑表单
│   └── permission/
│   │   ├── RolePermissionPanel.vue # 角色权限配置面板
│   │   ├── PermissionConfigDialog.vue # 权限配置弹窗
│   │   └── UserPermissionView.vue   # 用户权限查看
```

## 模块一：角色管理

### Pinia Store设计 (stores/role.js)

角色树需要跨组件共享（RoleTree、RoleDetail、RolePermissionPanel），使用Pinia Store：

```javascript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { roleApi } from '../api/role'

export const useRoleStore = defineStore('role', () => {
  const roles = ref([])
  const loading = ref(false)
  const currentRole = ref(null)
  const loaded = ref(false)

  // 加载角色树
  const loadRoles = async () => {
    loading.value = true
    try {
      roles.value = await roleApi.tree()
      loaded.value = true
    } finally {
      loading.value = false
    }
  }

  // 选择角色
  const selectRole = (role) => {
    currentRole.value = role
  }

  // 刷新角色树
  const refreshRoles = async () => {
    await loadRoles()
  }

  // 清空
  const clearRoles = () => {
    roles.value = []
    currentRole.value = null
    loaded.value = false
  }

  return { roles, loading, currentRole, loaded, loadRoles, selectRole, refreshRoles, clearRoles }
})
```

### 页面布局

**RoleList.vue**: 左侧角色树（300px） + 右侧详情面板

### 左侧角色树 (RoleTree.vue)

- el-tree组件，显示角色层级结构
- 支持搜索筛选（输入框过滤）
- 选中角色触发右侧详情更新
- 顶部按钮：新增根角色（权限控制）、刷新
- 节点操作：新增子角色、删除（权限控制）

### 右侧详情面板 (RoleDetail.vue)

- 基本信息卡片：
  - ID、编码、名称、状态、继承模式、排序、创建/更新时间
  - 编辑按钮（权限控制：ROLE_MANAGE + UPDATE）
  - 删除按钮（权限控制：ROLE_MANAGE + DELETE）
  - 启用/禁用按钮（权限控制）

- 统计信息卡片：
  - 关联用户数（删除时后端返回）
  - 子角色数

- 权限配置入口：
  - 按钮"配置权限"（权限控制：PERMISSION + UPDATE）

### 角色编辑表单 (RoleForm.vue)

弹窗表单，字段：
- 编码（code）：必填，格式校验 `[A-Z][A-Z0-9_]{1,49}`
- 名称（name）：必填，2-50字符
- 父角色（parentId）：下拉选择（从roleStore.roles构建树形选项）
- 继承模式（inheritMode）：ALL/NONE/CUSTOM（下拉选择）
- 排序（sort）：数字输入，默认0

**表单校验规则**：
```javascript
const rules = {
  code: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { pattern: /^[A-Z][A-Z0-9_]{1,49}$/, message: '编码格式：大写字母开头，2-50字符，仅含字母数字下划线', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 50, message: '名称长度2-50字符', trigger: 'blur' }
  ],
  sort: [
    { type: 'number', min: 0, message: '排序值必须大于等于0', trigger: 'blur' }
  ]
}
```

### API封装 (api/role.js)

```javascript
import api from './index'

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

### 错误处理与加载状态

页面统一处理：
```vue
<script setup>
import { ElMessage, ElLoading } from 'element-plus'

const loading = ref(false)

const handleCreate = async () => {
  const loadingInstance = ElLoading.service({ lock: true })
  try {
    await roleApi.create(formData)
    ElMessage.success('创建成功')
    await roleStore.refreshRoles()
  } catch (error) {
    // error已由axios拦截器处理（ElMessage.error）
  } finally {
    loadingInstance.close()
  }
}
</script>
```

## 模块二：资源管理

### 页面布局

**ResourceList.vue**: 顶部Tab切换 + 资源树表格

资源树仅页面内使用，无需Pinia Store，使用局部状态。

### Tab分类

- 菜单资源（MENU）
- 操作资源（OPERATION）
- API资源（API）

### 资源树表格 (ResourceTree.vue)

- el-table带树形数据（row-key="id", tree-props）
- 列：名称、编码、路径、状态、排序、操作
- 操作按钮：编辑、删除、启用/禁用、新增子资源（权限控制：RESOURCE_MANAGE + UPDATE/DELETE）
- 顶部按钮：新增根资源（权限控制：RESOURCE_MANAGE + CREATE）、刷新

### 资源编辑表单 (ResourceForm.vue)

弹窗表单，字段：
- 编码（code）：必填
- 名称（name）：必填，2-50字符
- 父资源（parentId）：下拉选择（树形选择器）
- 类型（type）：MENU/OPERATION/API，根据Tab自动设置
- 路径（path）：菜单/API必填
- 路径模式（pathPattern）：API资源可选
- 方法（method）：API资源必填（GET/POST/PUT/DELETE）
- 图标（icon）：菜单资源可选
- 组件（component）：菜单资源可选
- 排序（sort）：数字输入，默认0
- 状态（status）：启用/禁用

### API封装 (api/resource.js)

```javascript
import api from './index'

export const resourceApi = {
  // 获取资源树
  tree: () => api.get('/resources'),
  // 获取资源树V2
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
  addField: (id, data) => api.post(`/resources/${id}/fields`, data)
}
```

## 模块三：权限管理

### 页面布局

**PermissionList.vue**: 顶部Tab（角色权限配置 / 用户权限查看）

### Tab 1: 角色权限配置 (RolePermissionPanel.vue)

**左侧**：角色树选择器（单选，来自roleStore）

**右侧**：资源权限配置区
- 资源树表格（来自resourceApi.treeV2()）
- 列：名称、编码、类型、操作按钮（配置）
- 点击行打开PermissionConfigDialog配置权限

### PermissionConfigDialog.vue

弹窗设计：
- 显示资源名称、编码
- **警告提示**："配置将覆盖该资源的现有权限设置"
- 操作权限复选框组：VIEW、CREATE、UPDATE、DELETE、EXECUTE
- 权限效果选择：ALLOW/DENY（下拉）
- 确定后调用 `roleApi.assignPermission`
- **刷新权限**：成功后调用 `usePermissionStore().loadPermissions()` 刷新当前用户权限

```vue
<template>
  <el-dialog title="配置权限">
    <el-alert type="warning" :closable="false">
      配置将覆盖该资源的现有权限设置
    </el-alert>
    <el-form>
      <el-form-item label="操作权限">
        <el-checkbox-group v-model="actions">
          <el-checkbox label="VIEW">查看</el-checkbox>
          <el-checkbox label="CREATE">创建</el-checkbox>
          <el-checkbox label="UPDATE">更新</el-checkbox>
          <el-checkbox label="DELETE">删除</el-checkbox>
          <el-checkbox label="EXECUTE">执行</el-checkbox>
        </el-checkbox-group>
      </el-form-item>
      <el-form-item label="权限效果">
        <el-select v-model="effect">
          <el-option label="允许" value="ALLOW" />
          <el-option label="拒绝" value="DENY" />
        </el-select>
      </el-form-item>
    </el-form>
  </el-dialog>
</template>
```

### Tab 2: 用户权限查看 (UserPermissionView.vue)

- 用户搜索框（输入用户名搜索，调用userApi.list）
- 搜索结果列表，选择用户后显示：
  - 用户基本信息
  - 关联角色列表：
    - 显示角色名称、编码、状态
    - 支持移除角色（调用roleApi.removeFromUser）
    - 支持添加角色（弹窗选择角色，调用roleApi.assignToUser）
  - 注：完整权限汇总需后端扩展API，当前版本仅显示角色列表

## 路由配置

**路由已存在**（router/index.js），只需替换Placeholder组件：

```javascript
// 现有配置（router/index.js:59-85）
{
  path: 'system/roles',
  name: 'RoleList',
  component: () => import('../views/Placeholder.vue'), // ← 替换为 RoleList.vue
  meta: { title: '角色管理', resource: 'ROLE_MANAGE' }
},
{
  path: 'system/resources',
  name: 'ResourceList',
  component: () => import('../views/Placeholder.vue'), // ← 替换为 ResourceList.vue
  meta: { title: '资源管理', resource: 'RESOURCE_MANAGE' }
},
{
  path: 'system/permissions',
  name: 'PermissionList',
  component: () => import('../views/Placeholder.vue'), // ← 替换为 PermissionList.vue
  meta: { title: '权限管理', resource: 'PERMISSION' }
}
```

实现时只需修改import路径。

## API导出

更新 `api/index.js`:

```javascript
// 现有导出
export { permissionApi } from './permission'

// 新增导出
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

1. **API封装层**：role.js、resource.js + api/index.js导出更新
2. **Pinia Store**：stores/role.js
3. **角色管理页面**：RoleTree.vue + RoleForm.vue + RoleDetail.vue + RoleList.vue
4. **资源管理页面**：ResourceTree.vue + ResourceForm.vue + ResourceList.vue
5. **权限管理页面**：RolePermissionPanel.vue + PermissionConfigDialog.vue + UserPermissionView.vue + PermissionList.vue
6. **路由更新**：修改router/index.js的import路径
7. **测试验证**：功能测试、权限控制测试