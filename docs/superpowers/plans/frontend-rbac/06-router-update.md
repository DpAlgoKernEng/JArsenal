# P6: 路由更新实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 更新路由配置，将Placeholder组件替换为实际的RBAC管理页面组件

**Architecture:** 修改router/index.js中3个路由的component import路径

**Tech Stack:** Vue 3 + Vue Router

**前置依赖:** 
- P3 角色管理页面 (`ui/src/views/RoleList.vue`)
- P4 资源管理页面 (`ui/src/views/ResourceList.vue`)
- P5 权限管理页面 (`ui/src/views/PermissionList.vue`)

---

## 文件清单

| 文件 | 操作 | 说明 |
|------|------|------|
| `ui/src/router/index.js` | 修改 | 替换3个路由的组件import |

---

### Task 1: 更新路由配置

**Files:**
- Modify: `ui/src/router/index.js`

- [ ] **Step 1: 读取当前路由配置**

Run: `grep -n "system/roles\|system/resources\|system/permissions" ui/src/router/index.js`
Expected: 显示3个路由配置行号

- [ ] **Step 2: 替换组件import路径**

将以下3处 `Placeholder.vue` 替换为实际组件：

```javascript
// 原配置
{
  path: 'system/roles',
  name: 'RoleList',
  component: () => import('../views/Placeholder.vue'),
  meta: { title: '角色管理', resource: 'ROLE_MANAGE' }
},
{
  path: 'system/resources',
  name: 'ResourceList',
  component: () => import('../views/Placeholder.vue'),
  meta: { title: '资源管理', resource: 'RESOURCE_MANAGE' }
},
{
  path: 'system/permissions',
  name: 'PermissionList',
  component: () => import('../views/Placeholder.vue'),
  meta: { title: '权限管理', resource: 'PERMISSION' }
}

// 替换为
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

- [ ] **Step 3: 验证替换成功**

Run: `grep -n "RoleList.vue\|ResourceList.vue\|PermissionList.vue" ui/src/router/index.js`
Expected: 显示3个新的import路径

---

### Task 2: 提交代码

- [ ] **Step 1: Git提交**

```bash
cd ui
git add src/router/index.js
git commit -m "feat(router): replace placeholder with RBAC management page components"
```

---

## 验收标准

- [ ] `/system/roles` 路由指向 RoleList.vue
- [ ] `/system/resources` 路由指向 ResourceList.vue
- [ ] `/system/permissions` 路由指向 PermissionList.vue
- [ ] 代码已提交

---

## 测试验证

### Task 3: 启动开发服务器验证

- [ ] **Step 1: 启动前端开发服务器**

```bash
cd ui
npm run dev
```

Expected: 服务器启动成功，显示 http://localhost:3000

- [ ] **Step 2: 访问角色管理页面**

URL: http://localhost:3000/system/roles
Expected: 显示角色树和详情面板（不再是Placeholder）

- [ ] **Step 3: 访问资源管理页面**

URL: http://localhost:3000/system/resources
Expected: 显示资源Tab和树表格（不再是Placeholder）

- [ ] **Step 4: 访问权限管理页面**

URL: http://localhost:3000/system/permissions
Expected: 显示权限配置Tab（不再是Placeholder）

---

## 完成

所有计划执行完毕后，前端RBAC系统已完整实现：
- 角色管理：左树右详情布局
- 资源管理：Tab分类 + 树表格
- 权限管理：角色权限配置 + 用户权限查看

可进行完整功能测试和权限控制测试。