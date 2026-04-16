# P2: 角色Pinia Store实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 创建角色Pinia Store，供角色树、角色详情、权限配置面板等组件共享

**Architecture:** 使用Pinia Composition API风格，管理角色树数据、当前选中角色、加载状态

**Tech Stack:** Vue 3 + Pinia

**前置依赖:** P1 API封装层已完成 (`ui/src/api/role.js`)

---

## 文件清单

| 文件 | 操作 | 说明 |
|------|------|------|
| `ui/src/stores/role.js` | 创建 | 角色状态管理 |

---

### Task 1: 创建角色Store

**Files:**
- Create: `ui/src/stores/role.js`

- [ ] **Step 1: 创建 role.js 文件**

```javascript
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { roleApi } from '../api/role'

/**
 * 角色 Pinia Store
 * 管理角色树数据、当前选中角色、加载状态
 * 供 RoleTree、RoleDetail、RolePermissionPanel 等组件共享
 */
export const useRoleStore = defineStore('role', () => {
  // 角色树数据
  const roles = ref([])

  // 加载状态
  const loading = ref(false)

  // 当前选中角色
  const currentRole = ref(null)

  // 是否已加载
  const loaded = ref(false)

  /**
   * 加载角色树
   */
  const loadRoles = async () => {
    loading.value = true
    try {
      roles.value = await roleApi.tree()
      loaded.value = true
    } finally {
      loading.value = false
    }
  }

  /**
   * 选择角色
   * @param {Object} role 角色对象
   */
  const selectRole = (role) => {
    currentRole.value = role
  }

  /**
   * 刷新角色树
   */
  const refreshRoles = async () => {
    await loadRoles()
  }

  /**
   * 清空角色数据
   */
  const clearRoles = () => {
    roles.value = []
    currentRole.value = null
    loaded.value = false
  }

  /**
   * 根据ID查找角色（递归）
   * @param {number} id 角色ID
   * @returns {Object|null}
   */
  const findRoleById = (id) => {
    return findRoleInTree(roles.value, id)
  }

  /**
   * 递归查找角色
   */
  const findRoleInTree = (tree, id) => {
    for (const role of tree) {
      if (role.id === id) return role
      if (role.children && role.children.length > 0) {
        const found = findRoleInTree(role.children, id)
        if (found) return found
      }
    }
    return null
  }

  /**
   * 构建树形选择器选项
   * 用于父角色选择下拉框
   * @returns {Array}
   */
  const buildTreeOptions = () => {
    return buildOptionsFromTree(roles.value)
  }

  /**
   * 递归构建选项
   */
  const buildOptionsFromTree = (tree, options = []) => {
    for (const role of tree) {
      options.push({
        value: role.id,
        label: role.name,
        children: role.children && role.children.length > 0
          ? buildOptionsFromTree(role.children)
          : undefined
      })
    }
    return options
  }

  return {
    roles,
    loading,
    currentRole,
    loaded,
    loadRoles,
    selectRole,
    refreshRoles,
    clearRoles,
    findRoleById,
    buildTreeOptions
  }
})
```

- [ ] **Step 2: 验证文件创建成功**

Run: `ls -la ui/src/stores/role.js`
Expected: 文件存在

---

### Task 2: 提交代码

- [ ] **Step 1: Git提交**

```bash
cd ui
git add src/stores/role.js
git commit -m "feat(store): add role Pinia store for cross-component state sharing"
```

---

## 验收标准

- [ ] Store包含状态：roles, loading, currentRole, loaded
- [ ] Store包含方法：loadRoles, selectRole, refreshRoles, clearRoles, findRoleById, buildTreeOptions
- [ ] 使用Composition API风格
- [ ] 代码已提交

---

## 下一步

完成此计划后，执行 `03-role-management.md` 创建角色管理页面。