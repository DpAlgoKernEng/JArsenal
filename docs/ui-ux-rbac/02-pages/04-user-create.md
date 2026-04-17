# 新建/编辑用户

## 页面信息

| 属性 | 值 |
|------|-----|
| **路由** | `/user/create` (新建), `/user/edit/:id` (编辑) |
| **功能** | 用户表单，创建或编辑用户信息 |
| **权限** | `user:create` (新建), `user:edit` (编辑) |

---

## 页面布局

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ [Logo] 企业权限管理平台        🔍 搜索...        🔔(3)  [头像▼] Minghui    │
├──────────────┬──────────────────────────────────────────────────────────────┤
│              │                                                               │
│  🏠 首页     │   首页 > 用户管理 > 用户列表 > 新建用户                       │
│              │   ─────────────────────────────────────────                   │
│  👥 用户管理 │                                                               │
│   └ 用户列表 │   ┌───────────────────────────────────────────────────────┐ │
│   └ 角色管理 │   │                       │                               │ │
│   └ 权限配置 │   │   新建用户             │                               │ │
│              │   │                       │                               │ │
│              │   │   ─── 基本信息 ─────── │                               │ │
│              │   │                       │                               │ │
│              │   │   用户名 *             │                               │ │
│              │   │   ┌──────────────────┐ │                               │ │
│              │   │   │                  │ │                               │ │
│              │   │   └──────────────────┘ │                               │ │
│              │   │   用于登录和显示       │                               │ │
│              │   │                       │                               │ │
│              │   │   邮箱 *               │                               │ │
│              │   │   ┌──────────────────┐ │                               │ │
│              │   │   │                  │ │                               │ │
│              │   │   └──────────────────┘ │                               │ │
│              │   │   用于接收通知         │                               │ │
│              │   │                       │                               │ │
│              │   │   手机号               │                               │ │
│              │   │   ┌──────────────────┐ │                               │ │
│              │   │   │                  │ │                               │ │
│              │   │   └──────────────────┘ │                               │ │
│              │   │                       │                               │ │
│              │   │   部门                 │                               │ │
│              │   │   ┌──────────────────┐ │                               │ │
│              │   │   │ 请选择 ▼         │ │                               │ │
│              │   │   └──────────────────┘ │                               │ │
│              │   │                       │                               │ │
│              │   │   ─── 角色分配 ─────── │                               │ │
│              │   │                       │                               │ │
│              │   │   选择角色             │                               │ │
│              │   │   ┌──────────────────┐ │                               │ │
│              │   │   │ ☑ 管理员         │ │                               │ │
│              │   │   │ ☐ 财务           │ │                               │ │
│              │   │   │ ☐ 普通用户       │ │                               │ │
│              │   │   │ ☐ 销售           │ │                               │ │
│              │   │   └──────────────────┘ │                               │ │
│              │   │                       │                               │ │
│              │   │   ─── 状态设置 ─────── │                               │ │
│              │   │                       │                               │ │
│              │   │   ☑ 启用该用户        │                               │ │
│              │   │                       │                               │ │
│              │   │   ┌────────────────┐  │                               │ │
│              │   │   │    取消        │  │  [保存并创建]                │ │
│              │   │   └────────────────┘  │                               │ │
│              │   │                       │                               │ │
│              │   └───────────────────────┘                               │ │
│              │                                                               │
└──────────────┴──────────────────────────────────────────────────────────────┘
```

---

## 表单区域划分

| 区域 | 字段 | 说明 |
|------|-----|-----|
| **基本信息** | 用户名、邮箱、手机号、部门、工号 | 用户基本资料 |
| **角色分配** | 多选角色 | 为用户分配角色 |
| **状态设置** | 启用/禁用、首次登录修改密码 | 用户状态配置 |

---

## 表单字段详解

### 基本信息

| 字段 | 类型 | 必填 | 验证规则 | 说明 |
|------|-----|-----|---------|-----|
| **用户名** | 输入框 | ✓ | 3-50字符，仅字母数字下划线 | 用于登录和显示 |
| **邮箱** | 输入框 | ✓ | 邮箱格式，唯一性检查 | 用于接收通知 |
| **手机号** | 输入框 | | 11位数字 | 可选 |
| **部门** | 下拉选择 | | 部门树选择器 | 可选 |
| **工号** | 输入框 | | 最大20字符 | 可选 |

### 角色分配

```
选择角色
┌──────────────────────────────────────────┐
│ ☑ 管理员      │ ☐ 财务      │ ☐ 普通用户│
│ ☐ 销售        │ ☐ HR       │ ☐ 审批员  │
└──────────────────────────────────────────┘

CSS 规范:
.role-checkbox-group {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.role-checkbox {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  cursor: pointer;
}

.role-checkbox.checked {
  border-color: var(--color-primary);
  background: var(--color-primary-bg);
}
```

### 状态设置

```
☑ 启用该用户

☐ 首次登录修改密码  ← 新建用户默认勾选

CSS 规范:
.status-toggle {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.toggle-label {
  font-size: var(--font-size-base);
}
```

---

## 表单布局

### 分组标题

```
─── 基本信息 ───────

CSS 规范:
.section-title {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--color-foreground);
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--color-border);
}
```

### 输入框组件

```
用户名 *
┌──────────────────────────────────────────┐
│                                          │
└──────────────────────────────────────────┘
用于登录和显示  ← Helper text

CSS 规范:
.form-group {
  margin-bottom: 24px;
}

.form-label {
  display: block;
  font-size: var(--font-size-base);
  font-weight: 500;
  color: var(--color-foreground);
  margin-bottom: 8px;
}

.form-label.required::after {
  content: '*';
  color: var(--color-destructive);
  margin-left: 4px;
}

.form-input {
  width: 100%;
  height: 44px;
  padding: 0 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: var(--font-size-base);
}

.form-input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 2px rgba(var(--color-primary-rgb), 0.1);
}

.form-helper {
  font-size: var(--font-size-sm);
  color: var(--color-muted-foreground);
  margin-top: 4px;
}

.form-error {
  font-size: var(--font-size-sm);
  color: var(--color-destructive);
  margin-top: 4px;
}
```

---

## 表单验证

### 验证时机

| 字段 | 验证时机 |
|------|---------|
| 用户名 | 失焦验证 + 提交验证 |
| 邮箱 | 失焦验证 + 提交验证 + 异步唯一性检查 |
| 手机号 | 提交验证 |
| 角色 | 提交验证 |

### 验证错误显示

```
用户名 *
┌──────────────────────────────────────────┐
│                                          │  ← 边框变红
└──────────────────────────────────────────┘
⚠ 用户名长度需在3-50字符之间
```

### 异步验证

邮箱唯一性检查:
- 输入邮箱后失焦
- 请求 `/api/users/check-email?email=xxx`
- 返回已存在时显示错误 "该邮箱已被使用"

---

## 操作按钮

| 按钮 | 位置 | 行为 |
|------|-----|-----|
| **取消** | 左侧 | 返回用户列表 |
| **保存** | 右侧 (Primary) | 提交表单，返回列表 |
| **保存并创建** | 右侧 (可选) | 提交表单，继续创建新用户 |

### 按钮样式

```css
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 32px;
  padding-top: 16px;
  border-top: 1px solid var(--color-border);
}

.cancel-button {
  background: var(--color-background);
  color: var(--color-foreground);
  border: 1px solid var(--color-border);
}

.submit-button {
  background: var(--color-primary);
  color: var(--color-on-primary);
}
```

---

## 新建与编辑差异

| 差异项 | 新建用户 | 编辑用户 |
|------|---------|---------|
| **页面标题** | 新建用户 | 编辑用户 |
| **表单默认值** | 空表单 | 加载用户数据 |
| **密码相关** | 可设置初始密码 | 显示「重置密码」按钮 |
| **提交按钮** | 保存 | 保存更改 |
| **API** | POST /api/users | PUT /api/users/:id |

---

## 编辑页特殊字段

### 头像上传（编辑页可选）

```
头像
┌────────────┐
│   [头像]   │  ← 当前头像 80x80px
│            │
│  [更换头像] │
└────────────┘

交互:
- 点击「更换头像」打开图片选择器
- 支持格式: JPG, PNG, GIF
- 最大尺寸: 2MB
- 上传后即时预览
```

### 重置密码按钮

```
┌──────────────────────────────────────────┐
│ 🔐 重置密码                [重置密码]    │
│                                          │
│ 重置后用户将收到包含新密码的邮件          │
└──────────────────────────────────────────┘

CSS 规范:
.reset-password-section {
  padding: 16px;
  background: var(--color-muted);
  border-radius: var(--radius-md);
  margin-bottom: 24px;
}
```

---

## 表单提交流程

```
1. 用户填写表单
2. 点击「保存」按钮
3. 表单验证:
   - 必填字段检查
   - 格式验证
   - 异步唯一性检查 (邮箱)
4. 验证通过:
   - 按钮 show loading
   - 发送 API 请求
5. API 成功:
   - Toast 提示「用户创建成功」或「用户信息已更新」
   - 跳转回用户列表
6. API 失败:
   - Toast 提示错误信息
   - 恢复按钮状态
```

---

## Vue 组件示例

```vue
<template>
  <div class="user-form-page">
    <!-- 面包屑 -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- 表单卡片 -->
    <div class="form-card">
      <h2 class="form-title">{{ isEdit ? '编辑用户' : '新建用户' }}</h2>

      <form @submit.prevent="handleSubmit">
        <!-- 基本信息 -->
        <section class="form-section">
          <h3 class="section-title">基本信息</h3>

          <!-- 编辑页显示头像 -->
          <div v-if="isEdit" class="avatar-upload">
            <AvatarUpload
              :src="form.avatar"
              @change="handleAvatarChange"
            />
          </div>

          <FormGroup
            label="用户名"
            required
            :error="errors.username"
            helper="用于登录和显示"
          >
            <input
              v-model="form.username"
              type="text"
              placeholder="请输入用户名"
              @blur="validateUsername"
            />
          </FormGroup>

          <FormGroup
            label="邮箱"
            required
            :error="errors.email"
            helper="用于接收通知"
          >
            <input
              v-model="form.email"
              type="email"
              placeholder="请输入邮箱"
              @blur="validateEmail"
            />
          </FormGroup>

          <FormGroup
            label="手机号"
            :error="errors.phone"
          >
            <input
              v-model="form.phone"
              type="tel"
              placeholder="请输入手机号"
            />
          </FormGroup>

          <FormGroup label="部门">
            <DepartmentSelect v-model="form.departmentId" />
          </FormGroup>

          <FormGroup label="工号">
            <input
              v-model="form.employeeId"
              type="text"
              placeholder="请输入工号"
            />
          </FormGroup>
        </section>

        <!-- 角色分配 -->
        <section class="form-section">
          <h3 class="section-title">角色分配</h3>

          <RoleCheckboxGroup
            v-model="form.roles"
            :options="roleOptions"
          />
        </section>

        <!-- 状态设置 -->
        <section class="form-section">
          <h3 class="section-title">状态设置</h3>

          <div class="status-toggles">
            <ToggleSwitch
              v-model="form.enabled"
              label="启用该用户"
            />
            <ToggleSwitch
              v-if="!isEdit"
              v-model="form.forcePasswordChange"
              label="首次登录修改密码"
            />
          </div>
        </section>

        <!-- 编辑页显示重置密码 -->
        <section v-if="isEdit" class="form-section">
          <div class="reset-password-section">
            <div class="section-header">
              <span>🔐 重置密码</span>
              <Button
                type="secondary"
                size="small"
                @click="handleResetPassword"
              >
                重置密码
              </Button>
            </div>
            <p class="helper-text">重置后用户将收到包含新密码的邮件</p>
          </div>
        </section>

        <!-- 操作按钮 -->
        <div class="form-actions">
          <Button type="secondary" @click="handleCancel">取消</Button>
          <Button
            type="primary"
            :loading="submitting"
          >
            {{ isEdit ? '保存更改' : '保存' }}
          </Button>
          <Button
            v-if="!isEdit"
            type="primary"
            :loading="submitting"
            @click="handleSubmit(true)"
          >
            保存并创建
          </Button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const isEdit = computed(() => route.params.id)
const userId = computed(() => route.params.id)

// 面包屑
const breadcrumbItems = computed(() => [
  { label: '首页', route: '/home' },
  { label: '用户管理', route: '/user' },
  { label: '用户列表', route: '/user/list' },
  { label: isEdit.value ? '编辑用户' : '新建用户' }
])

// 表单数据
const form = ref({
  username: '',
  email: '',
  phone: '',
  departmentId: '',
  employeeId: '',
  roles: [],
  enabled: true,
  forcePasswordChange: true,
  avatar: ''
})

// 错误信息
const errors = ref({
  username: '',
  email: '',
  phone: ''
})

// 角色选项
const roleOptions = ref([])
const submitting = ref(false)

// 加载用户数据（编辑模式）
const loadUserData = async () => {
  if (isEdit.value) {
    const res = await api.getUser(userId.value)
    form.value = res.data
  }
}

// 加载角色选项
const loadRoleOptions = async () => {
  const res = await api.getRoles()
  roleOptions.value = res.data
}

// 表单验证
const validateUsername = () => {
  if (!form.value.username) {
    errors.value.username = '请输入用户名'
  } else if (form.value.username.length < 3 || form.value.username.length > 50) {
    errors.value.username = '用户名长度需在3-50字符之间'
  } else {
    errors.value.username = ''
  }
}

const validateEmail = async () => {
  if (!form.value.email) {
    errors.value.email = '请输入邮箱'
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.value.email)) {
    errors.value.email = '邮箱格式不正确'
  } else {
    // 异步唯一性检查
    const res = await api.checkEmail(form.value.email, userId.value)
    if (res.data.exists) {
      errors.value.email = '该邮箱已被使用'
    } else {
      errors.value.email = ''
    }
  }
}

// 提交表单
const handleSubmit = async (keepCreating = false) => {
  validateUsername()
  await validateEmail()

  if (errors.value.username || errors.value.email) {
    return
  }

  submitting.value = true
  try {
    if (isEdit.value) {
      await api.updateUser(userId.value, form.value)
      showToast('用户信息已更新')
    } else {
      await api.createUser(form.value)
      showToast('用户创建成功')
    }

    if (keepCreating) {
      // 清空表单继续创建
      form.value = { ...defaultForm }
    } else {
      router.push('/user/list')
    }
  } catch (err) {
    showToast(err.message, 'error')
  } finally {
    submitting.value = false
  }
}

// 取消
const handleCancel = () => {
  router.push('/user/list')
}

// 重置密码
const handleResetPassword = async () => {
  if (confirm('确认重置该用户密码？')) {
    await api.resetPassword(userId.value)
    showToast('密码已重置，新密码已发送至用户邮箱')
  }
}

onMounted(() => {
  loadRoleOptions()
  if (isEdit.value) {
    loadUserData()
  }
})
</script>
```