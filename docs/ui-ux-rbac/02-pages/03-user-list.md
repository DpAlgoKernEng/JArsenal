# 用户列表

## 页面信息

| 属性 | 值 |
|------|-----|
| **路由** | `/user/list` |
| **功能** | 用户管理主界面，CRUD 操作 |
| **权限** | `user:view` |

---

## 页面布局

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ [Logo] 企业权限管理平台        🔍 搜索...        🔔(3)  [头像▼] Minghui    │
├──────────────┬──────────────────────────────────────────────────────────────┤
│              │                                                               │
│  🏠 首页     │   首页 > 用户管理 > 用户列表                                   │
│              │   ─────────────────────────────                                │
│  👥 用户管理 │                                                               │
│  ●└ 用户列表 │   用户管理                                        共 1,256 条 │
│   └ 角色管理 │   ────────────────────────────────────────────────────────── │
│   └ 权限配置 │                                                               │
│              │   ┌───────────────────────────────────────────────────────┐ │
│              │   │ 🔍 搜索用户名/邮箱     │ 角色 ▼ │ 状态 ▼ │ 部门 ▼      │ │
│              │   │                       │        │        │              │ │
│              │   │ [新建用户] [导入] [导出]                    [重置筛选] │ │
│              │   └───────────────────────────────────────────────────────┘ │
│  📋 审计日志 │                                                               │
│              │   ┌────────────────────────────────────────────────────────┐│
│              │   │ 已选中 3 项  [批量分配角色] [批量启用] [批量禁用] [删除]││ ← 批量操作栏
│              │   └────────────────────────────────────────────────────────┘│
│              │                                                               │
│              │   ┌────────────────────────────────────────────────────────┐│
│              │   │ ☑ │ 头像 │用户名  │ 邮箱          │ 角色     │状态│操作 ││
│              │   ├───┼──────┼────────┼────────────────┼──────────┼────┼─────┤│
│              │   │ ☑ │ 🧑  │admin  │admin@corp.com  │管理员    │启用│ ⋮  ││
│              │   │ ☐ │ 👨  │zhangsan│zs@corp.com    │财务      │启用│ ⋮  ││
│              │   │ ☑ │ 👩  │lisi    │lisi@corp.com  │普通用户  │禁用│ ⋮  ││
│              │   │ ☐ │ 👨  │wangwu  │ww@corp.com    │销售      │启用│ ⋮  ││
│              │   │ ☐ │ 👩  │zhaoliu │zl@corp.com    │普通用户  │启用│ ⋮  ││
│              │   └────────────────────────────────────────────────────────┘│
│              │                                                               │
│              │   ────────────────────────────────────────────────────────── │
│              │   < 1 2 3 ... 50 >                     每页显示: [10▼] 条    │
│              │                                                               │
└──────────────┴──────────────────────────────────────────────────────────────┘
```

---

## 区域划分

| 区域 | 功能 |
|------|-----|
| **面包屑** | 显示当前位置层级 |
| **页面标题** | "用户管理" + 数据总数 |
| **筛选栏** | 搜索框、筛选下拉、操作按钮 |
| **批量操作栏** | 选中数据后显示，提供批量操作 |
| **数据表格** | 用户列表核心展示 |
| **分页** | 页码导航 + 每页条数选择 |

---

## 组件详解

### 面包屑

```
首页 > 用户管理 > 用户列表

CSS 规范:
.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: var(--font-size-sm);
  color: var(--color-muted-foreground);
}

.breadcrumb-item {
  color: var(--color-muted-foreground);
}

.breadcrumb-item:last-child {
  color: var(--color-foreground);
}

.breadcrumb-separator {
  color: var(--color-border);
}

.breadcrumb-link {
  color: var(--color-primary);
  cursor: pointer;
}

.breadcrumb-link:hover {
  text-decoration: underline;
}
```

### 筛选栏

```
┌───────────────────────────────────────────────────────────────┐
│ 🔍 搜索用户名/邮箱     │ 角色 ▼ │ 状态 ▼ │ 部门 ▼              │
│                       │        │        │                      │
│ [新建用户] [导入] [导出]                    [重置筛选]         │
└───────────────────────────────────────────────────────────────┘

CSS 规范:
.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding: 16px;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border);
}

.search-input {
  flex: 1;
  min-width: 200px;
  max-width: 400px;
  height: 36px;
}

.filter-select {
  width: 120px;
  height: 36px;
}

.action-buttons {
  display: flex;
  gap: 8px;
  margin-left: auto;
}

.reset-button {
  color: var(--color-muted-foreground);
}
```

**筛选字段**:

| 字段 | 类型 | 数据来源 |
|------|-----|---------|
| 搜索 | 输入框 | 搜索用户名/邮箱，模糊匹配 |
| 角色 | 下拉选择 | `/api/roles` (角色列表) |
| 状态 | 下拉选择 | 启用/禁用/全部 |
| 部门 | 下拉选择 | `/api/departments` (部门树) |

**筛选交互**:

- 搜索: 输入后按 Enter 或点击搜索图标触发
- 下拉: 选择后立即触发筛选
- 重置: 清空所有筛选条件，恢复默认

### 操作按钮

| 按钮 | 权限 | 行为 |
|------|-----|-----|
| **新建用户** | `user:create` | 跳转到 `/user/create` |
| **导入** | `user:create` | 打开导入弹窗 |
| **导出** | `user:export` | 导出当前筛选结果为 Excel |
| **重置筛选** | 无 | 清空筛选条件 |

**按钮样式**:

```css
.primary-button {
  background: var(--color-primary);
  color: var(--color-on-primary);
}

.secondary-button {
  background: var(--color-background);
  color: var(--color-foreground);
  border: 1px solid var(--color-border);
}
```

### 批量操作栏

```
┌────────────────────────────────────────────────────────────────┐
│ 已选中 3 项  [批量分配角色] [批量启用] [批量禁用] [删除]        │
└────────────────────────────────────────────────────────────────┘

CSS 规范:
.batch-action-bar {
  display: none; /* 默认隐藏 */
  align-items: center;
  gap: 16px;
  padding: 12px 16px;
  background: var(--color-info-bg); /* 浅蓝背景 */
  border-radius: var(--radius-md);
  margin-bottom: 12px;
}

.batch-action-bar.visible {
  display: flex;
}

.selected-count {
  font-size: var(--font-size-base);
  font-weight: 500;
  color: var(--color-info);
}

.batch-buttons {
  display: flex;
  gap: 8px;
}
```

**批量操作**:

| 操作 | 权限 | 行为 |
|------|-----|-----|
| **批量分配角色** | `user:edit` | 打开批量分配角色弹窗 |
| **批量启用** | `user:edit` | 批量设置状态为启用 |
| **批量禁用** | `user:edit` | 批量设置状态为禁用 |
| **批量删除** | `user:delete` | 打开批量删除确认弹窗 |

**显示逻辑**:
- 选中 ≥1 条数据时显示
- 取消所有选中时隐藏

### 数据表格

```
┌────────────────────────────────────────────────────────────────┐
│ ☑ │ 头像 │用户名  │ 邮箱          │ 角色     │状态│操作       │
├───┼──────┼────────┼────────────────┼──────────┼────┼───────────┤
│ ☑ │ 🧑  │admin  │admin@corp.com  │管理员    │启用│    ⋮     │
│ ☐ │ 👨  │zhangsan│zs@corp.com    │财务      │启用│    ⋮     │
│ ☑ │ 👩  │lisi    │lisi@corp.com  │普通用户  │禁用│    ⋮     │
└────────────────────────────────────────────────────────────────┘

CSS 规范:
.data-table {
  width: 100%;
  border-collapse: collapse;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
}

.data-table th {
  padding: 12px 16px;
  background: var(--color-muted);
  font-size: var(--font-size-sm);
  font-weight: 500;
  color: var(--color-muted-foreground);
  text-align: left;
  border-bottom: 1px solid var(--color-border);
}

.data-table td {
  padding: 12px 16px;
  font-size: var(--font-size-base);
  border-bottom: 1px solid var(--color-border);
  vertical-align: middle;
}

.data-table tr:hover td {
  background: var(--color-muted);
}

.data-table tr.selected td {
  background: var(--color-info-bg);
}
```

**表格列定义**:

| 列 | 宽度 | 内容 | 排序 |
|------|-----|-----|-----|
| **复选框** | 40px | 全选/单选 checkbox | - |
| **头像** | 48px | 用户头像圆形 | - |
| **用户名** | auto | 可点击跳转详情 | ✓ |
| **邮箱** | 200px | 邮箱地址 | - |
| **角色** | 120px | 角色标签列表 | ✓ |
| **状态** | 80px | 启用/禁用标签 | ✓ |
| **操作** | 80px | ⋮ 菜单按钮 | - |

**列交互**:

- 点击表头可排序的列，切换升序/降序
- 显示排序图标: `sort-asc` / `sort-desc`

**行交互**:

- 悬停: 行背景高亮
- 选中: 复选框勾选，行背景变为浅蓝色
- 点击用户名: 跳转用户详情页

### 头像组件

```
┌──────┐
│  🧑  │  ← 40x40px 圆形头像
└──────┘

CSS 规范:
.avatar {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-full);
  background: var(--color-muted);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-fallback {
  font-size: var(--font-size-lg);
  color: var(--color-muted-foreground);
}
```

**头像显示逻辑**:
- 有头像图片: 显示图片
- 无头像图片: 显示默认头像或首字母

### 角色标签

```
┌──────────────────┐
│ 管理员 │ 财务   │  ← 多角色显示为标签列表
└──────────────────┘

CSS 规范:
.role-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.role-tag {
  padding: 2px 8px;
  font-size: var(--font-size-xs);
  background: var(--color-primary-bg);
  color: var(--color-primary);
  border-radius: var(--radius-sm);
}
```

### 状态标签

```
启用状态: ┌──────┐
         │ 启用 │  ← 绿色背景
         └──────┘

禁用状态: ┌──────┐
         │ 禁用 │  ← 红色背景
         └──────┘

CSS 规范:
.status-tag {
  padding: 2px 8px;
  font-size: var(--font-size-xs);
  border-radius: var(--radius-sm);
}

.status-tag.enabled {
  background: var(--color-accent-bg);
  color: var(--color-accent);
}

.status-tag.disabled {
  background: var(--color-destructive-bg);
  color: var(--color-destructive);
}
```

### 操作菜单

点击 ⋮ 按钮弹出操作菜单:

```
┌──────────────────┐
│ 👁 查看详情       │  ← 跳转用户详情页
│ ✏ 编辑用户       │  ← 跳转编辑页
│ 🔐 重置密码       │  ← 打开重置密码弹窗
│ 📋 分配角色       │  ← 打开分配角色弹窗
│ ──────────────── │
│ ⛔ 禁用用户       │  ← 切换启用/禁用状态
│ 🗑 删除用户       │  ← 打开删除确认弹窗
└──────────────────┘

CSS 规范:
.action-menu {
  position: absolute;
  right: 0;
  width: 160px;
  background: var(--color-surface);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-3);
  padding: 4px 0;
}

.action-menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  font-size: var(--font-size-sm);
  cursor: pointer;
}

.action-menu-item:hover {
  background: var(--color-muted);
}

.action-menu-divider {
  height: 1px;
  background: var(--color-border);
  margin: 4px 0;
}

.action-menu-item.danger {
  color: var(--color-destructive);
}
```

**菜单项权限**:

| 菜单项 | 权限 | 无权限时 |
|--------|-----|---------|
| 查看详情 | `user:view` | 正常显示 |
| 编辑用户 | `user:edit` | 禁用或隐藏 |
| 重置密码 | `user:reset-password` | 禁用或隐藏 |
| 分配角色 | `user:assign-role` | 禁用或隐藏 |
| 禁用用户 | `user:edit` | 禁用或隐藏 |
| 删除用户 | `user:delete` | 禁用或隐藏 |

### 分页组件

```
< 1 2 3 ... 50 >                     每页显示: [10▼] 条

CSS 规范:
.pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
}

.page-nav {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-button {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  cursor: pointer;
}

.page-button.active {
  background: var(--color-primary);
  color: var(--color-on-primary);
  border-color: var(--color-primary);
}

.page-button:disabled {
  background: var(--color-muted);
  color: var(--color-muted-foreground);
  cursor: not-allowed;
}

.page-size-select {
  width: 80px;
  height: 32px;
}
```

**分页配置**:

| 属性 | 默认值 |
|------|-----|
| 每页条数 | 10, 20, 50, 100 |
| 默认每页 | 10 |
| 最多显示页码 | 7 个 (含省略号) |

---

## 表格交互

### 复选框交互

```
全选逻辑:
- 点击表头复选框 → 勾选当前页所有行
- 当前页全部勾选 → 表头复选框显示勾选状态
- 当前页部分勾选 → 表头复选框显示半选状态 (indeterminate)

单选逻辑:
- 点击行复选框 → 勾选/取消勾选该行
- 勾选后 → 显示批量操作栏
- 取消所有勾选 → 隐藏批量操作栏
```

### 排序交互

```
- 点击可排序的列头 → 切换升序/降序
- 排序图标:
  - 默认: sort-none (无排序)
  - 升序: sort-asc
  - 降序: sort-desc
- 排序时重新请求 API
```

### 行点击交互

```
- 点击用户名 → 跳转详情页 /user/detail/:id
- 点击其他区域 → 无操作
- 双击行 → 跳转详情页 (可选)
```

---

## 空状态

无数据时显示空状态:

```
┌────────────────────────────────────────────────────────────────┐
│                                                                │
│                          [空状态图标]                          │
│                                                                │
│                       暂无用户数据                             │
│                                                                │
│                  [新建用户] 或 [导入用户]                      │
│                                                                │
└────────────────────────────────────────────────────────────────┘

CSS 规范:
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px;
  text-align: center;
}

.empty-icon {
  width: 64px;
  height: 64px;
  color: var(--color-muted-foreground);
}

.empty-text {
  margin-top: 16px;
  font-size: var(--font-size-lg);
  color: var(--color-muted-foreground);
}

.empty-actions {
  margin-top: 24px;
  display: flex;
  gap: 12px;
}
```

---

## 加载状态

数据加载中显示骨架屏:

```
┌────────────────────────────────────────────────────────────────┐
│ ☐ │ [□□] │[□□□□] │ [□□□□□□□□] │ [□□□] │[□□]│   [□]   │
│ ☐ │ [□□] │[□□□□] │ [□□□□□□□□] │ [□□□] │[□□]│   [□]   │
│ ☐ │ [□□] │[□□□□] │ [□□□□□□□□] │ [□□□] │[□□]│   [□]   │
└────────────────────────────────────────────────────────────────┘

CSS 规范:
.skeleton {
  background: linear-gradient(90deg, var(--color-muted) 25%, var(--color-surface) 50%, var(--color-muted) 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s infinite;
}

@keyframes skeleton-loading {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}
```

---

## Vue 组件示例

```vue
<template>
  <div class="user-list-page">
    <!-- 面包屑 -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <SearchInput
        v-model="filters.search"
        placeholder="搜索用户名/邮箱"
        @search="handleSearch"
      />
      <FilterSelect
        v-model="filters.role"
        label="角色"
        :options="roleOptions"
      />
      <FilterSelect
        v-model="filters.status"
        label="状态"
        :options="statusOptions"
      />
      <FilterSelect
        v-model="filters.department"
        label="部门"
        :options="departmentOptions"
      />
      <div class="action-buttons">
        <Button
          v-if="hasPermission('user:create')"
          type="primary"
          icon="user-plus"
          @click="goToCreate"
        >
          新建用户
        </Button>
        <Button
          v-if="hasPermission('user:create')"
          icon="upload"
          @click="openImportDialog"
        >
          导入
        </Button>
        <Button
          v-if="hasPermission('user:export')"
          icon="download"
          @click="handleExport"
        >
          导出
        </Button>
        <Button
          type="ghost"
          @click="resetFilters"
        >
          重置筛选
        </Button>
      </div>
    </div>

    <!-- 批量操作栏 -->
    <BatchActionBar
      :visible="selectedUsers.length > 0"
      :count="selectedUsers.length"
      :actions="batchActions"
      @action="handleBatchAction"
    />

    <!-- 数据表格 -->
    <DataTable
      :columns="columns"
      :data="users"
      :loading="loading"
      :selected-keys="selectedIds"
      @select="handleSelect"
      @sort="handleSort"
      @row-click="handleRowClick"
    >
      <!-- 自定义列渲染 -->
      <template #avatar="{ row }">
        <Avatar :src="row.avatar" :fallback="row.username" />
      </template>
      <template #roles="{ row }">
        <RoleTags :roles="row.roles" />
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
      :page-sizes="[10, 20, 50, 100]"
      @change="handlePageChange"
      @size-change="handlePageSizeChange"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 面包屑
const breadcrumbItems = [
  { label: '首页', route: '/home' },
  { label: '用户管理', route: '/user' },
  { label: '用户列表' }
]

// 筛选条件
const filters = ref({
  search: '',
  role: '',
  status: '',
  department: ''
})

// 用户数据
const users = ref([])
const loading = ref(false)
const selectedIds = ref([])
const selectedUsers = computed(() => {
  return users.value.filter(u => selectedIds.value.includes(u.id))
})

// 分页
const pagination = ref({
  current: 1,
  total: 0,
  pageSize: 10
})

// 表格列定义
const columns = [
  { key: 'checkbox', width: 40, type: 'selection' },
  { key: 'avatar', title: '头像', width: 48 },
  { key: 'username', title: '用户名', sortable: true },
  { key: 'email', title: '邮箱', width: 200 },
  { key: 'roles', title: '角色', width: 120, sortable: true },
  { key: 'status', title: '状态', width: 80, sortable: true },
  { key: 'actions', title: '操作', width: 80 }
]

// 获取操作菜单项
const getActionItems = (user) => {
  return [
    { key: 'view', label: '查看详情', icon: 'eye', permission: 'user:view' },
    { key: 'edit', label: '编辑用户', icon: 'edit', permission: 'user:edit' },
    { key: 'reset-password', label: '重置密码', icon: 'key', permission: 'user:reset-password' },
    { key: 'assign-role', label: '分配角色', icon: 'shield', permission: 'user:assign-role' },
    { key: 'divider', type: 'divider' },
    { key: user.status === 'enabled' ? 'disable' : 'enable', label: user.status === 'enabled' ? '禁用用户' : '启用用户', icon: 'ban', permission: 'user:edit' },
    { key: 'delete', label: '删除用户', icon: 'trash-2', permission: 'user:delete', danger: true }
  ].filter(item => !item.permission || hasPermission(item.permission))
}

// 加载用户列表
const loadUsers = async () => {
  loading.value = true
  try {
    const res = await api.getUsers({
      ...filters.value,
      page: pagination.value.current,
      pageSize: pagination.value.pageSize,
      sortField: sort.value.field,
      sortOrder: sort.value.order
    })
    users.value = res.data.list
    pagination.value.total = res.data.total
  } finally {
    loading.value = false
  }
}

// 权限检查
const hasPermission = (perm) => userStore.hasPermission(perm)

// 事件处理
const handleSearch = () => loadUsers()
const handleSort = (field, order) => loadUsers()
const handlePageChange = (page) => { pagination.value.current = page; loadUsers() }
const handlePageSizeChange = (size) => { pagination.value.pageSize = size; loadUsers() }
const handleSelect = (ids) => { selectedIds.value = ids }
const handleRowClick = (row) => router.push(`/user/detail/${row.id}`)
const goToCreate = () => router.push('/user/create')

// ... 更多事件处理函数

onMounted(() => {
  loadUsers()
  loadRoleOptions()
  loadDepartmentOptions()
})
</script>
```

---

## 响应式适配

### 平板 (768px - 1024px)

```
筛选栏: 纵向排列或折叠
表格: 部分列隐藏 (如邮箱列)
操作菜单: 正常显示
```

### 手机 (< 768px)

```
筛选栏: 完全折叠，点击展开
表格: 卡片式列表替代表格
  ┌───────────────────────────────────────┐
  │ 🧑 admin                              │
  │ 管理员 · admin@corp.com               │
  │ 状态: 启用                            │
  │                      [编辑] [详情]    │
  └───────────────────────────────────────┘
分页: 简化显示，仅显示页码
```