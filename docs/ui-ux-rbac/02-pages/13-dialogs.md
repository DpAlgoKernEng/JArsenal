# 弹窗组件

本文档描述 RBAC 系统中常用的弹窗组件设计。

---

## 弹窗类型

| 类型 | 使用场景 |
|------|---------|
| **确认弹窗** | 删除、禁用等危险操作确认 |
| **表单弹窗** | 新建、编辑等表单操作 |
| **详情弹窗** | 查看详细信息 |
| **批量操作弹窗** | 批量分配角色、批量删除等 |

---

## 1. 批量分配角色弹窗

### 使用场景

用户列表页选中多个用户后，点击「批量分配角色」

### 弹窗布局

```
┌─────────────────────────────────────────────────────┐
│              批量分配角色                           │
│  ─────────────────────────────────────────────────  │
│                                                     │
│  已选中 3 个用户:                                   │
│  ┌───────────────────────────────────────────────┐ │
│  │ ☑ admin    │ ☑ lisi    │ ☑ wangwu           │ │
│  └───────────────────────────────────────────────┘ │
│                                                     │
│  ─── 选择角色 ───────────────────────────────────  │
│                                                     │
│  操作模式:  [▼ 添加角色] (添加/移除/替换)           │
│                                                     │
│  ┌───────────────────────────────────────────────┐ │
│  │ ☑ 管理员     │ ☐ 财务      │ ☐ 普通用户      │ │
│  │ ☐ 销售       │ ☐ HR       │ ☐ 审批员        │ │
│  └───────────────────────────────────────────────┘ │
│                                                     │
│  💡 添加角色: 为选中用户追加新角色                  │
│     移除角色: 从选中用户移除指定角色                │
│     替换角色: 清除现有角色，替换为选中角色          │
│                                                     │
│  ┌────────────────┐                                │
│  │     取消       │        [确认分配]             │
│  └────────────────┘                                │
└─────────────────────────────────────────────────────┘
```

### 操作模式

| 模式 | 说明 |
|------|-----|
| **添加角色** | 为选中用户追加新角色（保留原有角色） |
| **移除角色** | 从选中用户移除指定角色 |
| **替换角色** | 清除选中用户所有角色，替换为选中的角色 |

### Vue 组件示例

```vue
<template>
  <Dialog
    v-model:visible="visible"
    title="批量分配角色"
    width="500px"
  >
    <!-- 已选中用户 -->
    <div class="selected-users">
      <span class="label">已选中 {{ users.length }} 个用户:</span>
      <div class="user-tags">
        <Tag v-for="user in users" :key="user.id">
          {{ user.username }}
        </Tag>
      </div>
    </div>

    <!-- 操作模式 -->
    <FormGroup label="操作模式">
      <Select v-model="mode" :options="modeOptions" />
    </FormGroup>

    <!-- 模式说明 -->
    <div class="mode-hint">
      <p v-if="mode === 'add'">💡 添加角色: 为选中用户追加新角色</p>
      <p v-else-if="mode === 'remove'">💡 移除角色: 从选中用户移除指定角色</p>
      <p v-else>💡 替换角色: 清除现有角色，替换为选中角色</p>
    </div>

    <!-- 角色选择 -->
    <FormGroup label="选择角色">
      <CheckboxGroup v-model="selectedRoles" :options="roleOptions" />
    </FormGroup>

    <template #footer>
      <Button type="secondary" @click="visible = false">取消</Button>
      <Button type="primary" :loading="submitting" @click="handleConfirm">
        确认分配
      </Button>
    </template>
  </Dialog>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  users: Array,    // 选中的用户列表
  currentRoles: Array  // 当前角色（用于移除模式）
})

const emit = defineEmits(['success'])

const visible = defineModel('visible')
const mode = ref('add')
const selectedRoles = ref([])
const submitting = ref(false)
const roleOptions = ref([])

const modeOptions = [
  { value: 'add', label: '添加角色' },
  { value: 'remove', label: '移除角色' },
  { value: 'replace', label: '替换角色' }
]

const handleConfirm = async () => {
  if (!selectedRoles.value.length) {
    showToast('请选择角色', 'error')
    return
  }

  submitting.value = true
  try {
    const userIds = props.users.map(u => u.id)
    await api.batchAssignRoles({
      userIds,
      mode: mode.value,
      roles: selectedRoles.value
    })
    showToast('角色分配成功')
    emit('success')
    visible.value = false
  } catch (err) {
    showToast(err.message, 'error')
  } finally {
    submitting.value = false
  }
}
</script>
```

---

## 2. 删除确认弹窗

### 使用场景

删除用户、角色、菜单等需要二次确认的操作

### 弹窗布局

```
┌─────────────────────────────────────────────────────┐
│              ⚠️ 删除确认                            │
│  ─────────────────────────────────────────────────  │
│                                                     │
│  您即将删除角色: [管理员]                           │
│                                                     │
│  该角色当前关联 5 个用户:                           │
│  ┌───────────────────────────────────────────────┐ │
│  │ admin, zhangsan, lisi, wangwu, zhaoliu        │ │
│  └───────────────────────────────────────────────┘ │
│                                                     │
│  ⚠️ 警告: 删除角色将移除这些用户的角色关联。        │
│                                                     │
│  请选择处理方式:                                    │
│  ○ 同时删除关联用户                                 │
│  ● 仅删除角色，保留用户 (推荐)                      │
│                                                     │
│  ─── 二次确认 ───────────────────────────────────  │
│                                                     │
│  请输入角色名称「管理员」以确认删除:                 │
│  ┌───────────────────────────────────────────────┐ │
│  │                                               │ │
│  └───────────────────────────────────────────────┘ │
│                                                     │
│  ┌────────────────┐                                │
│  │     取消       │        [确认删除]             │ ← 禁用直到输入正确
│  └────────────────┘                                │
└─────────────────────────────────────────────────────┘
```

### 二次确认规则

| 操作类型 | 二次确认方式 |
|---------|-------------|
| **删除用户** | 输入用户名 |
| **删除角色** | 输入角色名称 |
| **批量删除** | 输入「DELETE」 |
| **禁用用户** | 无需二次确认，仅确认弹窗 |

### Vue 组件示例

```vue
<template>
  <Dialog
    v-model:visible="visible"
    title="⚠️ 删除确认"
    width="450px"
  >
    <div class="delete-confirm">
      <!-- 删除对象 -->
      <p class="delete-target">
        您即将删除角色: <strong>{{ role.name }}</strong>
      </p>

      <!-- 关联信息 -->
      <div v-if="role.userCount > 0" class="associated-users">
        <p>该角色当前关联 {{ role.userCount }} 个用户:</p>
        <div class="user-list">
          {{ associatedUsers.join(', ') }}
        </div>
        <p class="warning">⚠️ 警告: 删除角色将移除这些用户的角色关联。</p>
      </div>

      <!-- 处理方式选择 -->
      <div v-if="role.userCount > 0" class="handle-options">
        <RadioGroup v-model="handleMode" :options="handleOptions" />
      </div>

      <!-- 二次确认输入 -->
      <div class="confirm-input">
        <p>请输入角色名称「{{ role.name }}」以确认删除:</p>
        <input
          v-model="confirmInput"
          type="text"
          placeholder="输入角色名称确认"
        />
      </div>
    </div>

    <template #footer>
      <Button type="secondary" @click="visible = false">取消</Button>
      <Button
        type="danger"
        :disabled="confirmInput !== role.name"
        :loading="submitting"
        @click="handleDelete"
      >
        确认删除
      </Button>
    </template>
  </Dialog>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  role: Object,
  associatedUsers: Array
})

const emit = defineEmits(['success'])

const visible = defineModel('visible')
const handleMode = ref('keep-users')
const confirmInput = ref('')
const submitting = ref(false)

const handleOptions = [
  { value: 'keep-users', label: '仅删除角色，保留用户 (推荐)' },
  { value: 'delete-users', label: '同时删除关联用户' }
]

const handleDelete = async () => {
  submitting.value = true
  try {
    await api.deleteRole(props.role.id, { handleMode: handleMode.value })
    showToast('角色已删除')
    emit('success')
    visible.value = false
  } catch (err) {
    showToast(err.message, 'error')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.delete-confirm {
  text-align: center;
}

.warning {
  color: var(--color-destructive);
  font-size: var(--font-size-sm);
}

.confirm-input input {
  width: 100%;
  margin-top: 8px;
}

.confirm-input input:focus {
  border-color: var(--color-destructive);
}
</style>
```

---

## 3. 重置密码弹窗

### 弹窗布局

```
┌─────────────────────────────────────────────────────┐
│              重置密码                               │
│  ─────────────────────────────────────────────────  │
│                                                     │
│  用户: admin (admin@corp.com)                       │
│                                                     │
│  重置方式:                                          │
│  ● 自动生成新密码并发送邮件                         │
│  ○ 手动设置新密码                                   │
│                                                     │
│  ─── 手动设置密码 ───────────────────────────────  │
│  (选择手动设置时显示)                               │
│                                                     │
│  新密码 *  [                              ]       │
│            [生成随机密码]                           │
│                                                     │
│  ☑ 首次登录修改密码                                 │
│                                                     │
│  ┌────────────────┐                                │
│  │     取消       │        [确认重置]             │
│  └────────────────┘                                │
└─────────────────────────────────────────────────────┘
```

### Vue 组件示例

```vue
<template>
  <Dialog
    v-model:visible="visible"
    title="重置密码"
    width="400px"
  >
    <div class="reset-password">
      <p class="user-info">用户: {{ user.username }} ({{ user.email }})</p>

      <!-- 重置方式 -->
      <RadioGroup v-model="resetMode" :options="resetModeOptions" />

      <!-- 手动设置密码 -->
      <div v-if="resetMode === 'manual'" class="manual-password">
        <FormGroup label="新密码" required>
          <input v-model="newPassword" type="password" />
          <Button size="small" @click="generateRandomPassword">
            生成随机密码
          </Button>
        </FormGroup>

        <ToggleSwitch v-model="forceChange" label="首次登录修改密码" />
      </div>
    </div>

    <template #footer>
      <Button type="secondary" @click="visible = false">取消</Button>
      <Button type="primary" :loading="submitting" @click="handleReset">
        确认重置
      </Button>
    </template>
  </Dialog>
</template>

<script setup>
const props = defineProps({
  user: Object
})

const emit = defineEmits(['success'])

const visible = defineModel('visible')
const resetMode = ref('auto')
const newPassword = ref('')
const forceChange = ref(true)
const submitting = ref(false)

const resetModeOptions = [
  { value: 'auto', label: '自动生成新密码并发送邮件' },
  { value: 'manual', label: '手动设置新密码' }
]

const generateRandomPassword = () => {
  newPassword.value = generatePassword(12)
}

const handleReset = async () => {
  submitting.value = true
  try {
    await api.resetPassword(props.user.id, {
      mode: resetMode.value,
      password: resetMode.value === 'manual' ? newPassword.value : undefined,
      forceChange: forceChange.value
    })
    showToast('密码已重置')
    emit('success')
    visible.value = false
  } finally {
    submitting.value = false
  }
}
</script>
```

---

## 4. 导入弹窗

### 弹窗布局

```
┌─────────────────────────────────────────────────────┐
│              导入用户                               │
│  ─────────────────────────────────────────────────  │
│                                                     │
│  ┌───────────────────────────────────────────────┐ │
│  │                                               │ │
│  │           [点击上传文件]                       │ │
│  │           或拖拽文件到此处                     │ │
│  │                                               │ │
│  │           支持: .xlsx, .csv                    │ │
│  │           最大: 10MB                           │ │
│  │                                               │ │
│  └───────────────────────────────────────────────┘ │
│                                                     │
│  已选择文件: users_import.xlsx (2.3MB)             │
│                                                     │
│  ┌────────────────┐                                │
│  │  [下载模板]   │       [开始导入]             │
│  └────────────────┘                                │
│                                                     │
│  ─── 导入预览 ───────────────────────────────────  │
│                                                     │
│  共 50 条数据，其中:                                │
│  • 有效数据: 48 条                                  │
│  • 重复数据: 2 条 (将跳过)                          │
│                                                     │
│  ┌────────────────────────────────────────────────┐│
│  │ 行号 │ 用户名 │ 邮箱 │ 状态 │ 问题 │          ││
│  │  5   │ test  │ ... │ 重复 │ 用户名已存在 │   ││
│  │ 12   │ abc   │ ... │ 格式 │ 邮箱格式错误 │   ││
│  └────────────────────────────────────────────────┘│
│                                                     │
│  ☑ 仅导入有效数据                                   │
│                                                     │
│  ┌────────────────┐                                │
│  │     取消       │        [确认导入]             │
│  └────────────────┘                                │
└─────────────────────────────────────────────────────┘
```

### Vue 组件示例

```vue
<template>
  <Dialog
    v-model:visible="visible"
    title="导入用户"
    width="600px"
  >
    <!-- 文件上传 -->
    <FileUpload
      v-model="file"
      accept=".xlsx,.csv"
      :max-size="10 * 1024 * 1024"
      @upload="handleUpload"
    >
      <template #placeholder>
        <div class="upload-placeholder">
          <UploadIcon />
          <p>点击上传文件或拖拽文件到此处</p>
          <p class="hint">支持: .xlsx, .csv | 最大: 10MB</p>
        </div>
      </template>
    </FileUpload>

    <!-- 下载模板 -->
    <Button size="small" icon="download" @click="downloadTemplate">
      下载导入模板
    </Button>

    <!-- 导入预览 -->
    <div v-if="previewData" class="import-preview">
      <p class="preview-summary">
        共 {{ previewData.total }} 条数据，其中:
        • 有效数据: {{ previewData.valid }} 条
        • 重复数据: {{ previewData.duplicate }} 条 (将跳过)
        • 格式错误: {{ previewData.invalid }} 条
      </p>

      <!-- 问题数据表格 -->
      <table v-if="previewData.errors.length" class="error-table">
        <thead>
          <tr>
            <th>行号</th>
            <th>用户名</th>
            <th>邮箱</th>
            <th>状态</th>
            <th>问题</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="error in previewData.errors" :key="error.row">
            <td>{{ error.row }}</td>
            <td>{{ error.username }}</td>
            <td>{{ error.email }}</td>
            <td>{{ error.status }}</td>
            <td>{{ error.message }}</td>
          </tr>
        </tbody>
      </table>

      <Checkbox v-model="importValidOnly" label="仅导入有效数据" />
    </div>

    <template #footer>
      <Button type="secondary" @click="visible = false">取消</Button>
      <Button
        type="primary"
        :loading="submitting"
        :disabled="!previewData || previewData.valid === 0"
        @click="handleImport"
      >
        确认导入
      </Button>
    </template>
  </Dialog>
</template>
```

---

## 弹窗通用 CSS 规范

```css
/* 弹窗容器 */
.dialog {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.dialog-overlay {
  position: absolute;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.4);
}

.dialog-content {
  position: relative;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-4);
  max-width: 90vw;
  max-height: 90vh;
  overflow: auto;
}

.dialog-header {
  padding: 16px 24px;
  border-bottom: 1px solid var(--color-border);
}

.dialog-title {
  font-size: var(--font-size-lg);
  font-weight: 600;
}

.dialog-body {
  padding: 24px;
}

.dialog-footer {
  padding: 16px 24px;
  border-top: 1px solid var(--color-border);
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 弹窗尺寸 */
.dialog-small { width: 400px; }
.dialog-medium { width: 500px; }
.dialog-large { width: 600px; }
.dialog-xlarge { width: 800px; }

/* 动画 */
.dialog-enter {
  animation: dialog-enter 250ms var(--ease-out);
}

.dialog-exit {
  animation: dialog-exit 150ms var(--ease-in);
}

@keyframes dialog-enter {
  from {
    opacity: 0;
    transform: scale(0.95) translateY(-10px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

@keyframes dialog-exit {
  from {
    opacity: 1;
    transform: scale(1);
  }
  to {
    opacity: 0;
    transform: scale(0.95);
  }
}
```

---

## 弹窗无障碍规范

| 要求 | 实现 |
|------|-----|
| **键盘导航** | Tab 在弹窗内循环，Escape 关闭弹窗 |
| **Focus 管理** | 打开弹窗时 focus 移入弹窗内第一个可交互元素 |
| **屏幕阅读器** | `role="dialog"` + `aria-modal="true"` + `aria-labelledby` |
| **关闭方式** | 提供明确的关闭按钮 + Escape 键 + 点击遮罩层(可选) |