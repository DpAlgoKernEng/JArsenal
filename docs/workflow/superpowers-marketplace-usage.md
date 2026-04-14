# Superpowers Marketplace 使用文档

> 创建日期：2026-04-14
> 版本：superpowers 5.0.7

## 一、概述

**Superpowers Marketplace** 是由 Jesse Vincent (obra) 开发的 Claude Code 插件市场，提供一系列经过实战检验的技能（Skills）、工作流和生产力工具，帮助 AI 编程助手更高效、更规范地完成软件开发任务。

### 核心价值

- **自动化工作流**：从设计到实现的全流程自动化
- **测试驱动开发**：强制执行 RED-GREEN-REFACTOR 循环
- **系统化调试**：4阶段根因分析流程
- **协作规范化**：设计文档、实施计划、代码审查的完整流程

---

## 二、安装配置

### 2.1 添加 Marketplace

在 Claude Code 中注册 marketplace：

```bash
/plugin marketplace add obra/superpowers-marketplace
```

### 2.2 安装 Superpowers 核心插件

从 marketplace 安装：

```bash
/plugin install superpowers@superpowers-marketplace
```

或从官方 marketplace 安装：

```bash
/plugin install superpowers@claude-plugins-official
```

### 2.3 配置文件位置

安装完成后，相关文件位于：

| 路径 | 说明 |
|------|------|
| `~/.claude/plugins/marketplaces/superpowers-marketplace/` | Marketplace 源码 |
| `~/.claude/plugins/cache/claude-plugins-official/superpowers/5.0.7/` | 安装的插件缓存 |

### 2.4 启用/禁用插件

在项目的 `.claude/settings.local.json` 中配置：

```json
{
  "enabledPlugins": {
    "superpowers@claude-plugins-official": false
  }
}
```

---

## 三、核心工作流

### 3.1 基础工作流程图

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  brainstorming  │ ──► │  writing-plans  │ ──► │ subagent-driven │
│   (设计阶段)     │     │   (计划阶段)     │     │  -development   │
└─────────────────┘     └─────────────────┘     └─────────────────┘
                                                        │
                                                        ▼
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│ finishing-a-    │ ◄── │ requesting-code │ ◄── │ test-driven-    │
│ development-    │     │    -review      │     │  development    │
│ branch          │     │                 │     │                 │
└─────────────────┘     └─────────────────┘     └─────────────────┘
```

### 3.2 各阶段详解

#### 阶段 1：Brainstorming（头脑风暴）

**触发条件**：任何创建性工作之前 - 创建功能、构建组件、添加新行为

**核心原则**：
- 不写代码，先理解需求
- 通过提问逐步细化想法
- 分段展示设计，获取用户批准

**流程**：
1. 探索项目上下文（检查文件、文档、最近提交）
2. 逐个提问澄清需求
3. 提出 2-3 种方案，给出推荐
4. 分段展示设计，逐段获取批准
5. 编写设计文档到 `docs/superpowers/specs/YYYY-MM-DD-<topic>-design.md`
6. 用户审查规格文档
7. 调用 `writing-plans` skill 进入实现计划

**硬性规则**：
```
在没有展示设计并获得用户批准之前，不得：
- 调用任何实现 skill
- 编写任何代码
- 创建任何项目结构
- 执行任何实现操作
```

#### 阶段 2：Writing Plans（编写计划）

**触发条件**：有规格或需求，需要多步骤任务，在编写代码之前

**核心原则**：
- 假设工程师零上下文、品味一般
- 每个任务 2-5 分钟可完成
- DRY、YAGNI、TDD 原则
- 频繁提交

**计划文档位置**：`docs/superpowers/plans/YYYY-MM-DD-<feature-name>.md`

**任务粒度示例**：
```
- "编写失败的测试" - 一个步骤
- "运行测试确认失败" - 一个步骤
- "实现最小代码使测试通过" - 一个步骤
- "运行测试确认通过" - 一个步骤
- "提交" - 一个步骤
```

#### 阶段 3：Subagent-Driven Development（子代理驱动开发）

**触发条件**：有实施计划，任务大多独立，保持在当前会话

**核心原理**：
- 每个任务派遣全新的子代理（隔离上下文）
- 两阶段审查：先检查规格合规，再检查代码质量
- 快速迭代（任务间无需人工介入）

**流程**：
```
for 每个任务:
    派遣实现子代理
    子代理有问题？ → 回答问题，提供上下文
    任务完成？ → 规格合规审查 → 代码质量审查
    有问题？ → 派遣修复子代理
    通过？ → 进入下一个任务
```

#### 阶段 4：Test-Driven Development（测试驱动开发）

**触发条件**：实现任何功能或修复 bug，在编写实现代码之前

**铁律**：
```
没有失败的测试，就没有生产代码
```

**RED-GREEN-REFACTOR 循环**：
```
1. RED:   编写失败的测试 → 运行确认失败
2. GREEN: 编写最小代码使测试通过 → 运行确认通过
3. REFACTOR: 重构代码（可选） → 确认测试仍通过
4. COMMIT: 提交代码
```

**如果先写了代码**：
- 删除它，从头开始
- 不要保留作为"参考"
- 不要"改编"它
- 删除就是删除

#### 阶段 5：Requesting Code Review（请求代码审查）

**触发条件**：任务之间，需要审查已完成的工作

**审查内容**：
- 规格合规性检查
- 代码质量评估
- 架构和设计审查
- 文档和标准检查

#### 阶段 6：Finishing a Development Branch（完成开发分支）

**触发条件**：所有任务完成

**选项**：
- 合并到主分支
- 创建 Pull Request
- 保持分支
- 丢弃分支

---

## 四、可用 Skills 列表

### 4.1 测试类

| Skill | 说明 |
|-------|------|
| `test-driven-development` | RED-GREEN-REFACTOR 循环，包含测试反模式参考 |

### 4.2 调试类

| Skill | 说明 |
|-------|------|
| `systematic-debugging` | 4阶段根因分析流程，包含根因追踪、防御深度、条件等待技术 |
| `verification-before-completion` | 确保问题真正修复 |

### 4.3 协作类

| Skill | 说明 |
|-------|------|
| `brainstorming` | 苏格拉底式设计细化 |
| `writing-plans` | 详细实施计划编写 |
| `executing-plans` | 批量执行带检查点 |
| `dispatching-parallel-agents` | 并发子代理工作流 |
| `requesting-code-review` | 预审查清单 |
| `receiving-code-review` | 响应反馈 |
| `using-git-worktrees` | 并行开发分支 |
| `finishing-a-development-branch` | 合并/PR 决策工作流 |
| `subagent-driven-development` | 快速迭代 + 两阶段审查 |

### 4.4 元类

| Skill | 说明 |
|-------|------|
| `writing-skills` | 按最佳实践创建新 skills（包含测试方法） |
| `using-superpowers` | Skills 系统入门介绍 |

---

## 五、可用 Agents

### 5.1 code-reviewer

**用途**：项目主要步骤完成后，需要对照原始计划和编码标准进行审查

**职责**：
1. 计划对齐分析 - 比较实现与原始规划
2. 代码质量评估 - 模式、错误处理、类型安全
3. 架构和设计审查 - SOLID 原则、关注点分离
4. 文档和标准检查 - 注释、文档、命名规范
5. 问题识别和建议 - Critical/Important/Suggestions 分级

**调用方式**：
```python
Agent(subagent_type="superpowers:code-reviewer", prompt="审查第3步的用户认证实现")
```

---

## 六、斜杠命令

Superpowers 提供以下快捷命令：

| 命令 | 说明 |
|------|------|
| `/brainstorm` | 启动头脑风暴 skill |
| `/write-plan` | 启动编写计划 skill |
| `/execute-plan` | 启动执行计划 skill |

---

## 七、Hooks 配置

### 7.1 SessionStart Hook

每次会话启动时自动注入 `using-superpowers` skill 内容，确保 AI 知道如何使用 skills 系统。

**配置文件**：`hooks/hooks.json`

```json
{
  "hooks": {
    "SessionStart": [
      {
        "matcher": "startup|clear|compact",
        "hooks": [
          {
            "type": "command",
            "command": "\"${CLAUDE_PLUGIN_ROOT}/hooks/run-hook.cmd\" session-start",
            "async": false
          }
        ]
      }
    ]
  }
}
```

---

## 八、其他可用插件

Superpowers Marketplace 还提供以下插件：

| 插件名 | 说明 | 版本 |
|--------|------|------|
| `superpowers-chrome` | Chrome DevTools Protocol 直接访问（beta） | 1.8.0 |
| `elements-of-style` | 基于 Strunk 写作指南的写作指导 | 1.0.0 |
| `episodic-memory` | 会话语义搜索，跨会话记忆 | 1.0.15 |
| `superpowers-lab` | 实验性 skills：tmux 自动化、MCP 发现、重复函数检测 | 0.4.0 |
| `superpowers-developing-for-claude-code` | Claude Code 插件开发资源 | 0.3.1 |
| `claude-session-driver` | 通过 tmux 启动控制其他 Claude 会话 | 1.0.1 |
| `double-shot-latte` | 自动判断是否继续工作，减少打断 | 1.2.0 |

**安装示例**：
```bash
/plugin install episodic-memory@superpowers-marketplace
/plugin install elements-of-style@superpowers-marketplace
```

---

## 九、设计哲学

### 9.1 核心原则

| 原则 | 说明 |
|------|------|
| **Test-Driven Development** | 先写测试，永远 |
| **Systematic over ad-hoc** | 流程胜过猜测 |
| **Complexity reduction** | 简单性是首要目标 |
| **Evidence over claims** | 验证后再宣布成功 |

### 9.2 YAGNI 原则

You Aren't Gonna Need It - 不要为假设的未来需求设计

### 9.3 DRY 原则

Don't Repeat Yourself - 每个知识点单一明确表达

---

## 十、文件结构

### 10.1 Superpowers 插件目录结构

```
superpowers/5.0.7/
├── skills/                     # Skills 目录
│   ├── brainstorming/
│   │   └── SKILL.md            # Skill 定义文件
│   ├── subagent-driven-development/
│   ├── test-driven-development/
│   ├── writing-plans/
│   └── ...
├── agents/                     # Agents 目录
│   └── code-reviewer.md        # Agent 定义文件
├── commands/                   # 斜杠命令
│   ├── brainstorm.md
│   ├── execute-plan.md
│   └── write-plan.md
├── hooks/                      # Hooks 配置
│   ├── hooks.json
│   └── session-start           # SessionStart 脚本
├── CLAUDE.md                   # 插件说明
└── README.md                   # README
```

### 10.2 项目中使用 Superpowers 的文件结构

```
your-project/
├── docs/
│   └── superpowers/
│       ├── specs/              # 设计文档
│       │   └── YYYY-MM-DD-xxx-design.md
│       └── plans/              # 实施计划
│           └── YYYY-MM-DD-xxx-implementation.md
└── .claude/
    └── settings.local.json     # 插件启用配置
```

---

## 十一、实战示例

### 11.1 创建新功能完整流程

```bash
# 1. 启动会话，描述需求
"我想添加一个用户导出功能"

# 2. AI 自动触发 brainstorming skill
# - 探索项目结构
# - 提问澄清需求（导出格式？导出范围？权限？）
# - 提出方案选择
# - 分段展示设计

# 3. 用户批准设计后，AI 自动触发 writing-plans skill
# - 编写详细实施计划到 docs/superpowers/plans/

# 4. 用户说 "go"，AI 触发 subagent-driven-development
# - 派遣子代理逐个任务执行
# - 两阶段审查每个任务

# 5. 所有任务完成，触发 finishing-a-development-branch
# - 选择合并/PR/保持分支
```

### 11.2 使用斜杠命令快速启动

```bash
# 直接启动头脑风暴
/brainstorm

# 有设计文档后直接写计划
/write-plan

# 有计划后直接执行
/execute-plan
```

### 11.3 调用 code-reviewer Agent

当完成一个重要步骤后：

```python
# AI 会自动调用
Agent(
    subagent_type="superpowers:code-reviewer",
    prompt="审查 docs/superpowers/plans/xxx-implementation.md 中第 3 步的实现"
)
```

---

## 十二、常见问题

### Q1: 如何查看已安装的 skills？

Skills 会在 SessionStart 时自动加载。AI 可以通过 Skill tool 调用：

```python
Skill(skill="superpowers:brainstorming")
```

### Q2: 如何禁用某个 skill？

在项目的 `.claude/settings.local.json` 中：

```json
{
  "enabledPlugins": {
    "superpowers@claude-plugins-official": false
  }
}
```

### Q3: 设计文档和计划文档的区别？

| 文档类型 | 位置 | 内容 |
|----------|------|------|
| 设计文档 | `docs/superpowers/specs/` | 需求、方案选择、架构设计 |
| 计划文档 | `docs/superpowers/plans/` | 具体实施步骤、代码片段、测试方法 |

### Q4: 如何更新插件？

```bash
/plugin update superpowers
```

---

## 十三、参考链接

| 资源 | 链接 |
|------|------|
| Marketplace 源码 | https://github.com/obra/superpowers-marketplace |
| Superpowers 核心 | https://github.com/obra/superpowers |
| 作者博客 | https://blog.fsck.com |
| Discord 社区 | https://discord.gg/35wsABTejz |
| 问题反馈 | https://github.com/obra/superpowers/issues |
| 版本公告订阅 | https://primeradiant.com/superpowers/ |

---

## 十四、License

MIT License

---

*文档由 Claude Code 根据实际安装的插件内容生成*