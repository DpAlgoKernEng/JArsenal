# 组件库规范

本文档描述 RBAC 系统中通用组件的设计规范。

---

## 按钮 (Button)

### 按钮类型

| 类型 | 样式 | 使用场景 |
|------|-----|---------|
| **Primary** | 深蓝背景 + 白字 | 主要操作：保存、创建、确认 |
| **Secondary** | 浅灰背景 + 深灰字 + 边框 | 次要操作：取消、关闭 |
| **Danger** | 红色背景 + 白字 | 危险操作：删除、禁用 |
| **Ghost** | 无背景 + 主题色字 | 辅助操作：表格内操作、链接 |
| **Link** | 无背景 + 蓝色字 + 下划线 | 文字链接 |

### 按钮尺寸

| 尺寸 | 高度 | 字号 | Padding |
|------|-----|-----|---------|
| **Small** | 32px | 12px | 8px 12px |
| **Medium** | 40px | 14px | 10px 16px |
| **Large** | 48px | 16px | 12px 24px |

### 按钮状态

| 状态 | 样式 |
|------|-----|
| **Hover** | 背景色加深 10% |
| **Active** | scale(0.98) 微缩放 |
| **Focus** | 2px 外环阴影 |
| **Disabled** | opacity: 0.5 + cursor: not-allowed |
| **Loading** | spinner + 禁用点击 |

### CSS 规范

```css
.button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--duration-fast);
}

.button:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}

.button.primary {
  background: var(--color-primary);
  color: var(--color-on-primary);
}

.button.primary:hover {
  background: color-mix(in srgb, var(--color-primary) 90%, black);
}

.button.danger {
  background: var(--color-destructive);
  color: var(--color-on-primary);
}

.button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.button.loading {
  pointer-events: none;
}

.button-icon {
  width: 20px;
  height: 20px;
}
```

---

## 输入框 (Input)

### 输入框尺寸

| 尺寸 | 高度 | 字号 |
|------|-----|-----|
| **Small** | 32px | 12px |
| **Medium** | 44px | 14px |
| **Large** | 52px | 16px |

### 输入框状态

| 状态 | 样式 |
|------|-----|
| **Default** | 灰色边框 |
| **Hover** | 边框加深 |
| **Focus** | 主题色边框 + 外环阴影 |
| **Error** | 红色边框 + 红色错误提示 |
| **Disabled** | 灰色背景 + 禁止输入 |

### 输入框组件

```css
.input-wrapper {
  display: flex;
  align-items: center;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-background);
}

.input-wrapper:focus-within {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 2px rgba(var(--color-primary-rgb), 0.1);
}

.input-wrapper.error {
  border-color: var(--color-destructive);
}

.input-wrapper.disabled {
  background: var(--color-muted);
  pointer-events: none;
}

.input {
  flex: 1;
  height: 100%;
  padding: 0 12px;
  border: none;
  background: transparent;
  font-size: var(--font-size-base);
}

.input:focus {
  outline: none;
}

.input-icon {
  width: 20px;
  height: 20px;
  margin: 0 12px;
  color: var(--color-muted-foreground);
}
```

---

## 下拉选择 (Select)

### 下拉组件结构

```
┌────────────────────────────────────┐
│ 请选择                    ▼       │  ← 选择器
└────────────────────────────────────┘

点击后展开:
┌────────────────────────────────────┐
│ 选项 1                             │
│ 选项 2                             │
│ 选项 3                             │
└────────────────────────────────────┘
```

### CSS 规范

```css
.select-wrapper {
  position: relative;
}

.select-trigger {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  padding: 0 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  cursor: pointer;
}

.select-dropdown {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  width: 100%;
  background: var(--color-surface);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-3);
  z-index: 100;
}

.select-option {
  padding: 10px 12px;
  cursor: pointer;
}

.select-option:hover {
  background: var(--color-muted);
}

.select-option.selected {
  background: var(--color-primary-bg);
  color: var(--color-primary);
}
```

---

## 表格 (Table)

### 表格结构

```
┌────────────────────────────────────────────────────────────────┐
│ 列标题1 │ 列标题2 │ 列标题3 │ 列标题4 │ 操作 │                │
├────────────────────────────────────────────────────────────────┤
│ 数据1  │ 数据2   │ 数据3   │ 数据4   │  ⋮  │                │
│ 数据1  │ 数据2   │ 数据3   │ 数据4   │  ⋮  │                │
└────────────────────────────────────────────────────────────────┘
```

### 表格规范

| 属性 | 规格 |
|------|-----|
| **表头背景** | `--color-muted` |
| **表头字号** | 14px, 字重 500 |
| **行高** | 56px |
| **单元格 Padding** | 12px 16px |
| **悬停行背景** | `--color-muted` (浅色) |
| **选中行背景** | `--color-info-bg` + 左侧蓝色边框 3px |

### CSS 规范

```css
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
  border-left: 3px solid var(--color-info);
}
```

---

## 分页 (Pagination)

### 分页结构

```
< 1 2 3 ... 50 >                     每页显示: [10▼] 条
```

### CSS 规范

```css
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
}
```

---

## 标签 (Tag)

### 标签类型

| 类型 | 背景 | 文字颜色 |
|------|-----|---------|
| **Default** | 灰色 | 深灰 |
| **Primary** | 蓝色浅底 | 蓝色 |
| **Success** | 绿色浅底 | 绿色 |
| **Warning** | 黄色浅底 | 黄色 |
| **Danger** | 红色浅底 | 红色 |
| **Info** | 蓝色浅底 | 蓝色 |

### CSS 规范

```css
.tag {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  font-size: var(--font-size-xs);
  border-radius: var(--radius-sm);
}

.tag.default {
  background: var(--color-muted);
  color: var(--color-muted-foreground);
}

.tag.primary {
  background: var(--color-primary-bg);
  color: var(--color-primary);
}

.tag.success {
  background: var(--color-accent-bg);
  color: var(--color-accent);
}

.tag.danger {
  background: var(--color-destructive-bg);
  color: var(--color-destructive);
}
```

---

## 弹窗 (Dialog)

### 弹窗尺寸

| 尺寸 | 宽度 |
|------|-----|
| **Small** | 400px |
| **Medium** | 500px |
| **Large** | 600px |
| **XLarge** | 800px |

### 弹窗结构

```
┌────────────────────────────────────┐
│ 标题                        [×]   │  ← Header
├────────────────────────────────────┤
│                                    │  ← Body
│         弹窗内容                   │
│                                    │
├────────────────────────────────────┤
│                    [取消] [确认]  │  ← Footer
└────────────────────────────────────┘
```

### CSS 规范

```css
.dialog-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 1000;
}

.dialog-content {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-4);
  max-width: 90vw;
  max-height: 90vh;
}

.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  border-bottom: 1px solid var(--color-border);
}

.dialog-body {
  padding: 24px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid var(--color-border);
}
```

---

## Toast 提示

### Toast 类型

| 类型 | 图标 | 颜色 |
|------|-----|-----|
| **Success** | ✓ | 绿色 |
| **Error** | ✗ | 红色 |
| **Warning** | ⚠ | 黄色 |
| **Info** | ⓘ | 蓝色 |

### Toast 位置

- 默认: 右上角
- 可配置: 顶部、底部、左上、右上

### CSS 规范

```css
.toast-container {
  position: fixed;
  top: 24px;
  right: 24px;
  z-index: 2000;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.toast {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: var(--color-surface);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-3);
  min-width: 200px;
}

.toast.success {
  border-left: 4px solid var(--color-accent);
}

.toast.error {
  border-left: 4px solid var(--color-destructive);
}

.toast-icon {
  width: 20px;
  height: 20px;
}

.toast-message {
  flex: 1;
  font-size: var(--font-size-base);
}

.toast-close {
  cursor: pointer;
}

/* 动画 */
.toast-enter {
  animation: toast-enter 300ms var(--ease-out);
}

.toast-exit {
  animation: toast-exit 200ms var(--ease-in);
}
```

---

## 骨架屏 (Skeleton)

### 使用场景

数据加载时显示骨架屏，避免空白闪烁。

### CSS 规范

```css
.skeleton {
  background: linear-gradient(
    90deg,
    var(--color-muted) 25%,
    var(--color-surface) 50%,
    var(--color-muted) 75%
  );
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s infinite;
  border-radius: var(--radius-md);
}

@keyframes skeleton-loading {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.skeleton-text {
  height: 16px;
  margin-bottom: 8px;
}

.skeleton-avatar {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-full);
}

.skeleton-button {
  width: 80px;
  height: 40px;
}
```

---

## 头像 (Avatar)

### 头像尺寸

| 尺寸 | 直径 |
|------|-----|
| **Small** | 32px |
| **Medium** | 40px |
| **Large** | 64px |
| **XLarge** | 80px |

### CSS 规范

```css
.avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-full);
  background: var(--color-muted);
  overflow: hidden;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-fallback {
  font-size: calc(size / 2);
  color: var(--color-muted-foreground);
}
```

---

## 空状态 (Empty State)

### 使用场景

列表无数据时显示空状态。

### 空状态布局

```
┌────────────────────────────────────────────────────┐
│                                                    │
│              [空状态图标]                          │
│                                                    │
│           暂无用户数据                             │
│                                                    │
│         [新建用户] 或 [导入用户]                   │
│                                                    │
└────────────────────────────────────────────────────┘
```

### CSS 规范

```css
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