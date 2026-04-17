# 用户详情

## 页面信息

| 属性 | 值 |
|------|-----|
| **路由** | `/user/detail/:id` |
| **功能** | 显示用户详细信息、角色、权限预览 |
| **权限** | `user:view` |

---

## 页面布局

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ [Logo] 企业权限管理平台        🔍 搜索...        🔔(3)  [头像▼] Minghui    │
├──────────────┬──────────────────────────────────────────────────────────────┤
│              │                                                               │
│              │   首页 > 用户管理 > 用户列表 > 用户详情                       │
│              │   ─────────────────────────────────────────                   │
│              │                                                               │
│              │   ┌────────────────────────────────────────────────────────┐│
│              │   │                                                        ││
│              │   │  ┌──────┐                                              ││
│              │   │  │  🧑  │  admin                                       ││
│              │   │  │      │  管理员 · 启用 · 系统管理员                   ││
│              │   │  └──────┘                                              ││
│              │   │                                                        ││
│              │   │  ┌───────────┬───────────┬───────────┬──────────────┐  ││
│              │   │  │ [编辑]    │ [重置密码]│ [分配角色]│ [禁用/删除] │  ││
│              │   │  └───────────┴───────────┴───────────┴──────────────┘  ││
│              │   │                                                        ││
│              │   │  ─── 基本信息 ──────────────────────────────────────  ││
│              │   │                                                        ││
│              │   │  邮箱        admin@corp.com                            ││
│              │   │  手机号      138****1234                               ││
│              │   │  部门        技术部                                      ││
│              │   │  工号        EMP001                                     ││
│              │   │  创建时间    2024-01-15 10:23:15                        ││
│              │   │  最后登录    2024-03-20 09:12:08  IP: 10.0.1.5         ││
│              │   │                                                        ││
│              │   │  ─── 已分配角色 ────────────────────────────────────── ││
│              │   │                                                        ││
│              │   │  ┌──────────────────────────────────────────────────┐ ││
│              │   │  │ 管理员      │ 系统管理员 │ 2024-01-15分配 │ [移除]│ ││
│              │   │  │ 财务审批    │ 财务部     │ 2024-02-20分配 │ [移除]│ ││
│              │   │  └──────────────────────────────────────────────────┘ ││
│              │   │                                                        ││
│              │   │  ─── 权限预览 ──────────────────────────────────────── ││
│              │   │                                                        ││
│              │   │  用户管理: 查看 ✓ 编辑 ✓ 删除 ✓                        ││
│              │   │  角色管理: 查看 ✓ 编辑 ✓                               ││
│              │   │  财务审批: 查看 ✓ 审批 ✓                               ││
│              │   │                                                        ││
│              │   │  ─── 操作历史 ──────────────────────────────────────── ││
│              │   │                                                        ││
│              │   │  ┌────────────────────────────────────────────────────┐││
│              │   │  │ 时间 │ 操作 │ 详情 │ 操作人 │                      │││
│              │   │  │ ... │ ... │ ... │ ... │                            │││
│              │   │  └────────────────────────────────────────────────────┘││
│              │   │                                                        ││
│              │   └────────────────────────────────────────────────────────┘│
│              │                                                               │
└──────────────┴──────────────────────────────────────────────────────────────┘
```

---

## 区域划分

| 区域 | 功能 |
|------|-----|
| **顶部信息卡片** | 用户头像、名称、角色标签、状态、操作按钮 |
| **基本信息** | 用户详细资料字段 |
| **已分配角色** | 角色列表，可移除 |
| **权限预览** | 合并所有角色权限后的预览 |
| **操作历史** | 该用户的操作日志 |

---

## 组件详解

### 顶部信息卡片

```
┌────────────────────────────────────────────────────────────────┐
│                                                                │
│  ┌──────┐                                                      │
│  │  🧑  │  admin                                               │  ← 80px 头像 + 用户名
│  │      │  管理员 · 启用 · 系统管理员                           │  ← 角色标签 + 状态 + 描述
│  └──────┘                                                      │
│                                                                │
│  ┌───────────┬───────────┬───────────┬──────────────┐         │
│  │ [编辑]    │ [重置密码]│ [分配角色]│ [禁用/删除] │         │  ← 操作按钮
│  └───────────┴───────────┴───────────┴──────────────┘         │
│                                                                │
└────────────────────────────────────────────────────────────────┘

CSS 规范:
.user-header {
  display: flex;
  align-items: flex-start;
  padding: 24px;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border);
}

.user-avatar {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-full);
  margin-right: 24px;
}

.user-info {
  flex: 1;
}

.user-name {
  font-size: 24px;
  font-weight: 600;
  color: var(--color-foreground);
}

.user-tags {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.user-actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}
```

### 用户标签

```
管理员 · 启用 · 系统管理员

CSS 规范:
.user-tag {
  padding: 4px 12px;
  font-size: var(--font-size-sm);
  border-radius: var(--radius-sm);
}

.user-tag.role {
  background: var(--color-primary-bg);
  color: var(--color-primary);
}

.user-tag.status {
  background: var(--color-accent-bg);
  color: var(--color-accent);
}

.user-tag.status.disabled {
  background: var(--color-destructive-bg);
  color: var(--color-destructive);
}

.user-tag.description {
  background: var(--color-muted);
  color: var(--color-muted-foreground);
}
```

### 操作按钮配置

| 按钮 | 权限 | 行为 |
|------|-----|-----|
| **编辑** | `user:edit` | 跳转 `/user/edit/:id` |
| **重置密码** | `user:reset-password` | 打开重置密码确认弹窗 |
| **分配角色** | `user:assign-role` | 打开角色分配弹窗 |
| **禁用/启用** | `user:edit` | 切换用户状态 |
| **删除** | `user:delete` | 打开删除确认弹窗 |

### 基本信息列表

```
─── 基本信息 ──────────────────────────────────────

邮箱        admin@corp.com
手机号      138****1234
部门        技术部
工号        EMP001
创建时间    2024-01-15 10:23:15
最后登录    2024-03-20 09:12:08  IP: 10.0.1.5

CSS 规范:
.info-section {
  padding: 24px;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  margin-top: 16px;
}

.info-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.info-item {
  display: flex;
}

.info-label {
  width: 80px;
  font-size: var(--font-size-sm);
  color: var(--color-muted-foreground);
}

.info-value {
  flex: 1;
  font-size: var(--font-size-base);
  color: var(--color-foreground);
}
```

**信息字段**:

| 字段 | 说明 |
|------|-----|
| 邮箱 | 用户邮箱 |
| 手机号 | 部分隐藏显示 |
| 部门 | 所属部门名称 |
| 工号 | 员工编号 |
| 创建时间 | 用户创建时间 |
| 最后登录 | 最后登录时间 + IP |

### 已分配角色

```
─── 已分配角色 ──────────────────────────────────────

┌──────────────────────────────────────────────────┐
│ 管理员      │ 系统管理员 │ 2024-01-15分配 │ [移除]│
│ 财务审批    │ 财务部     │ 2024-02-20分配 │ [移除]│
└──────────────────────────────────────────────────┘

CSS 规范:
.role-cards {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.role-card {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: var(--color-muted);
  border-radius: var(--radius-md);
}

.role-name {
  width: 120px;
  font-weight: 500;
}

.role-description {
  flex: 1;
  font-size: var(--font-size-sm);
  color: var(--color-muted-foreground);
}

.role-assigned-date {
  font-size: var(--font-size-sm);
  color: var(--color-muted-foreground);
}

.role-remove-button {
  color: var(--color-destructive);
}
```

**移除角色交互**:
- 点击「移除」按钮
- 弹出确认弹窗「确认移除角色「管理员」？」
- 确认后移除角色，刷新页面数据

### 权限预览

```
─── 权限预览 ────────────────────────────────────────

用户管理: 查看 ✓ 编辑 ✓ 删除 ✓
角色管理: 查看 ✓ 编辑 ✓
财务审批: 查看 ✓ 审批 ✓

CSS 规范:
.permission-preview {
  padding: 24px;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  margin-top: 16px;
}

.permission-group {
  margin-bottom: 16px;
}

.permission-module {
  font-weight: 500;
  margin-bottom: 8px;
}

.permission-list {
  display: flex;
  gap: 16px;
  font-size: var(--font-size-sm);
}

.permission-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.permission-item.has {
  color: var(--color-accent);
}

.permission-item.no {
  color: var(--color-muted-foreground);
}
```

**权限显示逻辑**:
- 合并所有已分配角色的权限
- 有权限显示 ✓ 绿色
- 无权限显示 ✗ 灰色

### 操作历史表格

```
─── 操作历史 ────────────────────────────────────────

┌────────────────────────────────────────────────────────────────┐
│ 时间              │ 操作     │ 详情            │ 操作人       │
├────────────────────────────────────────────────────────────────┤
│ 2024-03-20 10:23 │ 创建用户 │ 新增用户 admin │ system      │
│ 2024-03-20 10:25 │ 分配角色 │ 分配管理员角色 │ admin      │
│ 2024-03-21 09:12 │ 登录     │ IP: 10.0.1.5   │ admin      │
└────────────────────────────────────────────────────────────────┘

CSS 规范:
.history-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 16px;
}
```

---

## Vue 组件示例

```vue
<template>
  <div class="user-detail-page">
    <!-- 面包屑 -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- 顶部信息卡片 -->
    <div class="user-header-card">
      <div class="user-header">
        <Avatar
          :src="user.avatar"
          :fallback="user.username"
          size="large"
        />
        <div class="user-info">
          <h2 class="user-name">{{ user.username }}</h2>
          <div class="user-tags">
            <span
              v-for="role in user.roles"
              class="tag role"
            >
              {{ role.name }}
            </span>
            <span class="tag status" :class="user.enabled ? 'enabled' : 'disabled'">
              {{ user.enabled ? '启用' : '禁用' }}
            </span>
            <span v-if="user.description" class="tag description">
              {{ user.description }}
            </span>
          </div>
          <div class="user-actions">
            <Button
              v-if="hasPermission('user:edit')"
              icon="edit"
              @click="goToEdit"
            >
              编辑
            </Button>
            <Button
              v-if="hasPermission('user:reset-password')"
              icon="key"
              @click="handleResetPassword"
            >
              重置密码
            </Button>
            <Button
              v-if="hasPermission('user:assign-role')"
              icon="shield"
              @click="openRoleDialog"
            >
              分配角色
            </Button>
            <Button
              v-if="hasPermission('user:edit')"
              :icon="user.enabled ? 'ban' : 'check'"
              @click="toggleStatus"
            >
              {{ user.enabled ? '禁用' : '启用' }}
            </Button>
            <Button
              v-if="hasPermission('user:delete')"
              icon="trash-2"
              type="danger"
              @click="handleDelete"
            >
              删除
            </Button>
          </div>
        </div>
      </div>
    </div>

    <!-- 基本信息 -->
    <section class="info-section">
      <h3 class="section-title">基本信息</h3>
      <div class="info-list">
        <InfoItem label="邮箱" :value="user.email" />
        <InfoItem label="手机号" :value="maskPhone(user.phone)" />
        <InfoItem label="部门" :value="user.department?.name || '未分配'" />
        <InfoItem label="工号" :value="user.employeeId || '-'" />
        <InfoItem label="创建时间" :value="formatDateTime(user.createdAt)" />
        <InfoItem
          label="最后登录"
          :value="user.lastLogin ? `${formatDateTime(user.lastLogin)} IP: ${user.lastLoginIp}` : '从未登录'"
        />
      </div>
    </section>

    <!-- 已分配角色 -->
    <section class="info-section">
      <h3 class="section-title">已分配角色</h3>
      <div class="role-cards">
        <div
          v-for="role in user.roles"
          class="role-card"
        >
          <span class="role-name">{{ role.name }}</span>
          <span class="role-description">{{ role.description }}</span>
          <span class="role-assigned-date">{{ formatDate(role.assignedAt) }}分配</span>
          <Button
            v-if="hasPermission('user:assign-role')"
            type="ghost"
            size="small"
            class="role-remove-button"
            @click="removeRole(role)"
          >
            移除
          </Button>
        </div>
        <div v-if="!user.roles.length" class="empty-roles">
          未分配任何角色
        </div>
      </div>
    </section>

    <!-- 权限预览 -->
    <section class="info-section">
      <h3 class="section-title">权限预览</h3>
      <div class="permission-preview">
        <div
          v-for="module in permissionsPreview"
          class="permission-group"
        >
          <div class="permission-module">{{ module.name }}</div>
          <div class="permission-list">
            <span
              v-for="perm in module.permissions"
              class="permission-item"
              :class="perm.has ? 'has' : 'no'"
            >
              {{ perm.name }} {{ perm.has ? '✓' : '✗' }}
            </span>
          </div>
        </div>
      </div>
    </section>

    <!-- 操作历史 -->
    <section class="info-section">
      <h3 class="section-title">操作历史</h3>
      <table class="history-table">
        <thead>
          <tr>
            <th>时间</th>
            <th>操作</th>
            <th>详情</th>
            <th>操作人</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="log in operationLogs" :key="log.id">
            <td>{{ formatDateTime(log.timestamp) }}</td>
            <td>{{ log.action }}</td>
            <td>{{ log.detail }}</td>
            <td>{{ log.operator }}</td>
          </tr>
        </tbody>
      </table>
    </section>

    <!-- 角色分配弹窗 -->
    <RoleAssignDialog
      v-model:visible="roleDialogVisible"
      :user-id="userId"
      :current-roles="user.roles"
      @success="loadUserDetail"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const userId = computed(() => route.params.id)

const breadcrumbItems = computed(() => [
  { label: '首页', route: '/home' },
  { label: '用户管理', route: '/user' },
  { label: '用户列表', route: '/user/list' },
  { label: '用户详情' }
])

// 用户数据
const user = ref({
  username: '',
  email: '',
  phone: '',
  avatar: '',
  roles: [],
  enabled: true,
  department: null,
  employeeId: '',
  createdAt: '',
  lastLogin: '',
  lastLoginIp: ''
})

// 权限预览
const permissionsPreview = ref([])
const operationLogs = ref([])
const roleDialogVisible = ref(false)

// 加载用户详情
const loadUserDetail = async () => {
  const res = await api.getUserDetail(userId.value)
  user.value = res.data.user
  permissionsPreview.value = res.data.permissions
  operationLogs.value = res.data.logs
}

// 权限检查
const hasPermission = (perm) => userStore.hasPermission(perm)

// 操作处理
const goToEdit = () => router.push(`/user/edit/${userId.value}`)

const handleResetPassword = async () => {
  if (confirm('确认重置该用户密码？新密码将发送至用户邮箱。')) {
    await api.resetPassword(userId.value)
    showToast('密码已重置')
  }
}

const toggleStatus = async () => {
  const action = user.value.enabled ? '禁用' : '启用'
  if (confirm(`确认${action}该用户？`)) {
    await api.toggleUserStatus(userId.value)
    showToast(`用户已${action}`)
    loadUserDetail()
  }
}

const handleDelete = async () => {
  if (confirm('确认删除该用户？此操作不可恢复。')) {
    await api.deleteUser(userId.value)
    showToast('用户已删除')
    router.push('/user/list')
  }
}

const removeRole = async (role) => {
  if (confirm(`确认移除角色「${role.name}」？`)) {
    await api.removeUserRole(userId.value, role.id)
    showToast('角色已移除')
    loadUserDetail()
  }
}

const openRoleDialog = () => {
  roleDialogVisible.value = true
}

// 格式化函数
const formatDateTime = (date) => new Date(date).toLocaleString('zh-CN')
const formatDate = (date) => new Date(date).toLocaleDateString('zh-CN')
const maskPhone = (phone) => phone?.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2') || '-'

onMounted(() => {
  loadUserDetail()
})
</script>
```