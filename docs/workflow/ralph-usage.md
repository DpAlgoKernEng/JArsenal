# Ralph 使用文档

> 创建日期：2026-04-15
> 版本：Ralph latest
> 作者：snarktank (基于 Geoffrey Huntley 的 Ralph pattern)

## 一、概述

**Ralph** 是一个自主 AI agent 循环，它反复运行 AI 编程工具（Amp 或 Claude Code）直到所有 PRD 项目完成。每次迭代是一个**新鲜实例**，具有干净的上下文。记忆通过 git 历史、`progress.txt` 和 `prd.json` 持久化。

### 核心定位

> *"Ralph is an autonomous AI agent loop that runs AI coding tools repeatedly until all PRD items are complete."*

Ralph 的设计哲学：**每次迭代 = 新鲜上下文**。每次迭代调用一个**新的 AI 实例**（Amp 或 Claude Code），具有干净的上下文。迭代之间唯一的记忆是：
- Git 历史（之前迭代的提交）
- `progress.txt`（学习和上下文）
- `prd.json`（哪些故事已完成）

### 核心特点

| 特性 | 说明 |
|------|------|
| **Fresh Context Per Iteration** | 每次迭代 200K 干净上下文，无累积垃圾 |
| **Autonomous Execution** | 自动循环执行，直到所有 PRD 项目完成 |
| **Memory Persistence** | Git 历史 + progress.txt + prd.json |
| **Small Tasks** | 每个 PRD 项目足够小，一次迭代完成 |
| **Quality Gates** | Typecheck + lint + tests 必须通过才能提交 |
| **AGENTS.md Updates** | 每次迭代更新 AGENTS.md，供未来迭代和开发者参考 |

### 支持的 AI 工具

| 工具 | 说明 |
|------|------|
| **Amp CLI** (默认) | ampcode.com 的 AI 编程工具 |
| **Claude Code** | Anthropic 官方 CLI (`@anthropic/ai/claude-code`) |

---

## 二、安装配置

### 2.1 前置条件

- Amp CLI 或 Claude Code 已安装并认证
- `jq` 已安装 (`brew install jq` on macOS)
- 一个 git 仓库

### 2.2 安装方式

#### 方式 1: 复制到项目

```bash
# From your project root
mkdir -p scripts/ralph
cp /path/to/ralph/ralph.sh scripts/ralph/

# Copy the prompt template for your AI tool:
cp /path/to/ralph/prompt.md scripts/ralph/prompt.md    # For Amp
# OR
cp /path/to/ralph/CLAUDE.md scripts/ralph/CLAUDE.md    # For Claude Code

chmod +x scripts/ralph/ralph.sh
```

#### 方式 2: 全局安装 Skills (Amp)

```bash
cp -r skills/prd ~/.config/amp/skills/
cp -r skills/ralph ~/.config/amp/skills/
```

For Claude Code (manual):
```bash
cp -r skills/prd ~/.claude/skills/
cp -r skills/ralph ~/.claude/skills/
```

#### 方式 3: Claude Code Marketplace

```bash
/plugin marketplace add snarktank/ralph
/plugin install ralph-skills@ralph-marketplace
```

安装后的可用 Skills：
- `/prd` - 生成 Product Requirements Documents
- `/ralph` - 将 PRDs 转换为 prd.json 格式

### 2.3 配置 Amp auto-handoff (推荐)

添加到 `~/.config/amp/settings.json`:

```json
{
  "amp.experimental.autoHandoff": { "context": 90 }
}
```

这启用上下文填充时的自动 handoff，允许 Ralph 处理超过单个上下文窗口的大型故事。

---

## 三、工作流程

### 3.1 流程图

```
┌─────────────────────────────────────────────────────────────┐
│                    RALPH LOOP                                │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────┐                                        │
│  │ 1. Read prd.json│                                        │
│  └──────────┬──────┘                                        │
│             │                                               │
│  ┌──────────▼──────┐                                        │
│  │ 2. Read         │                                        │
│  │ progress.txt    │                                        │
│  └──────────┬──────┘                                        │
│             │                                               │
│  ┌──────────▼──────┐                                        │
│  │ 3. Check branch │                                        │
│  │ (branchName)    │                                        │
│  └──────────┬──────┘                                        │
│             │                                               │
│  ┌──────────▼─────────────────────────────────────────────┐ │
│  │ 4. Pick highest priority story where passes: false     │ │
│  └──────────┬─────────────────────────────────────────────┘ │
│             │                                               │
│  ┌──────────▼──────┐                                        │
│  │ 5. Implement    │  <- Fresh 200K context                │
│  │ single story    │                                        │
│  └──────────┬──────┘                                        │
│             │                                               │
│  ┌──────────▼──────┐                                        │
│  │ 6. Run quality  │                                        │
│  │ checks          │                                        │
│  └──────────┬──────┘                                        │
│             │                                               │
│  ┌──────────▼──────┐                                        │
│  │ 7. Update       │                                        │
│  │ AGENTS.md       │                                        │
│  └──────────┬──────┘                                        │
│             │                                               │
│  ┌──────────▼──────┐                                        │
│  │ 8. Commit if    │                                        │
│  │ checks pass     │                                        │
│  └──────────┬──────┘                                        │
│             │                                               │
│  ┌──────────▼──────┐                                        │
│  │ 9. Update       │                                        │
│  │ prd.json        │                                        │
│  │ passes: true    │                                        │
│  └──────────┬──────┘                                        │
│             │                                               │
│  ┌──────────▼──────┐                                        │
│  │ 10. Append to   │                                        │
│  │ progress.txt    │                                        │
│  └──────────┬──────┘                                        │
│             │                                               │
│             ├───────────────────────────────┐               │
│             │                               │               │
│  ┌──────────▼──────┐    ┌──────────────────▼─────┐         │
│  │ All stories     │    │ More stories           │         │
│  │ passes: true?   │    │ with passes: false     │         │
│  └──────────┬──────┘    └──────────────────┬─────┘         │
│             │                               │               │
│  ┌──────────▼──────┐    ┌──────────────────▼─────┐         │
│  │ OUTPUT:         │    │ Continue loop          │         │
│  │ <promise>       │    │ (next iteration)       │         │
│  │ COMPLETE        │    └────────────────────────┘         │
│  │ </promise>      │                                        │
│  └─────────────────┘                                        │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 3.2 步骤详解

#### 步骤 1: 创建 PRD

使用 PRD skill 生成详细的需求文档：

```
Load the prd skill and create a PRD for [your feature description]
```

回答澄清问题。Skill 将输出保存到 `tasks/prd-[feature-name].md`。

---

#### 步骤 2: 转换 PRD 到 Ralph 格式

使用 Ralph skill 将 markdown PRD 转换为 JSON：

```
Load the ralph skill and convert tasks/prd-[feature-name].md to prd.json
```

这创建 `prd.json`，用户故事结构化为自主执行。

---

#### 步骤 3: 运行 Ralph

```bash
# Using Amp (default)
./scripts/ralph/ralph.sh [max_iterations]

# Using Claude Code
./scripts/ralph/ralph.sh --tool claude [max_iterations]
```

默认 10 次迭代。使用 `--tool amp` 或 `--tool claude` 选择 AI 编程工具。

Ralph 将：
1. 创建一个 feature branch（从 PRD `branchName`）
2. 挑选最高优先级的 `passes: false` 故事
3. 实现该单个故事
4. 运行质量检查（typecheck、tests）
5. 如果检查通过，提交
6. 更新 `prd.json` 标记故事为 `passes: true`
7. 将学习追加到 `progress.txt`
8. 重复直到所有故事通过或达到最大迭代次数

---

## 四、关键文件

| 文件 | 用途 |
|------|------|
| `ralph.sh` | Bash 循环，调用新鲜 AI 实例（支持 `--tool amp` 或 `--tool claude`） |
| `prompt.md` | Amp 提示模板 |
| `CLAUDE.md` | Claude Code 提示模板 |
| `prd.json` | 用户故事，带 `passes` 状态（任务列表） |
| `prd.json.example` | PRD 格式示例 |
| `progress.txt` | 追加式学习，供未来迭代 |
| `skills/prd/` | 生成 PRDs 的 Skill |
| `skills/ralph/` | 转换 PRDs 到 JSON 的 Skill |
| `.claude-plugin/` | Claude Code marketplace 发现的 Plugin manifest |
| `archive/` | 之前运行的归档 |

---

## 五、PRD JSON 格式

### 5.1 结构

```json
{
  "project": "[Project Name]",
  "branchName": "ralph/[feature-name-kebab-case]",
  "description": "[Feature description from PRD title/intro]",
  "userStories": [
    {
      "id": "US-001",
      "title": "[Story title]",
      "description": "As a [user], I want [feature] so that [benefit]",
      "acceptanceCriteria": [
        "Criterion 1",
        "Criterion 2",
        "Typecheck passes"
      ],
      "priority": 1,
      "passes": false,
      "notes": ""
    }
  ]
}
```

### 5.2 示例 PRD

```json
{
  "project": "MyApp",
  "branchName": "ralph/task-priority",
  "description": "Task Priority System - Add priority levels to tasks",
  "userStories": [
    {
      "id": "US-001",
      "title": "Add priority field to database",
      "description": "As a developer, I need to store task priority so it persists across sessions.",
      "acceptanceCriteria": [
        "Add priority column to tasks table: 'high' | 'medium' | 'low' (default 'medium')",
        "Generate and run migration successfully",
        "Typecheck passes"
      ],
      "priority": 1,
      "passes": false,
      "notes": ""
    },
    {
      "id": "US-002",
      "title": "Display priority indicator on task cards",
      "description": "As a user, I want to see task priority at a glance.",
      "acceptanceCriteria": [
        "Each task card shows colored priority badge (red=high, yellow=medium, gray=low)",
        "Priority visible without hovering or clicking",
        "Typecheck passes",
        "Verify in browser using dev-browser skill"
      ],
      "priority": 2,
      "passes": false,
      "notes": ""
    },
    {
      "id": "US-003",
      "title": "Add priority selector to task edit",
      "description": "As a user, I want to change a task's priority when editing it.",
      "acceptanceCriteria": [
        "Priority dropdown in task edit modal",
        "Shows current priority as selected",
        "Saves immediately on selection change",
        "Typecheck passes",
        "Verify in browser using dev-browser skill"
      ],
      "priority": 3,
      "passes": false,
      "notes": ""
    },
    {
      "id": "US-004",
      "title": "Filter tasks by priority",
      "description": "As a user, I want to filter the task list to see only high-priority items.",
      "acceptanceCriteria": [
        "Filter dropdown with options: All | High | Medium | Low",
        "Filter persists in URL params",
        "Empty state message when no tasks match filter",
        "Typecheck passes",
        "Verify in browser using dev-browser skill"
      ],
      "priority": 4,
      "passes": false,
      "notes": ""
    }
  ]
}
```

---

## 六、关键概念

### 6.1 小任务原则

**每个故事必须能在一次 Ralph 迭代（一个上下文窗口）内完成。**

Ralph 每次迭代调用一个新鲜的 Amp 实例，无之前工作的记忆。如果任务太大，LLM 在完成前耗尽上下文并产生损坏的代码。

**正确大小的故事：**
- 添加一个数据库列和迁移
- 添加一个 UI 组件到现有页面
- 更新一个 server action 的逻辑
- 添加一个过滤下拉框到列表

**太大（需拆分）：**
- "构建整个 dashboard" → 拆分为：schema、queries、UI components、filters
- "添加认证" → 拆分为：schema、middleware、login UI、session handling
- "重构 API" → 拆分为每个 endpoint 或模式一个故事

**经验法则：** 如果无法在 2-3 句话内描述变更，它就太大。

---

### 6.2 故事排序：依赖优先

故事按优先级顺序执行。早期故事不能依赖后期故事。

**正确顺序：**
1. Schema/database changes（migrations）
2. Server actions / backend logic
3. 使用后端的 UI components
4. Dashboard/summary views（聚合数据）

**错误顺序：**
1. UI component（依赖尚不存在的 schema）
2. Schema change

---

### 6.3 Acceptance Criteria：必须可验证

每个标准必须是 Ralph 能检查的，不能模糊。

**好的标准（可验证）：**
- "Add `status` column to tasks table with default 'pending'"
- "Filter dropdown has options: All, Active, Completed"
- "Clicking delete shows confirmation dialog"
- "Typecheck passes"
- "Tests pass"

**坏的标准（模糊）：**
- "Works correctly"
- "User can do X easily"
- "Good UX"
- "Handles edge cases"

**始终作为最后标准：**
```
"Typecheck passes"
```

对于有可测试逻辑的故事，也添加：
```
"Tests pass"
```

对于改变 UI 的故事，也添加：
```
"Verify in browser using dev-browser skill"
```

---

### 6.4 AGENTS.md 更新至关重要

每次迭代后，Ralph 更新相关的 `AGENTS.md` 文件。这很关键，因为 AI 编程工具自动读取这些文件，所以未来迭代（和未来人类开发者）从发现的模式、陷阱和约定中受益。

**添加到 AGENTS.md 的示例：**
- 发现的模式（"此代码库用 X 做 Y"）
- 陷阱（"改变 W 时不要忘记更新 Z"）
- 有用的上下文（"设置面板在组件 X 中"）

---

### 6.5 反馈循环

Ralph 只有在有反馈循环时才能工作：
- Typecheck 捕获类型错误
- Tests 验证行为
- CI 必须保持绿色（损坏的代码会在迭代间累积）

---

### 6.6 UI 故事的浏览器验证

前端故事必须在 acceptance criteria 中包含 "Verify in browser using dev-browser skill"。Ralph 将使用 dev-browser skill 导航到页面、交互 UI，并确认变更工作。

---

### 6.7 停止条件

当所有故事有 `passes: true`，Ralph 输出 `<promise>COMPLETE</promise>` 并循环退出。

---

## 七、Progress Report 格式

追加到 progress.txt（永不替换，始终追加）：

```
## [Date/Time] - [Story ID]
Thread: https://ampcode.com/threads/$AMP_CURRENT_THREAD_ID
- What was implemented
- Files changed
- **Learnings for future iterations:**
  - Patterns discovered (e.g., "this codebase uses X for Y")
  - Gotchas encountered (e.g., "don't forget to update Z when changing W")
  - Useful context (e.g., "the evaluation panel is in component X")
---
```

学习部分至关重要 —— 它帮助未来迭代避免重复错误并更好地理解代码库。

---

## 八、Codebase Patterns 整合

如果发现**可复用模式**，添加到 progress.txt 的顶部 `## Codebase Patterns` 部分（如果不存在则创建）：

```
## Codebase Patterns
- Example: Use `sql<number>` template for aggregations
- Example: Always use `IF NOT EXISTS` for migrations
- Example: Export types from actions.ts for UI components
```

只添加**通用可复用的模式**，不是故事特定的细节。

---

## 九、调试

检查当前状态：

```bash
# 看哪些故事已完成
cat prd.json | jq '.userStories[] | {id, title, passes}'

# 看之前迭代的学习
cat progress.txt

# 检查 git 历史
git log --oneline -10
```

---

## 十、归档

Ralph 自动归档之前的运行，当你开始一个新功能（不同的 `branchName`）。归档保存到 `archive/YYYY-MM-DD-feature-name/`。

---

## 十一、自定义提示

复制 `prompt.md`（Amp）或 `CLAUDE.md`（Claude Code）到项目后，自定义它：
- 添加项目特定的质量检查命令
- 包含代码库约定
- 添加你栈的常见陷阱

---

## 十二、Skills 说明

### 12.1 `/prd` Skill

**用途**：生成 Product Requirements Documents

**触发词**：
- "create a prd"
- "write prd for"
- "plan this feature"
- "requirements for"
- "spec out"

**流程**：
1. 接收功能描述
2. 问 3-5 个澄清问题（带字母选项）
3. 基于答案生成结构化 PRD
4. 保存到 `tasks/prd-[feature-name].md`

**PRD 结构**：
- Introduction/Overview
- Goals
- User Stories
- Functional Requirements
- Non-Goals (Out of Scope)
- Design Considerations (可选)
- Technical Considerations (可选)
- Success Metrics
- Open Questions

---

### 12.2 `/ralph` Skill

**用途**：将 PRDs 转换为 prd.json 格式

**触发词**：
- "convert this prd"
- "turn this into ralph format"
- "create prd.json from this"
- "ralph json"

**转换规则**：
1. 每个用户故事成为一个 JSON entry
2. IDs: 顺序（US-001, US-002, etc.）
3. Priority: 基于依赖顺序
4. 所有故事: `passes: false` 和空 `notes`
5. branchName: 从功能名派生，kebab-case，前缀 `ralph/`
6. 始终添加: "Typecheck passes" 到每个故事的 acceptance criteria

---

## 十三、参考资源

| 资源 | 链接 |
|------|------|
| GitHub 仓库 | https://github.com/snarktank/ralph |
| Geoffrey Huntley 的 Ralph 文章 | https://ghuntley.com/ralph/ |
| Amp 文档 | https://ampcode.com/manual |
| Claude Code 文档 | https://docs.anthropic.com/en/docs/claude-code |
| 交互式流程图 | https://snarktank.github.io/ralph/ |

---

## 十四、总结

Ralph 是一个简单但强大的自主 AI agent 循环系统：

| 特性 | 价值 |
|------|------|
| **Fresh Context Per Iteration** | 200K 干净上下文，无累积垃圾 |
| **Autonomous Execution** | 自动循环直到所有 PRD 项目完成 |
| **Memory Persistence** | Git 历史 + progress.txt + prd.json |
| **Small Tasks** | 每个故事一次迭代完成 |
| **Quality Gates** | Typecheck + lint + tests 必须通过 |
| **AGENTS.md Updates** | 学习持久化，供未来使用 |

**与其他工具的关系**：

Ralph 是一个**自主执行循环**，与目录中其他工具形成互补：
- **GSD** — 完整的上下文工程 + 规范驱动开发系统
- **GStack** — YC CEO 的效率工作流，适合快速迭代
- **OpenSpec** — 轻量规格框架，流体工作流
- **Ralph** — 简单的 PRD → 自动执行循环

---

*文档由 Claude Code 根据 GitHub 仓库内容生成*