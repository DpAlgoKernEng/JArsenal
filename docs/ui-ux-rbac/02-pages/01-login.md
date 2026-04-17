# 登录页

## 页面信息

| 属性 | 值 |
|------|-----|
| **路由** | `/login` |
| **功能** | 用户身份认证入口 |
| **权限** | 无需登录 |

---

## 页面布局

```
┌─────────────────────────────────────────────────────────────────────┐
│                                                                      │
│                    ┌──────────────────────────┐                      │
│                    │      [系统 Logo]         │                      │
│                    │                          │                      │
│                    │  企业权限管理平台         │                      │
│                    └──────────────────────────┘                      │
│                                                                      │
│                    ┌──────────────────────────┐                      │
│                    │                          │                      │
│                    │  ┌────────────────────┐  │                      │
│                    │  │ 👤 用户名/邮箱     │  │                      │
│                    │  └────────────────────┘  │                      │
│                    │                          │                      │
│                    │  ┌────────────────────┐  │                      │
│                    │  │ 🔒 密码            │  │                      │
│                    │  │              [👁]  │  │                      │
│                    │  └────────────────────┘  │                      │
│                    │                          │                      │
│                    │  ☑ 记住登录状态           │                      │
│                    │                          │                      │
│                    │  ┌────────────────────┐  │                      │
│                    │  │      登 录         │  │                      │
│                    │  └────────────────────┘  │                      │
│                    │                          │                      │
│                    │  忘记密码？ | 帮助中心     │                      │
│                    │                          │                      │
│                    └──────────────────────────┘                      │
│                                                                      │
│                    ─────────── 其他登录方式 ───────────               │
│                    [企业微信] [钉钉] [SSO]                            │
│                                                                      │
│                    © 2024 企业名称 · 安全登录                         │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 组件结构

### 顶部 Logo 区

```
┌──────────────────────────┐
│      [系统 Logo]         │  ← Logo 图片或 SVG，尺寸建议 120x40px
│                          │
│  企业权限管理平台         │  ← 系统名称，字号 24px，字重 600
└──────────────────────────┘

CSS 规范:
.container {
  margin-bottom: 32px;
  text-align: center;
}

.logo-image {
  width: 120px;
  height: 40px;
  margin-bottom: 16px;
}

.system-name {
  font-size: 24px;
  font-weight: 600;
  color: var(--color-foreground);
}
```

### 登录表单卡片

```
┌──────────────────────────┐
│                          │  ← 卡片宽度 400px，圆角 12px
│  用户名/邮箱输入框       │    阴影 elevation-3
│                          │
│  密码输入框              │
│                          │
│  ☑ 记住登录状态          │
│                          │
│  [    登 录    ]         │  ← 按钮高度 48px，全宽
│                          │
│  忘记密码？ | 帮助中心    │  ← 辅助链接，字号 12px
│                          │
└──────────────────────────┘

CSS 规范:
.login-card {
  width: 400px;
  padding: 32px;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-3);
}
```

### 输入框组件

```
┌────────────────────────────┐
│ 👤  │ 用户名/邮箱         │  ← icon 左侧，输入右侧
└────────────────────────────┘

CSS 规范:
.input-wrapper {
  display: flex;
  align-items: center;
  height: 44px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-background);
}

.input-wrapper:focus-within {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 2px rgba(var(--color-primary-rgb), 0.1);
}

.input-icon {
  width: 20px;
  height: 20px;
  margin-left: 12px;
  color: var(--color-muted-foreground);
}

.input-field {
  flex: 1;
  height: 100%;
  padding: 0 12px;
  border: none;
  background: transparent;
  font-size: var(--font-size-base);
}

.input-field:focus {
  outline: none;
}
```

### 密码输入框（含显示/隐藏按钮）

```
┌────────────────────────────┐
│ 🔒  │ 密码        │ [👁] │  ← 右侧显示/隐藏按钮
└────────────────────────────┘

交互逻辑:
- 点击 👁 图标切换密码显示/隐藏
- 图标状态:
  - 显示密码: eye-open 图标
  - 隐藏密码: eye-off 图标
```

### 记住登录状态

```
☑ 记住登录状态

CSS 规范:
.checkbox-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 24px;
}

.checkbox {
  width: 16px;
  height: 16px;
  accent-color: var(--color-primary);
}

.checkbox-label {
  font-size: var(--font-size-sm);
  color: var(--color-foreground);
}
```

### 登录按钮

```
┌────────────────────────────┐
│          登 录             │
└────────────────────────────┘

CSS 规范:
.login-button {
  width: 100%;
  height: 48px;
  background: var(--color-primary);
  color: var(--color-on-primary);
  font-size: var(--font-size-lg);
  font-weight: 500;
  border-radius: var(--radius-md);
  border: none;
  cursor: pointer;
  transition: background var(--duration-normal) var(--ease-in-out);
}

.login-button:hover {
  background: var(--color-primary-hover); /* 加深 10% */
}

.login-button:active {
  transform: scale(0.98);
}

.login-button:disabled {
  background: var(--color-muted);
  color: var(--color-muted-foreground);
  cursor: not-allowed;
}
```

### 加载状态

```
┌────────────────────────────┐
│      [-spinner] 登录中... │  ← 点击后显示 loading spinner
└────────────────────────────┘

CSS 规范:
.login-button.loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.spinner {
  width: 20px;
  height: 20px;
  animation: spin 1s linear infinite;
}
```

### 辅助链接

```
忘记密码？ | 帮助中心

CSS 规范:
.links {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 16px;
  font-size: var(--font-size-sm);
}

.link {
  color: var(--color-primary);
  text-decoration: none;
}

.link:hover {
  text-decoration: underline;
}
```

### 其他登录方式

```
─────────── 其他登录方式 ───────────

[企业微信] [钉钉] [SSO]

CSS 规范:
.divider {
  display: flex;
  align-items: center;
  margin: 32px 0 24px;
  color: var(--color-muted-foreground);
  font-size: var(--font-size-sm);
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: var(--color-border);
}

.oauth-buttons {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.oauth-button {
  width: 48px;
  height: 48px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-background);
  cursor: pointer;
  transition: border-color var(--duration-fast);
}

.oauth-button:hover {
  border-color: var(--color-primary);
}
```

### 底部版权

```
© 2024 企业名称 · 安全登录

CSS 规范:
.footer {
  text-align: center;
  margin-top: 48px;
  font-size: var(--font-size-xs);
  color: var(--color-muted-foreground);
}
```

---

## 交互流程

### 登录流程

```
1. 用户输入用户名/邮箱
2. 用户输入密码
3. (可选)勾选"记住登录状态"
4. 点击"登录"按钮
5. 按钮 show loading spinner
6. 请求发送到后端

成功响应:
  - 存储 JWT Token
  - 存储 userId
  - 如果勾选"记住"，localStorage 存储；否则 sessionStorage
  - 跳转到首页 /

失败响应:
  - 显示错误提示 Toast
  - 错误类型:
    - 用户名不存在: "用户不存在"
    - 密码错误: "密码错误，请重试"
    - 账号锁定: "账号已被锁定，请联系管理员"
    - 网络错误: "网络异常，请稍后重试"
  - 按钮 restore to normal state
```

### 表单验证

| 字段 | 验证规则 | 错误提示 |
|------|---------|---------|
| 用户名 | 必填，长度 3-50 | "请输入用户名" |
| 密码 | 必填，长度 ≥6 | "请输入密码" |

验证时机:
- 点击登录时验证
- 实时验证（可选）: 输入时清除对应错误提示

---

## 错误处理

### 输入错误

```
┌────────────────────────────┐
│ 👤  │ 用户名              │
└────────────────────────────┘
│ ⚠ 请输入用户名             │  ← 红色错误提示，输入框下方
```

### 登录失败 Toast

```
┌────────────────────────────────┐
│ ⚠ 密码错误，请重试            │  ← 右上角 Toast，3s 自动消失
│                          [×]   │
└────────────────────────────────┘
```

### 验证码（可选）

```
┌────────────────────────────┐
│ 请输入验证码               │
│ ┌──────────┐ ┌──────────┐ │
│ │          │ │ [图片]   │ │  ← 验证码输入框 + 图片
│ └──────────┘ └──────────┘ │
│              [换一张]      │
└────────────────────────────┘
```

---

## 页面状态

### 默认状态

- 所有输入框为空
- 按钮可点击
- 无错误提示

### Loading 状态

- 按钮显示 spinner + "登录中..."
- 按钮禁用
- 输入框禁用

### 错误状态

- 输入框边框变红
- 错误文字提示
- Toast 提示登录失败原因

### 成功状态

- 短暂显示成功 Toast "登录成功"
- 跳转到首页

---

## 响应式适配

### 平板 (< 768px)

```
卡片宽度: 100% (max-width: 400px)
padding: 24px
```

### 手机 (< 480px)

```
卡片宽度: 100%
padding: 20px
margin: 16px
Logo 尺寸: 100x33px
按钮高度: 44px
```

---

## 安全增强

### 密码输入框

- 默认隐藏密码
- 点击图标切换显示/隐藏
- 禁止复制密码内容

### 防暴力破解

- 登录失败 5 次后显示验证码
- 连续失败锁定账号 30 分钟
- 登录失败 Toast 不提示具体原因（可选）

### HTTPS

- 强制 HTTPS 协议
- Token 存储加密

---

## 无障碍

| 要求 | 实现 |
|------|-----|
| **键盘导航** | Tab 按顺序切换: 用户名 → 密码 → 记住 → 登录 |
| **Focus 状态** | 输入框 focus 时边框高亮 |
| **屏幕阅读器** | 输入框 aria-label="用户名" aria-label="密码" |
| **错误通知** | aria-live="polite" 通知登录失败 |
| **图标按钮** | aria-label="显示密码" / aria-label="隐藏密码" |

---

## Vue 组件示例

```vue
<template>
  <div class="login-page">
    <!-- Logo -->
    <div class="logo-section">
      <img src="/logo.svg" alt="系统Logo" class="logo" />
      <h1 class="system-name">企业权限管理平台</h1>
    </div>

    <!-- 登录表单 -->
    <div class="login-card">
      <form @submit.prevent="handleLogin">
        <!-- 用户名 -->
        <div class="input-group" :class="{ error: errors.username }">
          <div class="input-wrapper">
            <User class="input-icon" />
            <input
              v-model="form.username"
              type="text"
              placeholder="用户名/邮箱"
              aria-label="用户名"
              @blur="validateUsername"
            />
          </div>
          <span v-if="errors.username" class="error-text">{{ errors.username }}</span>
        </div>

        <!-- 密码 -->
        <div class="input-group" :class="{ error: errors.password }">
          <div class="input-wrapper">
            <Lock class="input-icon" />
            <input
              v-model="form.password"
              :type="showPassword ? 'text' : 'password'"
              placeholder="密码"
              aria-label="密码"
              @blur="validatePassword"
            />
            <button
              type="button"
              class="toggle-password"
              @click="showPassword = !showPassword"
              :aria-label="showPassword ? '隐藏密码' : '显示密码'"
            >
              <Eye v-if="showPassword" />
              <EyeOff v-else />
            </button>
          </div>
          <span v-if="errors.password" class="error-text">{{ errors.password }}</span>
        </div>

        <!-- 记住登录 -->
        <div class="remember-me">
          <input
            v-model="form.remember"
            type="checkbox"
            id="remember"
          />
          <label for="remember">记住登录状态</label>
        </div>

        <!-- 登录按钮 -->
        <button
          type="submit"
          class="login-button"
          :disabled="loading"
          :class="{ loading }"
        >
          <Loader2 v-if="loading" class="spinner" />
          <span>{{ loading ? '登录中...' : '登录' }}</span>
        </button>
      </form>

      <!-- 辅助链接 -->
      <div class="links">
        <a href="/forgot-password">忘记密码？</a>
        <span>|</span>
        <a href="/help">帮助中心</a>
      </div>
    </div>

    <!-- 其他登录方式 -->
    <div class="oauth-section">
      <div class="divider">其他登录方式</div>
      <div class="oauth-buttons">
        <button class="oauth-button" aria-label="企业微信登录">
          <Wechat />
        </button>
        <button class="oauth-button" aria-label="钉钉登录">
          <Dingtalk />
        </button>
        <button class="oauth-button" aria-label="SSO登录">
          <Key />
        </button>
      </div>
    </div>

    <!-- 底部 -->
    <div class="footer">
      © 2024 企业名称 · 安全登录
    </div>
  </div>
</template>
```