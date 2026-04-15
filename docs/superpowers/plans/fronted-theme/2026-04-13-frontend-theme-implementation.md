# 前端企业级科技感主题实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将前端页面改造为现代科技风格，支持深色/浅色模式切换

**Architecture:** 使用 Element Plus SCSS 变量系统定制主题，CSS 变量定义颜色系统，useTheme composable 管理主题切换

**Tech Stack:** Vue 3, Element Plus, SCSS, Vite

---

## 文件结构规划

| 文件 | 操作 | 职责 |
|------|------|------|
| `ui/src/styles/variables.scss` | 创建 | Element Plus 变量覆盖 + 自定义颜色变量 |
| `ui/src/styles/themes/light.scss` | 创建 | 浅色主题 CSS 变量定义 |
| `ui/src/styles/themes/dark.scss` | 创建 | 深色主题 CSS 变量定义 |
| `ui/src/styles/mixins.scss` | 创建 | 玻璃态、渐变边框等混入 |
| `ui/src/styles/global.scss` | 创建 | 全局样式入口，引入所有样式文件 |
| `ui/src/composables/useTheme.js` | 创建 | 主题切换逻辑 composable |
| `ui/vite.config.js` | 修改 | 添加 SCSS 预处理配置 |
| `ui/package.json` | 修改 | 添加 sass 依赖 |
| `ui/src/App.vue` | 修改 | 引入全局样式，初始化主题 |
| `ui/src/components/Navbar.vue` | 修改 | 玻璃态导航栏 + 主题切换按钮 |
| `ui/src/views/Login.vue` | 修改 | 玻璃态卡片 + 渐变背景 |
| `ui/src/views/Register.vue` | 修改 | 玻璃态卡片 + 渐变背景 |
| `ui/src/views/UserList.vue` | 修改 | 表格样式优化 |
| `ui/src/views/UserEdit.vue` | 修改 | 玻璃态表单卡片 |
| `ui/src/views/NotFound.vue` | 修改 | 科技感 404 页面 |
| `ui/src/components/UserSearchForm.vue` | 修改 | 紧凑行内布局样式 |
| `ui/src/components/UserTable.vue` | 修改 | 表格样式优化 |
| `ui/src/components/UserDialog.vue` | 修改 | 玻璃态弹窗 |
| `ui/src/main.js` | 修改 | 引入全局样式 |

---

### Task 1: 安装 sass 依赖

**Files:**
- Modify: `ui/package.json`

- [ ] **Step 1: 安装 sass**

Run: `cd ui && npm install -D sass`

Expected: sass added to devDependencies

- [ ] **Step 2: 验证安装**

Run: `cd ui && npm list sass`

Expected: sass@x.x.x listed

- [ ] **Step 3: 提交**

```bash
git add ui/package.json ui/package-lock.json
git commit -m "chore: add sass dependency for SCSS theme customization"
```

---

### Task 2: 创建样式目录结构和变量文件

**Files:**
- Create: `ui/src/styles/variables.scss`

- [ ] **Step 1: 创建 variables.scss**

```scss
// Element Plus 变量覆盖
@forward 'element-plus/theme-chalk/src/common/var.scss' with (
  $colors: (
    'primary': ('base': #3b82f6),
    'success': ('base': #10b981),
    'warning': ('base': #f59e0b),
    'danger': ('base': #ef4444),
  ),
  $font-size: (
    'extra-small': 12px,
    'small': 14px,
    'base': 14px,
    'medium': 16px,
    'large': 18px,
    'extra-large': 20px
  ),
  $border-radius: (
    'base': 8px,
    'small': 4px,
    'round': 16px,
    'circle': 50%
  ),
  $box-shadow: (
    '': 0 4px 24px rgba(0, 0, 0, 0.08),
    'light': 0 2px 12px rgba(0, 0, 0, 0.04),
    'lighter': 0 1px 6px rgba(0, 0, 0, 0.02),
    'dark': 0 8px 32px rgba(0, 0, 0, 0.12)
  )
);
```

- [ ] **Step 2: 提交**

```bash
git add ui/src/styles/variables.scss
git commit -m "feat: create Element Plus SCSS variables override"
```

---

### Task 3: 创建主题 CSS 变量文件

**Files:**
- Create: `ui/src/styles/themes/light.scss`
- Create: `ui/src/styles/themes/dark.scss`

- [ ] **Step 1: 创建 light.scss**

```scss
// 浅色主题 CSS 变量
:root {
  // 主色调
  --color-primary: #3b82f6;
  --color-accent: #8b5cf6;

  // 背景色
  --color-bg-base: #f8fafc;
  --color-bg-elevated: #ffffff;
  --color-bg-glass: rgba(255, 255, 255, 0.8);
  --color-bg-gradient-start: #f8fafc;
  --color-bg-gradient-end: #e2e8f0;

  // 边框
  --color-border: #e2e8f0;
  --color-border-light: #f1f5f9;

  // 文字
  --color-text-primary: #1e293b;
  --color-text-secondary: #64748b;
  --color-text-muted: #94a3b8;

  // 渐变
  --gradient-primary: linear-gradient(135deg, var(--color-primary), var(--color-accent));
  --gradient-bg: linear-gradient(180deg, var(--color-bg-gradient-start), var(--color-bg-gradient-end));
  --gradient-text: linear-gradient(135deg, var(--color-primary), var(--color-accent));

  // 阴影
  --shadow-glass: 0 4px 24px rgba(0, 0, 0, 0.08);
  --shadow-hover: 0 8px 32px rgba(0, 0, 0, 0.12);
  --shadow-glow: 0 0 20px rgba(59, 130, 246, 0.3);
}
```

- [ ] **Step 2: 创建 dark.scss**

```scss
// 深色主题 CSS 变量
html.dark {
  // 主色调（深色模式下更亮）
  --color-primary: #60a5fa;
  --color-accent: #a78bfa;

  // 背景色
  --color-bg-base: #0f172a;
  --color-bg-elevated: #1e293b;
  --color-bg-glass: rgba(15, 23, 42, 0.7);
  --color-bg-gradient-start: #0f172a;
  --color-bg-gradient-end: #1e1b4b;

  // 边框
  --color-border: #334155;
  --color-border-light: #475569;

  // 文字
  --color-text-primary: #f1f5f9;
  --color-text-secondary: #94a3b8;
  --color-text-muted: #64748b;

  // 渐变
  --gradient-primary: linear-gradient(135deg, var(--color-primary), var(--color-accent));
  --gradient-bg: linear-gradient(180deg, var(--color-bg-gradient-start), var(--color-bg-gradient-end));
  --gradient-text: linear-gradient(135deg, var(--color-primary), var(--color-accent));

  // 阵影（深色模式下更深）
  --shadow-glass: 0 4px 24px rgba(0, 0, 0, 0.3);
  --shadow-hover: 0 8px 32px rgba(0, 0, 0, 0.4);
  --shadow-glow: 0 0 30px rgba(96, 165, 250, 0.4);
}
```

- [ ] **Step 3: 提交**

```bash
git add ui/src/styles/themes/
git commit -m "feat: create light and dark theme CSS variables"
```

---

### Task 4: 创建混入文件

**Files:**
- Create: `ui/src/styles/mixins.scss`

- [ ] **Step 1: 创建 mixins.scss**

```scss
// 玻璃态卡片
@mixin glass-card {
  background: var(--color-bg-glass);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid var(--color-border);
  border-radius: 16px;
  box-shadow: var(--shadow-glass);
}

// 霓虹渐变边框
@mixin neon-border {
  border: 2px solid transparent;
  background:
    linear-gradient(var(--color-bg-elevated), var(--color-bg-elevated)) padding-box,
    linear-gradient(135deg, var(--color-primary), var(--color-accent)) border-box;
  border-radius: 8px;
}

// 渐变按钮
@mixin gradient-button {
  background: var(--gradient-primary);
  border: none;
  color: white;
  border-radius: 8px;
  transition: all 0.3s ease;

  &:hover {
    box-shadow: var(--shadow-glow);
    transform: translateY(-1px);
  }

  &:active {
    transform: translateY(0);
  }
}

// 渐变文字
@mixin gradient-text {
  background: var(--gradient-text);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

// 透明输入框
@mixin transparent-input {
  background: transparent;
  border: none;
  border-bottom: 2px solid var(--color-border);
  border-radius: 0;
  transition: border-color 0.3s ease;

  &:focus {
    border-bottom-color: var(--color-primary);
  }
}

// 平滑过渡
@mixin smooth-transition($properties: all) {
  transition-property: $properties;
  transition-duration: 0.3s;
  transition-timing-function: ease;
}
```

- [ ] **Step 2: 提交**

```bash
git add ui/src/styles/mixins.scss
git commit -m "feat: create SCSS mixins for glass effect, neon border, gradient styles"
```

---

### Task 5: 创建全局样式入口

**Files:**
- Create: `ui/src/styles/global.scss`

- [ ] **Step 1: 创建 global.scss**

```scss
// 全局样式入口
@use './variables.scss' as *;
@use './mixins.scss' as *;
@use './themes/light.scss';
@use './themes/dark.scss';

// Element Plus 深色模式支持
@use 'element-plus/theme-chalk/dark/css-vars.scss' as *;

// 全局基础样式
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html {
  transition: background-color 0.3s ease, color 0.3s ease;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background-color: var(--color-bg-base);
  color: var(--color-text-primary);
  min-height: 100vh;
  transition: background-color 0.3s ease, color 0.3s ease;
}

// 全局链接样式
a {
  color: var(--color-primary);
  text-decoration: none;
  transition: color 0.2s ease;

  &:hover {
    color: var(--color-accent);
  }
}

// 全局滚动条样式（深色模式）
html.dark {
  &::-webkit-scrollbar {
    width: 8px;
    height: 8px;
  }

  &::-webkit-scrollbar-track {
    background: var(--color-bg-base);
  }

  &::-webkit-scrollbar-thumb {
    background: var(--color-border);
    border-radius: 4px;

    &:hover {
      background: var(--color-text-secondary);
    }
  }
}

// Element Plus 组件样式覆盖
.el-card {
  border-radius: 16px;
  border: 1px solid var(--color-border);
  background: var(--color-bg-elevated);
  transition: all 0.3s ease;
}

.el-button--primary {
  @include gradient-button;
}

.el-table {
  --el-table-border-color: var(--color-border);
  --el-table-header-bg-color: var(--color-bg-elevated);
  --el-table-row-hover-bg-color: var(--color-bg-glass);
  --el-table-bg-color: var(--color-bg-elevated);
  --el-table-tr-bg-color: var(--color-bg-elevated);
  --el-table-header-text-color: var(--color-text-primary);
  --el-table-text-color: var(--color-text-primary);
}

.el-dialog {
  border-radius: 16px;
  background: var(--color-bg-elevated);
}

.el-form-item__label {
  color: var(--color-text-primary);
}

.el-input__wrapper {
  background: var(--color-bg-elevated);
  box-shadow: 0 0 0 1px var(--color-border) inset;
  transition: all 0.3s ease;

  &:hover {
    box-shadow: 0 0 0 1px var(--color-primary) inset;
  }

  &.is-focus {
    box-shadow: 0 0 0 1px var(--color-primary) inset, var(--shadow-glow);
  }
}

.el-pagination {
  --el-pagination-bg-color: var(--color-bg-elevated);
  --el-pagination-text-color: var(--color-text-primary);
  --el-pagination-button-bg-color: var(--color-bg-elevated);
  --el-pagination-hover-color: var(--color-primary);
}
```

- [ ] **Step 2: 提交**

```bash
git add ui/src/styles/global.scss
git commit -m "feat: create global stylesheet with Element Plus overrides"
```

---

### Task 6: 配置 Vite SCSS 预处理

**Files:**
- Modify: `ui/vite.config.js`

- [ ] **Step 1: 修改 vite.config.js**

完整替换文件内容：

```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  css: {
    preprocessorOptions: {
      scss: {
        additionalData: `@use "@/styles/variables.scss" as *; @use "@/styles/mixins.scss" as *;`,
        api: 'modern-compiler'
      }
    }
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    sourcemap: false,
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true,
        drop_debugger: true
      }
    },
    rollupOptions: {
      output: {
        manualChunks: {
          'vue-vendor': ['vue', 'vue-router', 'pinia'],
          'element-plus': ['element-plus'],
          'axios': ['axios']
        },
        chunkFileNames: 'assets/js/[name]-[hash].js',
        entryFileNames: 'assets/js/[name]-[hash].js',
        assetFileNames: 'assets/[ext]/[name]-[hash].[ext]'
      }
    },
    chunkSizeWarningLimit: 500
  }
})
```

- [ ] **Step 2: 提交**

```bash
git add ui/vite.config.js
git commit -m "feat: configure Vite SCSS preprocessing with alias and modern compiler"
```

---

### Task 7: 创建 useTheme composable

**Files:**
- Create: `ui/src/composables/useTheme.js`

- [ ] **Step 1: 创建 useTheme.js**

```javascript
import { ref, onMounted, watch } from 'vue'

/**
 * 主题切换 composable
 * 支持深色/浅色模式切换，自动检测系统偏好，持久化到 localStorage
 */
export function useTheme() {
  const theme = ref('light')

  /**
   * 应用主题到 DOM
   */
  const applyTheme = (newTheme) => {
    document.documentElement.classList.toggle('dark', newTheme === 'dark')
    localStorage.setItem('theme-preference', newTheme)
    theme.value = newTheme
  }

  /**
   * 切换主题
   */
  const toggleTheme = () => {
    applyTheme(theme.value === 'light' ? 'dark' : 'light')
  }

  /**
   * 初始化：检测系统偏好 + localStorage
   */
  const initTheme = () => {
    const saved = localStorage.getItem('theme-preference')
    if (saved) {
      applyTheme(saved)
    } else {
      const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
      applyTheme(prefersDark ? 'dark' : 'light')
    }
  }

  // 监听系统偏好变化
  const watchSystemPreference = () => {
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
    mediaQuery.addEventListener('change', (e) => {
      // 只有用户没有手动设置偏好时才跟随系统
      if (!localStorage.getItem('theme-preference')) {
        applyTheme(e.matches ? 'dark' : 'light')
      }
    })
  }

  onMounted(() => {
    initTheme()
    watchSystemPreference()
  })

  return {
    theme,
    applyTheme,
    toggleTheme,
    isDark: () => theme.value === 'dark'
  }
}
```

- [ ] **Step 2: 提交**

```bash
git add ui/src/composables/useTheme.js
git commit -m "feat: create useTheme composable for dark/light mode switching"
```

---

### Task 8: 修改 main.js 引入全局样式

**Files:**
- Modify: `ui/src/main.js`

- [ ] **Step 1: 读取当前 main.js 内容**

Run: `cat ui/src/main.js`

- [ ] **Step 2: 修改 main.js 添加全局样式引入**

```javascript
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import 'element-plus/dist/index.css'
import './styles/global.scss'
import App from './App.vue'
import router from './router'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })

app.mount('#app')
```

- [ ] **Step 3: 提交**

```bash
git add ui/src/main.js
git commit -m "feat: import global SCSS styles in main.js"
```

---

### Task 9: 改造 App.vue

**Files:**
- Modify: `ui/src/App.vue`

- [ ] **Step 1: 重写 App.vue**

```vue
<template>
  <el-config-provider :locale="zhCn">
    <router-view />
  </el-config-provider>
</template>

<script setup>
import { onMounted } from 'vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import { useTheme } from './composables/useTheme'

// 初始化主题
useTheme()
</script>

<style>
/* 全局样式已由 global.scss 处理，这里仅保留必要覆盖 */
</style>
```

- [ ] **Step 2: 提交**

```bash
git add ui/src/App.vue
git commit -m "feat: integrate theme initialization in App.vue"
```

---

### Task 10: 改造 Navbar 组件

**Files:**
- Modify: `ui/src/components/Navbar.vue`

- [ ] **Step 1: 重写 Navbar.vue**

```vue
<template>
  <el-header class="navbar">
    <div class="navbar-content">
      <div class="logo">
        <span class="logo-text">JArsenal</span>
      </div>
      <div class="navbar-actions">
        <el-button class="theme-toggle" circle @click="toggleTheme">
          <el-icon :size="18">
            <component :is="theme === 'light' ? 'Moon' : 'Sunny'" />
          </el-icon>
        </el-button>
        <div class="user-info" v-if="userStore.isLoggedIn()">
          <el-dropdown trigger="click">
            <span class="user-dropdown-trigger">
              <el-avatar :size="32" class="user-avatar">
                {{ userStore.username?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="user-name">{{ userStore.username }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>
  </el-header>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Moon, Sunny, SwitchButton } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { useTheme } from '../composables/useTheme'

const router = useRouter()
const userStore = useUserStore()
const { theme, toggleTheme } = useTheme()

const handleLogout = async () => {
  try {
    await userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch (error) {
    // error handled by interceptor
  }
}
</script>

<style lang="scss" scoped>
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 64px;
  z-index: 1000;
  @include glass-card;
  border-radius: 0 0 16px 16px;
  padding: 0;

  .navbar-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    height: 100%;
    padding: 0 24px;
  }

  .logo {
    .logo-text {
      font-size: 22px;
      font-weight: 700;
      @include gradient-text;
    }
  }

  .navbar-actions {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .theme-toggle {
    background: var(--color-bg-glass);
    border: 1px solid var(--color-border);
    color: var(--color-text-primary);
    transition: all 0.3s ease;

    &:hover {
      background: var(--gradient-primary);
      border: none;
      color: white;
      box-shadow: var(--shadow-glow);
    }
  }

  .user-info {
    .user-dropdown-trigger {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      padding: 4px 12px;
      border-radius: 20px;
      transition: all 0.2s ease;

      &:hover {
        background: var(--color-bg-glass);
      }
    }

    .user-avatar {
      background: var(--gradient-primary);
      color: white;
      font-weight: 600;
    }

    .user-name {
      color: var(--color-text-primary);
      font-size: 14px;
    }
  }
}
</style>
```

- [ ] **Step 2: 提交**

```bash
git add ui/src/components/Navbar.vue
git commit -m "feat: redesign Navbar with glass effect and theme toggle button"
```

---

### Task 11: 改造 Login 页面

**Files:**
- Modify: `ui/src/views/Login.vue`

- [ ] **Step 1: 重写 Login.vue**

```vue
<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h1 class="login-title">JArsenal</h1>
        <p class="login-subtitle">用户登录</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" class="login-form">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            show-password
            :prefix-icon="Lock"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            class="login-button"
            size="large"
            @click="handleLogin"
            :loading="loading"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-footer">
        <el-button text @click="router.push('/register')">
          没有账号？点击注册
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { authApi } from '../api'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  try {
    await formRef.value.validate()
    loading.value = true
    const data = await authApi.login(form.username, form.password)
    userStore.setUser(data)
    ElMessage.success('登录成功')
    router.push('/users')
  } catch (error) {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: var(--gradient-bg);
  padding: 20px;

  // 深色模式下的额外渐变层
  html.dark & {
    background: linear-gradient(135deg, var(--color-bg-base) 0%, #1e1b4b 50%, #0f172a 100%);
  }
}

.login-card {
  width: 420px;
  @include glass-card;
  padding: 40px;

  .login-header {
    text-align: center;
    margin-bottom: 32px;

    .login-title {
      font-size: 32px;
      font-weight: 700;
      @include gradient-text;
      margin-bottom: 8px;
    }

    .login-subtitle {
      color: var(--color-text-secondary);
      font-size: 14px;
    }
  }

  .login-form {
    .el-form-item {
      margin-bottom: 24px;
    }

    .el-input {
      --el-input-bg-color: var(--color-bg-glass);
      --el-input-border-color: var(--color-border);
      --el-input-text-color: var(--color-text-primary);
      --el-input-placeholder-color: var(--color-text-muted);
    }

    .login-button {
      width: 100%;
      @include gradient-button;
      height: 44px;
      font-size: 16px;
    }
  }

  .login-footer {
    text-align: center;
    margin-top: 16px;

    .el-button {
      color: var(--color-text-secondary);

      &:hover {
        color: var(--color-primary);
      }
    }
  }
}
</style>
```

- [ ] **Step 2: 提交**

```bash
git add ui/src/views/Login.vue
git commit -m "feat: redesign Login page with glass card and gradient background"
```

---

### Task 12: 改造 Register 页面

**Files:**
- Modify: `ui/src/views/Register.vue`

- [ ] **Step 1: 重写 Register.vue**

```vue
<template>
  <div class="register-container">
    <div class="register-card">
      <div class="register-header">
        <h1 class="register-title">JArsenal</h1>
        <p class="register-subtitle">用户注册</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" class="register-form">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名 (2-50字符)"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码 (6-100字符)"
            size="large"
            show-password
            :prefix-icon="Lock"
          />
        </el-form-item>
        <el-form-item prop="email">
          <el-input
            v-model="form.email"
            placeholder="请输入邮箱"
            size="large"
            :prefix-icon="Message"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            class="register-button"
            size="large"
            @click="handleRegister"
            :loading="loading"
          >
            注册
          </el-button>
        </el-form-item>
      </el-form>
      <div class="register-footer">
        <el-button text @click="router.push('/login')">
          已有账号？点击登录
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Message } from '@element-plus/icons-vue'
import { authApi } from '../api'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  email: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 50, message: '用户名长度2-50字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度6-100字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  try {
    await formRef.value.validate()
    loading.value = true
    await authApi.register(form.username, form.password, form.email)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: var(--gradient-bg);
  padding: 20px;

  html.dark & {
    background: linear-gradient(135deg, var(--color-bg-base) 0%, #1e1b4b 50%, #0f172a 100%);
  }
}

.register-card {
  width: 480px;
  @include glass-card;
  padding: 40px;

  .register-header {
    text-align: center;
    margin-bottom: 32px;

    .register-title {
      font-size: 32px;
      font-weight: 700;
      @include gradient-text;
      margin-bottom: 8px;
    }

    .register-subtitle {
      color: var(--color-text-secondary);
      font-size: 14px;
    }
  }

  .register-form {
    .el-form-item {
      margin-bottom: 24px;
    }

    .el-input {
      --el-input-bg-color: var(--color-bg-glass);
      --el-input-border-color: var(--color-border);
      --el-input-text-color: var(--color-text-primary);
      --el-input-placeholder-color: var(--color-text-muted);
    }

    .register-button {
      width: 100%;
      @include gradient-button;
      height: 44px;
      font-size: 16px;
    }
  }

  .register-footer {
    text-align: center;
    margin-top: 16px;

    .el-button {
      color: var(--color-text-secondary);

      &:hover {
        color: var(--color-primary);
      }
    }
  }
}
</style>
```

- [ ] **Step 2: 提交**

```bash
git add ui/src/views/Register.vue
git commit -m "feat: redesign Register page matching Login page style"
```

---

### Task 13: 改造 UserList 页面

**Files:**
- Modify: `ui/src/views/UserList.vue`

- [ ] **Step 1: 重写 UserList.vue**

```vue
<template>
  <Navbar />
  <div class="user-list-container">
    <div class="content-card">
      <!-- 搜索表单 -->
      <UserSearchForm ref="searchFormRef" @search="handleSearch" @reset="handleReset" />

      <!-- 操作按钮 -->
      <div class="action-bar">
        <el-button class="create-button" @click="showCreateDialog">
          <el-icon><Plus /></el-icon>
          新增用户
        </el-button>
      </div>

      <!-- 用户列表 -->
      <UserTable
        :users="users"
        :loading="loading"
        @edit="handleEdit"
        @refresh="fetchUsers"
      />

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[5, 10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchUsers"
          @current-change="fetchUsers"
        />
      </div>
    </div>

    <!-- 创建/编辑弹窗 -->
    <UserDialog
      v-model="dialogVisible"
      :is-edit="isEdit"
      :user-id="editId"
      :user-data="editUserData"
      @success="fetchUsers"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { usePagination } from '../composables/usePagination'
import { userApi } from '../api'
import Navbar from '../components/Navbar.vue'
import UserSearchForm from '../components/UserSearchForm.vue'
import UserTable from '../components/UserTable.vue'
import UserDialog from '../components/UserDialog.vue'

const router = useRouter()
const searchFormRef = ref()

// 使用分页 composable
const { pageNum, pageSize, total, resetPagination, getPaginationParams } = usePagination(10)

// 用户列表数据
const loading = ref(false)
const users = ref([])
const searchParams = ref({})

// 弹窗状态
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const editUserData = ref(null)

// 获取用户列表
const fetchUsers = async () => {
  loading.value = true
  try {
    const data = await userApi.list({
      ...getPaginationParams(),
      ...searchParams.value
    })
    users.value = data.list
    total.value = data.total
  } catch (error) {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = (params) => {
  searchParams.value = params
  pageNum.value = 1
  fetchUsers()
}

// 重置搜索
const handleReset = () => {
  searchParams.value = {}
  resetPagination()
  fetchUsers()
}

// 显示创建弹窗
const showCreateDialog = () => {
  isEdit.value = false
  editId.value = null
  editUserData.value = null
  dialogVisible.value = true
}

// 编辑用户
const handleEdit = (id) => {
  router.push(`/users/${id}`)
}

onMounted(() => {
  fetchUsers()
})
</script>

<style lang="scss" scoped>
.user-list-container {
  padding: 84px 24px 24px 24px; // 顶部留出 Navbar 空间
  min-height: 100vh;
  background: var(--color-bg-base);
}

.content-card {
  max-width: 1200px;
  margin: 0 auto;
  @include glass-card;
  padding: 24px;
}

.action-bar {
  margin-bottom: 16px;
  display: flex;
  justify-content: flex-start;

  .create-button {
    @include gradient-button;
    padding: 10px 20px;

    .el-icon {
      margin-right: 4px;
    }
  }
}

.pagination-wrapper {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}
</style>
```

- [ ] **Step 2: 提交**

```bash
git add ui/src/views/UserList.vue
git commit -m "feat: redesign UserList page with glass card and optimized layout"
```

---

### Task 14: 改造 UserEdit 页面

**Files:**
- Modify: `ui/src/views/UserEdit.vue`

- [ ] **Step 1: 重写 UserEdit.vue**

```vue
<template>
  <Navbar />
  <div class="user-edit-container">
    <div class="edit-card">
      <div class="edit-header">
        <el-button class="back-button" @click="router.push('/users')">
          <el-icon><ArrowLeft /></el-icon>
          返回列表
        </el-button>
        <h2 class="edit-title">{{ isEdit ? '编辑用户' : '查看用户' }}</h2>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" v-loading="loading">
        <el-form-item label="ID">
          <el-input v-model="form.id" disabled />
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="创建时间">
          <el-input v-model="form.createTime" disabled />
        </el-form-item>
        <el-form-item label="更新时间">
          <el-input v-model="form.updateTime" disabled />
        </el-form-item>
        <el-form-item class="action-buttons">
          <el-button class="save-button" @click="handleSave" :loading="submitLoading">
            <el-icon><Check /></el-icon>
            保存
          </el-button>
          <el-button @click="router.push('/users')">
            取消
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Check } from '@element-plus/icons-vue'
import { userApi } from '../api'
import Navbar from '../components/Navbar.vue'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const submitLoading = ref(false)
const formRef = ref()
const isEdit = ref(true)

const form = reactive({
  id: '',
  username: '',
  email: '',
  status: 1,
  createTime: '',
  updateTime: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度2-20字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const fetchUser = async () => {
  loading.value = true
  try {
    const data = await userApi.get(route.params.id)
    form.id = data.id
    form.username = data.username
    form.email = data.email
    form.status = data.status
    form.createTime = data.createTime
    form.updateTime = data.updateTime
  } catch (error) {
    router.push('/users')
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  try {
    await formRef.value.validate()
    submitLoading.value = true
    await userApi.update(form.id, {
      username: form.username,
      email: form.email,
      status: form.status
    })
    ElMessage.success('保存成功')
    fetchUser()
  } catch (error) {
    // error handled by interceptor
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  fetchUser()
})
</script>

<style lang="scss" scoped>
.user-edit-container {
  padding: 84px 24px 24px 24px;
  min-height: 100vh;
  background: var(--color-bg-base);
  display: flex;
  justify-content: center;
}

.edit-card {
  width: 100%;
  max-width: 600px;
  @include glass-card;
  padding: 24px;

  .edit-header {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 24px;

    .back-button {
      background: var(--color-bg-glass);
      border: 1px solid var(--color-border);
      color: var(--color-text-primary);
      transition: all 0.3s ease;

      &:hover {
        background: var(--color-border);
      }

      .el-icon {
        margin-right: 4px;
      }
    }

    .edit-title {
      @include gradient-text;
      font-size: 20px;
      font-weight: 600;
    }
  }

  .el-form {
    .el-form-item {
      margin-bottom: 20px;
    }

    .el-input {
      --el-input-bg-color: var(--color-bg-glass);
      --el-input-border-color: var(--color-border);
      --el-input-text-color: var(--color-text-primary);
      --el-input-placeholder-color: var(--color-text-muted);
    }

    .action-buttons {
      margin-top: 32px;

      .save-button {
        @include gradient-button;
        padding: 10px 24px;

        .el-icon {
          margin-right: 4px;
        }
      }
    }
  }
}
</style>
```

- [ ] **Step 2: 提交**

```bash
git add ui/src/views/UserEdit.vue
git commit -m "feat: redesign UserEdit page with glass card and back button"
```

---

### Task 15: 改造 NotFound 页面

**Files:**
- Modify: `ui/src/views/NotFound.vue`

- [ ] **Step 1: 重写 NotFound.vue**

```vue
<template>
  <div class="not-found-container">
    <div class="not-found-content">
      <h1 class="error-code">404</h1>
      <p class="error-message">页面不存在</p>
      <p class="error-description">您访问的页面可能已被移除或暂时不可用</p>
      <el-button class="home-button" @click="goHome">
        <el-icon><HomeFilled /></el-icon>
        返回首页
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { HomeFilled } from '@element-plus/icons-vue'

const router = useRouter()

const goHome = () => {
  router.push('/')
}
</script>

<style lang="scss" scoped>
.not-found-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: var(--gradient-bg);

  html.dark & {
    background: linear-gradient(135deg, var(--color-bg-base) 0%, #1e1b4b 50%, #0f172a 100%);
  }
}

.not-found-content {
  text-align: center;
  @include glass-card;
  padding: 48px;

  .error-code {
    font-size: 72px;
    font-weight: 700;
    @include gradient-text;
    margin-bottom: 16px;
    line-height: 1;
  }

  .error-message {
    font-size: 24px;
    color: var(--color-text-primary);
    margin-bottom: 8px;
  }

  .error-description {
    font-size: 14px;
    color: var(--color-text-secondary);
    margin-bottom: 32px;
  }

  .home-button {
    @include gradient-button;
    padding: 12px 32px;
    font-size: 16px;

    .el-icon {
      margin-right: 8px;
    }
  }
}
</style>
```

- [ ] **Step 2: 提交**

```bash
git add ui/src/views/NotFound.vue
git commit -m "feat: redesign NotFound page with glass card and gradient text"
```

---

### Task 16: 改造 UserSearchForm 组件

**Files:**
- Modify: `ui/src/components/UserSearchForm.vue`

- [ ] **Step 1: 重写 UserSearchForm.vue**

```vue
<template>
  <el-form inline class="search-form">
    <el-form-item>
      <el-input
        v-model="searchForm.username"
        placeholder="搜索用户名"
        clearable
        :prefix-icon="Search"
        @keyup.enter="handleSearch"
        class="search-input"
      />
    </el-form-item>
    <el-form-item>
      <el-select
        v-model="searchForm.status"
        placeholder="状态"
        clearable
        class="status-select"
      >
        <el-option label="正常" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
    </el-form-item>
    <el-form-item>
      <el-button class="search-button" @click="handleSearch">
        <el-icon><Search /></el-icon>
        搜索
      </el-button>
      <el-button class="reset-button" @click="handleReset">
        <el-icon><Refresh /></el-icon>
        重置
      </el-button>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { reactive } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'

const emit = defineEmits(['search', 'reset'])

const searchForm = reactive({
  username: '',
  status: null
})

const handleSearch = () => {
  emit('search', {
    username: searchForm.username || undefined,
    status: searchForm.status ?? undefined
  })
}

const handleReset = () => {
  searchForm.username = ''
  searchForm.status = null
  emit('reset')
}

defineExpose({
  reset: () => {
    searchForm.username = ''
    searchForm.status = null
  }
})
</script>

<style lang="scss" scoped>
.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;

  .el-form-item {
    margin-bottom: 0;
  }

  .search-input {
    width: 200px;

    :deep(.el-input__wrapper) {
      background: var(--color-bg-glass);
      box-shadow: 0 0 0 1px var(--color-border) inset;
      transition: all 0.3s ease;

      &:hover {
        box-shadow: 0 0 0 1px var(--color-primary) inset;
      }

      &.is-focus {
        box-shadow: 0 0 0 1px var(--color-primary) inset, var(--shadow-glow);
      }
    }
  }

  .status-select {
    width: 120px;

    :deep(.el-input__wrapper) {
      background: var(--color-bg-glass);
      box-shadow: 0 0 0 1px var(--color-border) inset;
      transition: all 0.3s ease;

      &:hover {
        box-shadow: 0 0 0 1px var(--color-primary) inset;
      }

      &.is-focus {
        box-shadow: 0 0 0 1px var(--color-primary) inset, var(--shadow-glow);
      }
    }
  }

  .search-button {
    @include gradient-button;
    padding: 8px 16px;

    .el-icon {
      margin-right: 4px;
    }
  }

  .reset-button {
    background: var(--color-bg-glass);
    border: 1px solid var(--color-border);
    color: var(--color-text-primary);
    transition: all 0.3s ease;

    &:hover {
      background: var(--color-border);
    }

    .el-icon {
      margin-right: 4px;
    }
  }
}
</style>
```

- [ ] **Step 2: 提交**

```bash
git add ui/src/components/UserSearchForm.vue
git commit -m "feat: redesign UserSearchForm with gradient buttons and transparent inputs"
```

---

### Task 17: 改造 UserTable 组件

**Files:**
- Modify: `ui/src/components/UserTable.vue`

- [ ] **Step 1: 重写 UserTable.vue**

```vue
<template>
  <el-table :data="users" v-loading="loading" class="user-table">
    <el-table-column prop="id" label="ID" width="80" align="center" />
    <el-table-column prop="username" label="用户名" min-width="120" />
    <el-table-column prop="email" label="邮箱" min-width="180" />
    <el-table-column prop="status" label="状态" width="100" align="center">
      <template #default="{ row }">
        <el-tag
          :type="row.status === 1 ? 'success' : 'danger'"
          effect="plain"
          class="status-tag"
        >
          {{ row.status === 1 ? '正常' : '禁用' }}
        </el-tag>
      </template>
    </el-table-column>
    <el-table-column prop="createTime" label="创建时间" width="180" />
    <el-table-column label="操作" width="160" align="center">
      <template #default="{ row }">
        <el-button class="edit-button" link @click="handleEdit(row.id)">
          <el-icon><Edit /></el-icon>
          编辑
        </el-button>
        <el-button class="delete-button" link @click="handleDelete(row)">
          <el-icon><Delete /></el-icon>
          删除
        </el-button>
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup>
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, Delete } from '@element-plus/icons-vue'
import { userApi } from '../api'

const props = defineProps({
  users: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['edit', 'refresh'])

const handleEdit = (id) => {
  emit('edit', id)
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除用户 "${row.username}"？`, '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await userApi.delete(row.id)
    ElMessage.success('删除成功')
    emit('refresh')
  } catch (error) {
    if (error !== 'cancel') {
      // error handled by interceptor
    }
  }
}
</script>

<style lang="scss" scoped>
.user-table {
  border-radius: 8px;

  :deep(.el-table__header-wrapper) {
    th {
      background: var(--color-bg-elevated);
      color: var(--color-text-primary);
      font-weight: 600;
    }
  }

  :deep(.el-table__row) {
    transition: all 0.2s ease;

    &:hover > td {
      background: var(--color-bg-glass) !important;
    }
  }

  :deep(td) {
    border-bottom: 1px solid var(--color-border-light);
  }

  .status-tag {
    border-radius: 4px;
    font-size: 12px;
  }

  .edit-button {
    color: var(--color-primary);
    transition: all 0.2s ease;

    &:hover {
      color: var(--color-accent);
    }

    .el-icon {
      margin-right: 4px;
    }
  }

  .delete-button {
    color: var(--el-color-danger);
    transition: all 0.2s ease;

    &:hover {
      color: #ef4444;
    }

    .el-icon {
      margin-right: 4px;
    }
  }
}
</style>
```

- [ ] **Step 2: 提交**

```bash
git add ui/src/components/UserTable.vue
git commit -m "feat: redesign UserTable with hover effects and styled action buttons"
```

---

### Task 18: 改造 UserDialog 组件

**Files:**
- Modify: `ui/src/components/UserDialog.vue`

- [ ] **Step 1: 重写 UserDialog.vue**

```vue
<template>
  <el-dialog
    v-model="visible"
    :title="title"
    width="420px"
    @closed="handleClosed"
    class="user-dialog"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="用户名" prop="username">
        <el-input v-model="form.username" placeholder="请输入用户名" />
      </el-form-item>
      <el-form-item label="密码" prop="password" v-if="!isEdit">
        <el-input
          v-model="form.password"
          type="password"
          placeholder="请输入密码 (6-100字符)"
          show-password
        />
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="form.email" placeholder="请输入邮箱" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio :value="1">正常</el-radio>
          <el-radio :value="0">禁用</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button class="cancel-button" @click="visible = false">取消</el-button>
        <el-button class="submit-button" @click="handleSubmit" :loading="submitLoading">
          确定
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { userApi } from '../api'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  isEdit: {
    type: Boolean,
    default: false
  },
  userId: {
    type: Number,
    default: null
  },
  userData: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const title = computed(() => props.isEdit ? '编辑用户' : '新增用户')

const formRef = ref()
const submitLoading = ref(false)

const form = reactive({
  username: '',
  password: '',
  email: '',
  status: 1
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度2-20字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度6-100字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

watch(() => props.userData, (data) => {
  if (data && props.isEdit) {
    form.username = data.username || ''
    form.email = data.email || ''
    form.status = data.status ?? 1
  }
}, { immediate: true })

const handleClosed = () => {
  formRef.value?.resetFields()
  form.username = ''
  form.password = ''
  form.email = ''
  form.status = 1
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitLoading.value = true

    if (props.isEdit) {
      await userApi.update(props.userId, {
        username: form.username,
        email: form.email,
        status: form.status
      })
      ElMessage.success('更新成功')
    } else {
      await userApi.create({
        username: form.username,
        password: form.password,
        email: form.email,
        status: form.status
      })
      ElMessage.success('创建成功')
    }

    visible.value = false
    emit('success')
  } catch (error) {
    // error handled by interceptor
  } finally {
    submitLoading.value = false
  }
}

defineExpose({
  reset: handleClosed
})
</script>

<style lang="scss" scoped>
.user-dialog {
  :deep(.el-dialog) {
    @include glass-card;
    border-radius: 16px;

    .el-dialog__header {
      padding: 20px 24px 16px;
      border-bottom: 1px solid var(--color-border-light);

      .el-dialog__title {
        @include gradient-text;
        font-weight: 600;
      }
    }

    .el-dialog__body {
      padding: 24px;

      .el-form-item {
        margin-bottom: 20px;
      }

      .el-input {
        --el-input-bg-color: var(--color-bg-glass);
        --el-input-border-color: var(--color-border);
        --el-input-text-color: var(--color-text-primary);
        --el-input-placeholder-color: var(--color-text-muted);
      }
    }

    .el-dialog__footer {
      padding: 16px 24px 20px;
      border-top: 1px solid var(--color-border-light);
    }
  }

  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;

    .cancel-button {
      background: var(--color-bg-glass);
      border: 1px solid var(--color-border);
      color: var(--color-text-primary);
      transition: all 0.3s ease;

      &:hover {
        background: var(--color-border);
      }
    }

    .submit-button {
      @include gradient-button;
      padding: 8px 24px;
    }
  }
}
</style>
```

- [ ] **Step 2: 提交**

```bash
git add ui/src/components/UserDialog.vue
git commit -m "feat: redesign UserDialog with glass effect and gradient buttons"
```

---

### Task 19: 测试验证

- [ ] **Step 1: 启动开发服务器**

Run: `cd ui && npm run dev`

Expected: Server running on http://localhost:3000

- [ ] **Step 2: 验证浅色模式**

手动测试：
1. 访问 http://localhost:3000
2. 检查登录页渐变背景、玻璃态卡片
3. 登录后检查导航栏玻璃态效果
4. 检查用户列表页表格、分页样式
5. 点击编辑检查表单卡片样式
6. 检查 404 页面样式

- [ ] **Step 3: 验证深色模式**

手动测试：
1. 点击导航栏主题切换按钮（月亮图标）
2. 检查所有页面深色模式样式
3. 刷新页面，确认主题偏好保持
4. 检查 Element Plus 组件深色模式适配

- [ ] **Step 4: 验证主题切换流畅性**

手动测试：
1. 连续切换主题 5 次
2. 确认过渡动画流畅（无闪烁）
3. 确认 localStorage 存储 `theme-preference`

- [ ] **Step 5: 构建生产版本**

Run: `cd ui && npm run build`

Expected: Build successful, dist folder created

---

### Task 20: 最终提交

- [ ] **Step 1: 查看所有变更**

Run: `git status`

- [ ] **Step 2: 最终提交（如有遗漏）**

```bash
git add -A
git commit -m "feat: complete frontend enterprise tech theme with dark/light mode support"
```

- [ ] **Step 3: 推送到远程**

```bash
git push origin main
```

---

## 自检清单

| 检查项 | 状态 |
|--------|------|
| Spec 所有要求都有对应 Task | ✅ |
| 无 TBD/TODO 占位符 | ✅ |
| 类型/函数名一致性 | ✅ |
| 文件路径精确 | ✅ |
| 代码完整无缺失 | ✅ |
| 提交信息规范 | ✅ |