# 错误页面

本文档描述 RBAC 系统中的错误页面设计。

---

## 错误页面类型

| 页面 | 路由 | 说明 |
|------|-----|-----|
| **403 无权限** | `/403` | 用户无权限访问页面 |
| **404 未找到** | `/404` | 页面不存在 |
| **500 服务器错误** | `/500` | 服务器内部错误 |
| **网络错误** | - | 网络连接失败 |

---

## 1. 403 无权限页面

### 使用场景

- 用户访问无权限的页面
- API 返回权限拒绝错误
- 点击无权限的功能按钮

### 页面布局

```
┌─────────────────────────────────────────────────────────────────────┐
│                                                                      │
│                                                                      │
│                    ┌──────────────────────────┐                      │
│                    │                          │                      │
│                    │         🔒               │  ← 80px 大图标       │
│                    │                          │                      │
│                    │    无访问权限             │  ← 标题              │
│                    │                          │                      │
│                    │  您没有权限访问该页面      │  ← 说明文字          │
│                    │                          │                      │
│                    │  缺少权限:               │  ← 权限详情          │
│                    │  · 用户管理 - 删除       │                      │
│                    │                          │                      │
│                    │  [返回首页] [申请权限]    │  ← 操作按钮          │
│                    │                          │                      │
│                    └──────────────────────────┘                      │
│                                                                      │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

### Vue 组件示例

```vue
<template>
  <div class="error-page">
    <div class="error-content">
      <!-- 图标 -->
      <Lock class="error-icon" />

      <!-- 标题 -->
      <h1 class="error-title">无访问权限</h1>

      <!-- 说明 -->
      <p class="error-message">您没有权限访问该页面</p>

      <!-- 权限详情 -->
      <div v-if="missingPermissions" class="permission-details">
        <p>缺少权限:</p>
        <ul>
          <li v-for="perm in missingPermissions" :key="perm">
            {{ perm.module }} - {{ perm.action }}
          </li>
        </ul>
      </div>

      <!-- 操作按钮 -->
      <div class="error-actions">
        <Button type="primary" @click="goHome">返回首页</Button>
        <Button type="secondary" @click="requestPermission">申请权限</Button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/home')
}

const requestPermission = () => {
  // 打开权限申请流程
  // 如跳转到权限申请页面或发送申请邮件
}
</script>

<style scoped>
.error-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-background);
}

.error-content {
  text-align: center;
  max-width: 400px;
}

.error-icon {
  width: 80px;
  height: 80px;
  color: var(--color-destructive);
}

.error-title {
  margin-top: 24px;
  font-size: 32px;
  font-weight: 600;
  color: var(--color-foreground);
}

.error-message {
  margin-top: 16px;
  font-size: var(--font-size-lg);
  color: var(--color-muted-foreground);
}

.permission-details {
  margin-top: 24px;
  padding: 16px;
  background: var(--color-muted);
  border-radius: var(--radius-md);
}

.permission-details p {
  font-size: var(--font-size-sm);
  color: var(--color-muted-foreground);
}

.permission-details ul {
  list-style: disc;
  padding-left: 20px;
  margin-top: 8px;
}

.error-actions {
  margin-top: 32px;
  display: flex;
  gap: 12px;
  justify-content: center;
}
</style>
```

---

## 2. 404 未找到页面

### 页面布局

```
┌─────────────────────────────────────────────────────────────────────┐
│                                                                      │
│                                                                      │
│                    ┌──────────────────────────┐                      │
│                    │                          │                      │
│                    │        404               │  ← 大号数字          │
│                    │                          │                      │
│                    │    页面未找到             │                      │
│                    │                          │                      │
│                    │  您访问的页面不存在        │                      │
│                    │                          │                      │
│                    │  [返回首页]               │                      │
│                    │                          │                      │
│                    └──────────────────────────┘                      │
│                                                                      │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

### Vue 组件示例

```vue
<template>
  <div class="error-page">
    <div class="error-content">
      <h1 class="error-code">404</h1>
      <h2 class="error-title">页面未找到</h2>
      <p class="error-message">您访问的页面不存在或已被移除</p>
      <Button type="primary" @click="goHome">返回首页</Button>
    </div>
  </div>
</template>

<style scoped>
.error-code {
  font-size: 120px;
  font-weight: 700;
  color: var(--color-muted-foreground);
  line-height: 1;
}
</style>
```

---

## 3. 500 服务器错误页面

### 页面布局

```
┌─────────────────────────────────────────────────────────────────────┐
│                                                                      │
│                                                                      │
│                    ┌──────────────────────────┐                      │
│                    │                          │                      │
│                    │         ⚠️               │                      │
│                    │                          │                      │
│                    │    服务器错误             │                      │
│                    │                          │                      │
│                    │  系统暂时无法处理您的请求  │                      │
│                    │                          │                      │
│                    │  错误代码: ERR_500       │                      │
│                    │                          │                      │
│                    │  [刷新页面] [返回首页]    │                      │
│                    │                          │                      │
│                    │  如问题持续，请联系管理员 │                      │
│                    │                          │                      │
│                    └──────────────────────────┘                      │
│                                                                      │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

### Vue 组件示例

```vue
<template>
  <div class="error-page">
    <div class="error-content">
      <AlertTriangle class="error-icon" />
      <h1 class="error-title">服务器错误</h1>
      <p class="error-message">系统暂时无法处理您的请求</p>

      <!-- 错误代码 -->
      <p v-if="errorCode" class="error-code-text">
        错误代码: {{ errorCode }}
      </p>

      <!-- 操作按钮 -->
      <div class="error-actions">
        <Button type="primary" @click="refresh">刷新页面</Button>
        <Button type="secondary" @click="goHome">返回首页</Button>
      </div>

      <!-- 提示 -->
      <p class="error-hint">如问题持续，请联系系统管理员</p>
    </div>
  </div>
</template>

<script setup>
const refresh = () => {
  window.location.reload()
}
</script>

<style scoped>
.error-icon {
  width: 80px;
  height: 80px;
  color: var(--color-warning);
}

.error-hint {
  margin-top: 24px;
  font-size: var(--font-size-sm);
  color: var(--color-muted-foreground);
}
</style>
```

---

## 4. 网络错误组件

### 使用场景

- API 请求网络超时
- 网络断开连接
- 服务不可达

### 组件布局

```
┌─────────────────────────────────────────────────────────────────────┐
│                                                                      │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │                                                               │  │
│  │         📡 网络连接失败                                        │  │
│  │                                                               │  │
│  │  请检查您的网络连接后重试                                      │  │
│  │                                                               │  │
│  │  [重试]                                                       │  │
│  │                                                               │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘

或作为页面内的 Toast 提示:
┌────────────────────────────────────┐
│ ⚠ 网络连接失败，请稍后重试    [×] │
└────────────────────────────────────┘
```

### Vue 组件示例

```vue
<template>
  <div class="network-error">
    <WifiOff class="error-icon" />
    <h2 class="error-title">网络连接失败</h2>
    <p class="error-message">请检查您的网络连接后重试</p>
    <Button type="primary" :loading="retrying" @click="retry">重试</Button>
  </div>
</template>

<script setup>
const emit = defineEmits(['retry'])
const retrying = ref(false)

const retry = async () => {
  retrying.value = true
  try {
    emit('retry')
  } finally {
    retrying.value = false
  }
}
</script>
```

---

## 错误页面触发逻辑

### 路由守卫

```javascript
// router/index.js
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  // 检查权限
  if (to.meta.permission) {
    if (!userStore.hasPermission(to.meta.permission)) {
      // 记录缺少的权限
      next({
        path: '/403',
        query: { permission: to.meta.permission }
      })
      return
    }
  }

  next()
})

// 403 页面接收权限信息
const route = useRoute()
const missingPermission = route.query.permission
```

### API 错误处理

```javascript
// api/index.js - Axios 拦截器
axios.interceptors.response.use(
  response => response,
  error => {
    if (error.response) {
      switch (error.response.status) {
        case 401:
          // 未登录，跳转登录页
          router.push('/login')
          break
        case 403:
          // 无权限，跳转 403 页面
          router.push('/403')
          break
        case 404:
          // 资源未找到
          showToast('请求的资源不存在', 'error')
          break
        case 500:
          // 服务器错误
          router.push('/500')
          break
      }
    } else if (error.request) {
      // 网络错误
      showToast('网络连接失败，请稍后重试', 'error')
    }

    return Promise.reject(error)
  }
)
```

---

## 错误页面通用 CSS

```css
.error-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.error-content {
  text-align: center;
  max-width: 480px;
}

.error-icon {
  width: 80px;
  height: 80px;
}

.error-title {
  margin-top: 24px;
  font-size: 32px;
  font-weight: 600;
}

.error-message {
  margin-top: 16px;
  font-size: 18px;
  color: var(--color-muted-foreground);
}

.error-actions {
  margin-top: 32px;
  display: flex;
  gap: 12px;
  justify-content: center;
}
```