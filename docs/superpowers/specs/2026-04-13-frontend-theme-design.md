# 前端企业级科技感主题设计

> 创建日期：2026-04-13
> 状态：待确认

## 一、概述

将前端页面从基础的蓝色主题改造为现代科技风格，支持深色/浅色模式切换，类似 Vercel/Linear/Stripe 的设计风格。

**改造范围**：全部页面（登录、注册、用户列表、用户编辑、导航栏）

**实现方案**：Element Plus 官方 SCSS 变量系统定制主题

---

## 二、主题系统架构

### 2.1 目录结构

```
ui/src/
├── styles/
│   ├── variables.scss       # 主题变量定义（颜色、间距、字体）
│   ├── themes/
│   │   ├── light.scss       # 浅色主题变量覆盖
│   │   └── dark.scss        # 深色主题变量覆盖
│   ├── mixins.scss          # 常用样式混入（玻璃态、渐变）
│   └── global.scss          # 全局样式入口
├── composables/
│   └── useTheme.js          # 主题切换逻辑
└── App.vue                  # 主题Provider入口
```

### 2.2 主题切换机制

- 使用 `html` 根元素的 `class="dark"` 切换
- Element Plus 自动识别 `dark` 类并应用深色变量
- 自定义变量通过 CSS 变量 `--color-*` 传递
- 用户偏好存储在 `localStorage`（key: `theme-preference`）

---

## 三、颜色系统与视觉效果

### 3.1 核心颜色定义

| 变量名 | 浅色模式 | 深色模式 | 用途 |
|--------|---------|---------|------|
| `--color-primary` | `#3b82f6` | `#60a5fa` | 主按钮、链接 |
| `--color-accent` | `#8b5cf6` | `#a78bfa` | 渐变终点、强调 |
| `--color-bg-base` | `#f8fafc` | `#0f172a` | 页面背景 |
| `--color-bg-elevated` | `#ffffff` | `#1e293b` | 卡片、弹窗 |
| `--color-bg-glass` | `rgba(255,255,255,0.8)` | `rgba(15,23,42,0.7)` | 玻璃态效果 |
| `--color-border` | `#e2e8f0` | `#334155` | 边框 |
| `--color-text-primary` | `#1e293b` | `#f1f5f9` | 主文字 |
| `--color-text-secondary` | `#64748b` | `#94a3b8` | 次文字 |

### 3.2 视觉效果混入

**玻璃态卡片：**
```scss
@mixin glass-card {
  background: var(--color-bg-glass);
  backdrop-filter: blur(12px);
  border: 1px solid var(--color-border);
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.1);
}
```

**霓虹渐变边框：**
```scss
@mixin neon-border {
  border: 2px solid transparent;
  background:
    linear-gradient(var(--color-bg-elevated), var(--color-bg-elevated)) padding-box,
    linear-gradient(135deg, var(--color-primary), var(--color-accent)) border-box;
}
```

---

## 四、页面组件改造

### 4.1 导航栏 (Navbar)

- 玻璃态背景，固定顶部
- Logo 左侧，右侧用户信息 + 主题切换按钮
- 深色模式：微弱的蓝紫渐变底色
- 高度：64px，圆角底部边缘
- 交互：主题切换按钮（太阳/月亮图标），用户下拉菜单

### 4.2 登录页 (Login)

- 全屏渐变背景（浅色：灰白渐变，深色：深蓝紫渐变）
- 中央玻璃态登录卡片，居中悬浮
- Logo + 标题在卡片顶部，带渐变色文字
- 输入框：无边框透明背景，底部渐变线
- 登录按钮：霓虹渐变背景，hover 发光效果
- 底部注册链接：简洁文字链

### 4.3 注册页 (Register)

- 与登录页相同背景风格
- 玻璃态卡片，稍宽（480px）
- 表单验证错误提示：卡片内 inline 显示
- 注册按钮风格与登录一致

### 4.4 用户列表页 (UserList)

- 背景保持简洁，突出内容区
- 搜索表单：紧凑行内布局，输入框风格统一
- 表格：去掉默认边框，行 hover 高亮，深色模式行分隔线微弱
- 新增按钮：渐变背景，左上角固定
- 分页：简洁数字样式，当前页渐变高亮

### 4.5 用户编辑页 (UserEdit)

- 玻璃态卡片包裹表单
- 返回按钮：左上角，图标+文字
- 保存按钮：与新增按钮风格一致
- 表单布局：两列网格（浅色）或单列堆叠（深色更清晰）

---

## 五、技术实现细节

### 5.1 主题切换逻辑

```javascript
// composables/useTheme.js
export function useTheme() {
  const theme = ref(localStorage.getItem('theme-preference') || 'light')

  const applyTheme = (newTheme) => {
    document.documentElement.classList.toggle('dark', newTheme === 'dark')
    localStorage.setItem('theme-preference', newTheme)
    theme.value = newTheme
  }

  // 初始化：检测系统偏好 + localStorage
  onMounted(() => {
    const saved = localStorage.getItem('theme-preference')
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
    applyTheme(saved || (prefersDark ? 'dark' : 'light'))
  })

  return {
    theme,
    applyTheme,
    toggleTheme: () => applyTheme(theme.value === 'light' ? 'dark' : 'light')
  }
}
```

### 5.2 Element Plus SCSS 定制配置

```scss
// variables.scss - 覆盖 Element Plus 变量
@forward 'element-plus/theme-chalk/src/common/var.scss' with (
  $colors: (
    'primary': ('base': #3b82f6),
    'success': ('base': #10b981),
    'warning': ('base': #f59e0b),
    'danger': ('base': #ef4444),
  ),
  $font-size: ('extra-small': 12px, 'small': 14px, 'base': 14px),
  $border-radius: ('base': 8px, 'small': 4px, 'round': 16px),
  $box-shadow: ('': 0 4px 24px rgba(0,0,0,0.08)),
);
```

### 5.3 Vite 配置调整

```javascript
// vite.config.js - 添加 SCSS 预处理
export default defineConfig({
  css: {
    preprocessorOptions: {
      scss: {
        additionalData: `@use "@/styles/variables.scss" as *;`,
      }
    }
  }
})
```

---

## 六、实施步骤

| 步骤 | 任务 | 说明 |
|------|------|------|
| 1 | 创建样式目录结构 | `styles/` 下创建变量、混入、主题文件 |
| 2 | 配置 SCSS 编译 | Vite + Element Plus SCSS 定制 |
| 3 | 实现 useTheme composable | 主题切换逻辑 + 初始化检测 |
| 4 | 改造 App.vue | 添加主题 Provider、全局样式入口 |
| 5 | 改造 Navbar | 玻璃态 + 主题切换按钮 |
| 6 | 改造 Login/Register | 玻璃态卡片 + 渐变背景 |
| 7 | 改造 UserList | 表格样式 + 搜索表单 |
| 8 | 改造 UserEdit | 玻璃态表单卡片 |
| 9 | 测试验证 | 两种模式切换流畅、无样式断裂 |

---

## 七、测试验证清单

- [ ] 浅色模式：所有页面正常显示
- [ ] 深色模式：所有页面正常显示
- [ ] 主题切换：过渡动画流畅（≤300ms）
- [ ] localStorage 存储：刷新页面保持偏好
- [ ] 系统偏好检测：首次访问自动匹配
- [ ] 表格分页：样式一致性
- [ ] 弹窗/Dialog：玻璃态效果正确
- [ ] 按钮 hover：渐变发光效果正常