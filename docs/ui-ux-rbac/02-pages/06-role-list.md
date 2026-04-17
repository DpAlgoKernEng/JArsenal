# 角色列表

## 页面信息

| 属性 | 值 |
|------|-----|
| **路由** | `/user/role` |
| **功能** | 角色管理主界面，CRUD 操作 |
| **权限** | `role:view` |

---

## 页面布局

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ [Logo] 企业权限管理平台        🔍 搜索...        🔔(3)  [头像▼] Minghui    │
├──────────────┬──────────────────────────────────────────────────────────────┤
│              │                                                               │
│  🏠 首页     │   首页 > 用户管理 > 角色管理                                   │
│              │   ─────────────────────────────                                │
│  👥 用户管理 │                                                               │
│   └ 用户列表 │   角色管理                                        共 12 条    │
│  ●└ 角色管理 │   ────────────────────────────────────────────────────────── │
│   └ 权限配置 │                                                               │
│              │   ┌───────────────────────────────────────────────────────┐ │
│              │   │ 🔍 搜索角色名称     │ 类型 ▼ │ 状态 ▼ │ 创建时间范围  │ │
│              │   │                     │        │        │               │ │
│              │   │ [新建角色] [导入] [导出]                    [重置筛选] │ │
│              │   └───────────────────────────────────────────────────────┘ │
│              │                                                               │
│              │   ┌────────────────────────────────────────────────────────┐│
│              │   │ 角色名称 │ 描述         │ 用户数 │ 类型 │ 状态 │ 操作 ││
│              │   ├──────────┼──────────────┼────────┼──────┼──────┼───────┤│
│              │   │ 管理员   │ 系统管理员   │   5   │ 系统 │ 启用 │  ⋮   ││
│              │   │ 财务     │ 财务部门角色 │  15   │ 业务 │ 启用 │  ⋮   ││
│              │   │ 普通用户 │ 默认用户角色 │  200  │ 系统 │ 启用 │  ⋮   ││
│              │   │ 销售     │ 销售部门角色 │  30   │ 业务 │ 启用 │  ⋮   ││
│              │   │ HR       │ 人事部门角色 │  12   │ 业务 │ 禁用 │  ⋮   ││
│              │   │ 审批员   │ 流程审批角色 │   8   │ 业务 │ 启用 │  ⋮   ││
│              │   └────────────────────────────────────────────────────────┘│
│              │                                                               │
│              │   < 1 2 >                              每页显示: [10▼] 条     │
│              │                                                               │
└──────────────┴──────────────────────────────────────────────────────────────┘
```

---

## 表格列定义

| 列 | 宽度 | 内容 | 排序 | 说明 |
|------|-----|-----|-----|-----|
| **角色名称** | 150px | 角色名称 | ✓ | 可点击跳转详情 |
| **描述** | auto | 角色描述 | - | |
| **用户数** | 100px | 关联用户数量 | ✓ | 点击跳转用户列表筛选 |
| **类型** | 80px | 系统/业务 | - | 标签显示 |
| **状态** | 80px | 启用/禁用 | ✓ | 标签显示 |
| **操作** | 80px | ⋮ 菜单 | - | |

---

## 操作菜单

点击 ⋮ 按钮弹出:

```
┌──────────────────┐
│ 👁 查看详情       │  ← 跳转角色详情页
│ ✏ 编辑角色       │  ← 跳转编辑页
│ 🔐 配置权限       │  ← 核心功能：跳转权限配置页
│ 📋 复制角色       │  ← 复制角色及权限
│ ──────────────── │
│ ⛔ 禁用角色       │  ← 切换启用/禁用
│ 🗑 删除角色       │  ← 打开删除确认弹窗
└──────────────────┘
```

### 菜单项权限

| 菜单项 | 权限 | 特殊说明 |
|--------|-----|---------|
| 查看详情 | `role:view` | 正常显示 |
| 编辑角色 | `role:edit` | 系统角色禁用编辑 |
| 配置权限 | `permission:config` | 核心功能 |
| 复制角色 | `role:create` | 正常显示 |
| 禁用角色 | `role:edit` | 系统角色禁用禁用 |
| 删除角色 | `role:delete` | 有用户关联时禁止删除 |

### 删除角色限制

```
有用户关联时的删除流程:
1. 点击删除
2. 弹出提示弹窗:
   ┌────────────────────────────────────┐
   │ ⚠️ 无法删除                        │
   │                                    │
   │ 角色「财务」关联了 15 个用户:      │
   │ zhangsan, lisi, wangwu...         │
   │                                    │
   │ 请先移除关联用户，或将用户迁移至   │
   │ 其他角色后再删除。                │
   │                                    │
   │ [查看关联用户]  [取消]             │
   └────────────────────────────────────┘

3. 点击「查看关联用户」跳转用户列表，自动筛选该角色
```

---

## 角色类型标签

```
系统角色: ┌──────┐
         │ 系统 │  ← 紫色标签，不可删除/编辑
         └──────┘

业务角色: ┌──────┐
         │ 业务 │  ← 蓝色标签
         └──────┘

CSS 规范:
.type-tag {
  padding: 2px 8px;
  font-size: var(--font-size-xs);
  border-radius: var(--radius-sm);
}

.type-tag.system {
  background: var(--color-purple-bg);
  color: var(--color-purple);
}

.type-tag.business {
  background: var(--color-info-bg);
  color: var(--color-info);
}
```

---

## 新建角色弹窗

```
┌─────────────────────────────────────────────────────┐
│                    新建角色                         │
│  ─────────────────────────────────────────────────  │
│                                                     │
│  角色名称 *  [                              ]       │
│                                                     │
│  角色描述    [                              ]       │
│                                                     │
│  角色类型    [▼ 业务角色]                          │
│              ○ 系统角色 (需要管理员权限)            │
│              ● 业务角色                             │
│                                                     │
│  ─── 权限配置 ───────────────────────────────────  │
│                                                     │
│  [稍后配置] 或 [立即配置权限]                       │
│                                                     │
│  ┌────────────┐                                    │
│  │    取消    │       [保存]                       │
│  └────────────┘                                    │
└─────────────────────────────────────────────────────┘
```

### 保存后流程

```
选择「稍后配置」:
- 创建角色，跳转角色列表
- Toast 提示「角色创建成功」

选择「立即配置权限」:
- 创建角色，跳转权限配置页 `/user/role/permission/:id`
- Toast 提示「角色创建成功，请配置权限」
```

---

## 复制角色

```
┌─────────────────────────────────────────────────────┐
│                    复制角色                         │
│  ─────────────────────────────────────────────────  │
│                                                     │
│  源角色:    管理员                                  │
│                                                     │
│  新角色名称 *  [管理员(副本)              ]         │
│                                                     │
│  新角色描述    [系统管理员(副本)          ]         │
│                                                     │
│  ☑ 复制权限配置                                    │
│                                                     │
│  ┌────────────┐                                    │
│  │    取消    │       [确认复制]                   │
│  └────────────┘                                    │
└─────────────────────────────────────────────────────┘
```

---

## Vue 组件示例

```vue
<template>
  <div class="role-list-page">
    <!-- 面包屑 -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <SearchInput
        v-model="filters.search"
        placeholder="搜索角色名称"
        @search="handleSearch"
      />
      <FilterSelect
        v-model="filters.type"
        label="类型"
        :options="typeOptions"
      />
      <FilterSelect
        v-model="filters.status"
        label="状态"
        :options="statusOptions"
      />
      <div class="action-buttons">
        <Button
          v-if="hasPermission('role:create')"
          type="primary"
          icon="plus"
          @click="openCreateDialog"
        >
          新建角色
        </Button>
        <Button
          v-if="hasPermission('role:create')"
          icon="upload"
          @click="openImportDialog"
        >
          导入
        </Button>
        <Button
          v-if="hasPermission('role:export')"
          icon="download"
          @click="handleExport"
        >
          导出
        </Button>
      </div>
    </div>

    <!-- 数据表格 -->
    <DataTable
      :columns="columns"
      :data="roles"
      :loading="loading"
      @sort="handleSort"
      @row-click="handleRowClick"
    >
      <template #userCount="{ row }">
        <a class="user-count-link" @click.stop="goToUserList(row.id)">
          {{ row.userCount }}
        </a>
      </template>
      <template #type="{ row }">
        <span class="type-tag" :class="row.type">
          {{ row.type === 'system' ? '系统' : '业务' }}
        </span>
      </template>
      <template #status="{ row }">
        <StatusTag :status="row.status" />
      </template>
      <template #actions="{ row }">
        <ActionMenu :items="getActionItems(row)" @action="handleAction($event, row)" />
      </template>
    </DataTable>

    <!-- 分页 -->
    <Pagination
      :current="pagination.current"
      :total="pagination.total"
      :page-size="pagination.pageSize"
      @change="handlePageChange"
    />

    <!-- 新建角色弹窗 -->
    <RoleCreateDialog
      v-model:visible="createDialogVisible"
      @success="loadRoles"
    />

    <!-- 复制角色弹窗 -->
    <RoleCopyDialog
      v-model:visible="copyDialogVisible"
      :source-role="sourceRole"
      @success="loadRoles"
    />

    <!-- 删除确认弹窗 -->
    <RoleDeleteDialog
      v-model:visible="deleteDialogVisible"
      :role="deleteTargetRole"
      @success="loadRoles"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const breadcrumbItems = [
  { label: '首页', route: '/home' },
  { label: '用户管理', route: '/user' },
  { label: '角色管理' }
]

const filters = ref({ search: '', type: '', status: '' })
const roles = ref([])
const loading = ref(false)
const pagination = ref({ current: 1, total: 0, pageSize: 10 })

const createDialogVisible = ref(false)
const copyDialogVisible = ref(false)
const deleteDialogVisible = ref(false)
const sourceRole = ref(null)
const deleteTargetRole = ref(null)

const columns = [
  { key: 'name', title: '角色名称', width: 150, sortable: true },
  { key: 'description', title: '描述' },
  { key: 'userCount', title: '用户数', width: 100, sortable: true },
  { key: 'type', title: '类型', width: 80 },
  { key: 'status', title: '状态', width: 80, sortable: true },
  { key: 'actions', title: '操作', width: 80 }
]

const getActionItems = (role) => {
  const isSystem = role.type === 'system'
  return [
    { key: 'view', label: '查看详情', icon: 'eye', permission: 'role:view' },
    { key: 'edit', label: '编辑角色', icon: 'edit', permission: 'role:edit', disabled: isSystem },
    { key: 'permission', label: '配置权限', icon: 'key', permission: 'permission:config' },
    { key: 'copy', label: '复制角色', icon: 'copy', permission: 'role:create' },
    { key: 'divider', type: 'divider' },
    { key: 'disable', label: role.status === 'enabled' ? '禁用角色' : '启用角色', icon: 'ban', permission: 'role:edit', disabled: isSystem },
    { key: 'delete', label: '删除角色', icon: 'trash-2', permission: 'role:delete', danger: true, disabled: isSystem || role.userCount > 0 }
  ].filter(item => !item.permission || hasPermission(item.permission))
}

const loadRoles = async () => {
  loading.value = true
  try {
    const res = await api.getRoles({ ...filters.value, ...pagination.value })
    roles.value = res.data.list
    pagination.value.total = res.data.total
  } finally {
    loading.value = false
  }
}

const handleAction = (action, role) => {
  switch (action) {
    case 'view':
      router.push(`/user/role/detail/${role.id}`)
      break
    case 'edit':
      router.push(`/user/role/edit/${role.id}`)
      break
    case 'permission':
      router.push(`/user/role/permission/${role.id}`)
      break
    case 'copy':
      sourceRole.value = role
      copyDialogVisible.value = true
      break
    case 'disable':
      toggleStatus(role)
      break
    case 'delete':
      if (role.userCount > 0) {
        deleteTargetRole.value = role
        deleteDialogVisible.value = true
      } else {
        confirmDelete(role)
      }
      break
  }
}

const goToUserList = (roleId) => {
  router.push({ path: '/user/list', query: { role: roleId } })
}

const openCreateDialog = () => {
  createDialogVisible.value = true
}

onMounted(() => {
  loadRoles()
})
</script>
```