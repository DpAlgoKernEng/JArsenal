# P1: API封装层实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 创建角色和资源的API封装，供其他模块使用

**Architecture:** 封装axios实例调用，提供统一的API接口，与后端Controller完全对应

**Tech Stack:** Vue 3 + axios + Element Plus

**依赖:** 设计文档 `docs/superpowers/specs/fronted-rbac/2026-04-16-frontend-rbac-design.md`

---

## 文件清单

| 文件 | 操作 | 说明 |
|------|------|------|
| `ui/src/api/role.js` | 创建 | 角色管理API封装 |
| `ui/src/api/resource.js` | 创建 | 资源管理API封装 |
| `ui/src/api/index.js` | 修改 | 添加role/resource导出 |

---

### Task 1: 创建角色API封装

**Files:**
- Create: `ui/src/api/role.js`

- [ ] **Step 1: 创建 role.js 文件**

```javascript
import api from './index'

/**
 * 角色管理 API
 */
export const roleApi = {
  /**
   * 获取角色树
   * @returns {Promise<Array<RoleResponse>>}
   */
  tree: () => api.get('/roles'),

  /**
   * 获取角色详情
   * @param {number} id 角色ID
   * @returns {Promise<RoleResponse>}
   */
  get: (id) => api.get(`/roles/${id}`),

  /**
   * 创建角色
   * @param {Object} data {code, name, parentId, inheritMode, sort}
   * @returns {Promise<RoleResponse>}
   */
  create: (data) => api.post('/roles', data),

  /**
   * 更新角色
   * @param {number} id 角色ID
   * @param {Object} data {name, parentId, inheritMode, status, sort}
   * @returns {Promise<RoleResponse>}
   */
  update: (id, data) => api.put(`/roles/${id}`, data),

  /**
   * 删除角色
   * @param {number} id 角色ID
   * @returns {Promise<void>}
   */
  delete: (id) => api.delete(`/roles/${id}`),

  /**
   * 获取用户角色列表
   * @param {number} userId 用户ID
   * @returns {Promise<Array<RoleResponse>>}
   */
  getUserRoles: (userId) => api.get(`/roles/user/${userId}`),

  /**
   * 分配角色给用户
   * @param {number} userId 用户ID
   * @param {Array<number>} roleIds 角色ID列表
   * @returns {Promise<void>}
   */
  assignToUser: (userId, roleIds) => api.post(`/roles/user/${userId}/roles`, { roleIds }),

  /**
   * 移除用户角色
   * @param {number} userId 用户ID
   * @param {number} roleId 角色ID
   * @returns {Promise<void>}
   */
  removeFromUser: (userId, roleId) => api.delete(`/roles/user/${userId}/roles/${roleId}`),

  /**
   * 分配权限给角色
   * @param {number} roleId 角色ID
   * @param {Object} data {resourceId, actions, effect}
   * @returns {Promise<void>}
   */
  assignPermission: (roleId, data) => api.post(`/roles/${roleId}/permissions`, data)
}
```

- [ ] **Step 2: 验证文件创建成功**

Run: `ls -la ui/src/api/role.js`
Expected: 文件存在

---

### Task 2: 创建资源API封装

**Files:**
- Create: `ui/src/api/resource.js`

- [ ] **Step 1: 创建 resource.js 文件**

```javascript
import api from './index'

/**
 * 资源管理 API
 */
export const resourceApi = {
  /**
   * 获取资源树（ResourceResponse格式）
   * @returns {Promise<Array<ResourceResponse>>}
   */
  tree: () => api.get('/resources'),

  /**
   * 获取资源树V2（ResourceTreeResponse格式）
   * @returns {Promise<Array<ResourceTreeResponse>>}
   */
  treeV2: () => api.get('/resources/tree'),

  /**
   * 获取资源详情
   * @param {number} id 资源ID
   * @returns {Promise<ResourceResponse>}
   */
  get: (id) => api.get(`/resources/${id}`),

  /**
   * 创建资源
   * @param {Object} data {code, name, parentId, type, path, pathPattern, method, icon, component, sort}
   * @returns {Promise<ResourceResponse>}
   */
  create: (data) => api.post('/resources', data),

  /**
   * 更新资源
   * @param {number} id 资源ID
   * @param {Object} data {name, parentId, path, pathPattern, method, icon, component, status, sort}
   * @returns {Promise<ResourceResponse>}
   */
  update: (id, data) => api.put(`/resources/${id}`, data),

  /**
   * 删除资源
   * @param {number} id 资源ID
   * @returns {Promise<void>}
   */
  delete: (id) => api.delete(`/resources/${id}`),

  /**
   * 启用资源
   * @param {number} id 资源ID
   * @returns {Promise<void>}
   */
  enable: (id) => api.post(`/resources/${id}/enable`),

  /**
   * 禁用资源
   * @param {number} id 资源ID
   * @returns {Promise<void>}
   */
  disable: (id) => api.post(`/resources/${id}/disable`),

  /**
   * 获取资源敏感字段
   * @param {number} id 资源ID
   * @returns {Promise<Array<SensitiveFieldResponse>>}
   */
  getFields: (id) => api.get(`/resources/${id}/fields`),

  /**
   * 添加敏感字段
   * @param {number} id 资源ID
   * @param {Object} data {fieldCode, fieldName, sensitiveLevel, maskPattern}
   * @returns {Promise<void>}
   */
  addField: (id, data) => api.post(`/resources/${id}/fields`, data)
}
```

- [ ] **Step 2: 验证文件创建成功**

Run: `ls -la ui/src/api/resource.js`
Expected: 文件存在

---

### Task 3: 更新API导出

**Files:**
- Modify: `ui/src/api/index.js` (末尾添加导出)

- [ ] **Step 1: 添加导出语句**

在 `ui/src/api/index.js` 文件末尾添加：

```javascript
// 角色管理 API
export { roleApi } from './role'

// 资源管理 API
export { resourceApi } from './resource'
```

- [ ] **Step 2: 验证导出正确**

Run: `grep -n "roleApi\|resourceApi" ui/src/api/index.js`
Expected: 显示新增的导出行

---

### Task 4: 提交代码

- [ ] **Step 1: Git提交**

```bash
cd ui
git add src/api/role.js src/api/resource.js src/api/index.js
git commit -m "feat(api): add role and resource API wrappers for RBAC management"
```

---

## 验收标准

- [ ] `roleApi` 包含9个方法：tree, get, create, update, delete, getUserRoles, assignToUser, removeFromUser, assignPermission
- [ ] `resourceApi` 包含10个方法：tree, treeV2, get, create, update, delete, enable, disable, getFields, addField
- [ ] `api/index.js` 正确导出 roleApi 和 resourceApi
- [ ] 代码已提交

---

## 下一步

完成此计划后，执行 `02-role-store.md` 创建角色Pinia Store。