# GSD (Get Shit Done) 使用文档

> 创建日期：2026-04-15
> 版本：GSD v1.36.0+
> 作者：TÂCHES (GSD Foundation)

## 一、概述

**GSD (Get Shit Done)** 是一个轻量级但强大的元提示框架、上下文工程和规范驱动开发系统。它解决了 AI 编程中的核心问题：**Context Rot（上下文腐烂）** —— 随着 Claude 填充上下文窗口而发生的质量降解。

### 核心定位

> *"If you know clearly what you want, this WILL build it for you. No bs."*

> *"By far the most powerful addition to my Claude Code. Nothing over-engineered. Literally just gets shit done."*

GSD 的设计哲学：**不玩企业角色扮演游戏**。复杂性在系统中，不在你的工作流里。幕后：上下文工程、XML 提示格式化、子代理编排、状态管理。你看到的：几个命令，它们就是能工作。

### 核心特点

| 特性 | 说明 |
|------|------|
| **Context Engineering** | 结构化 artifact 给 AI 每个任务所需的所有上下文 |
| **Fresh Context Per Agent** | 每个代理获得干净的 200K token 上下文窗口，消除上下文腐烂 |
| **Multi-Agent Orchestration** | 薄编排器调用专业化代理，收集结果，路由到下一步 |
| **Spec-Driven Development** | 需求 → 研究 → 计划 → 执行 → 验证管道 |
| **State Management** | 持久化项目记忆，跨会话和上下文重置保持状态 |
| **Atomic Git Commits** | 每个任务独立提交，可追溯、可回滚 |

### 支持的运行时（15+）

| 运行时 | 安装方式 | 命令前缀 |
|--------|----------|----------|
| Claude Code | Skills (`skills/gsd-*/SKILL.md`) | `/gsd-*` |
| OpenCode | Skills | `/gsd-*` |
| Gemini CLI | Skills | `/gsd-*` |
| Kilo | Skills | `/gsd-*` |
| Codex | Skills | `$gsd-*` |
| Copilot | Commands | `/gsd-*` |
| Cursor | Skills | `/gsd-*` |
| Windsurf | Skills | `/gsd-*` |
| Antigravity | Skills | `/gsd-*` |
| Augment | Skills | `/gsd-*` |
| Trae | Skills | `/gsd-*` |
| Qwen Code | Skills | `/gsd-*` |
| CodeBuddy | Skills | `/gsd-*` |
| Cline | `.clinerules` | N/A |

---

## 二、安装配置

### 2.1 一键安装

```bash
npx get-shit-done-cc@latest
```

安装器会提示选择：
1. **运行时** — Claude Code、OpenCode、Gemini、Kilo、Codex、Copilot、Cursor、Windsurf、Antigravity、Augment、Trae、Qwen Code、CodeBuddy、Cline 或全部
2. **位置** — 全局（所有项目）或本地（当前项目）

### 2.2 验证安装

- Claude Code / Gemini / Copilot: `/gsd-help`
- OpenCode / Kilo / Augment / Trae: `/gsd-help`
- Codex: `$gsd-help`
- Cline: 检查 `.clinerules` 是否存在

### 2.3 非交互式安装（Docker、CI、脚本）

```bash
# Claude Code
npx get-shit-done-cc --claude --global   # 安装到 ~/.claude/
npx get-shit-done-cc --claude --local    # 安装到 ./.claude/

# OpenCode
npx get-shit-done-cc --opencode --global

# Gemini CLI
npx get-shit-done-cc --gemini --global

# Kilo
npx get-shit-done-cc --kilo --global

# Codex
npx get-shit-done-cc --codex --global

# Copilot
npx get-shit-done-cc --copilot --global

# Cursor
npx get-shit-done-cc --cursor --global

# Windsurf
npx get-shit-done-cc --windsurf --global

# Cline
npx get-shit-done-cc --cline --global

# 所有运行时
npx get-shit-done-cc --all --global
```

### 2.4 推荐配置：跳过权限模式

```bash
claude --dangerously-skip-permissions
```

这是 GSD 的预期使用方式 —— 停下来批准 `date` 和 `git commit` 50 次会违背初衷。

### 2.5 保持更新

```bash
npx get-shit-done-cc@latest
```

---

## 三、核心工作流程

### 3.1 完整项目生命周期

```
┌──────────────────────────────────────────────────┐
│                   NEW PROJECT                    │
│  /gsd-new-project                                │
│  Questions -> Research -> Requirements -> Roadmap│
└─────────────────────────┬────────────────────────┘
                          │
           ┌──────────────▼─────────────┐
           │      FOR EACH PHASE:       │
           │                            │
           │  ┌────────────────────┐    │
           │  │ /gsd-discuss-phase │    │  <- 锁定偏好
           │  └──────────┬─────────┘    │
           │             │              │
           │  ┌──────────▼─────────┐    │
           │  │ /gsd-ui-phase      │    │  <- 设计契约（前端）
           │  └──────────┬─────────┘    │
           │             │              │
           │  ┌──────────▼─────────┐    │
           │  │ /gsd-plan-phase    │    │  <- 研究 + 计划 + 验证
           │  └──────────┬─────────┘    │
           │             │              │
           │  ┌──────────▼─────────┐    │
           │  │ /gsd-execute-phase │    │  <- 并行执行
           │  └──────────┬─────────┘    │
           │             │              │
           │  ┌──────────▼─────────┐    │
           │  │ /gsd-verify-work   │    │  <- 手动 UAT
           │  └──────────┬─────────┘    │
           │             │              │
           │  ┌──────────▼─────────┐    │
           │  │ /gsd-ship          │    │  <- 创建 PR（可选）
           │  └──────────┬─────────┘    │
           │             │              │
           │     Next Phase?────────────┘
           │             │ No
           └─────────────┼──────────────┘
                          │
          ┌───────────────▼──────────────┐
          │  /gsd-audit-milestone        │
          │  /gsd-complete-milestone     │
          └───────────────┬──────────────┘
                          │
                 Another milestone?
                     │          │
                    Yes         No -> Done!
                     │
             ┌───────▼──────────────┐
             │  /gsd-new-milestone  │
             └──────────────────────┘
```

### 3.2 步骤详解

#### 步骤 1: 初始化项目

```
/gsd-new-project
```

一命令，一流程。系统执行：

1. **Questions** — 询问直到完全理解你的想法（目标、约束、技术偏好、边缘情况）
2. **Research** — 调用并行代理调查领域（可选但推荐）
3. **Requirements** — 提取 v1、v2 和范围外的内容
4. **Roadmap** — 创建映射到需求的阶段

你批准 roadmap。现在你可以开始构建了。

**创建文件:** `PROJECT.md`, `REQUIREMENTS.md`, `ROADMAP.md`, `STATE.md`, `.planning/research/`

---

#### 步骤 2: 讨论阶段

```
/gsd-discuss-phase 1
```

这是你塑造实现的地方。你的 roadmap 每个阶段只有一两句话，不足以构建你想象的东西。此步骤在研究或规划之前捕获你的偏好。

系统分析阶段并根据构建内容识别灰色区域：
- **视觉功能** → 布局、密度、交互、空状态
- **APIs/CLIs** → 响应格式、标志、错误处理、详细程度
- **内容系统** → 结构、语气、深度、流程
- **组织任务** → 分组标准、命名、重复、异常

**创建文件:** `{phase_num}-CONTEXT.md`

> **Assumptions Mode:** 代码库分析优先于提问？在 `/gsd-settings` 设置 `workflow.discuss_mode` 为 `assumptions`。

---

#### 步骤 3: 规划阶段

```
/gsd-plan-phase 1
```

系统执行：

1. **Researches** — 调查如何实现此阶段，由你的 CONTEXT.md 决策指导
2. **Plans** — 创建 2-3 个原子任务计划，XML 结构
3. **Verifies** — 检查计划是否满足需求，循环直到通过

每个计划足够小，可在新的上下文窗口中执行。无降解，无"我现在会更简洁"。

**创建文件:** `{phase_num}-RESEARCH.md`, `{phase_num}-{N}-PLAN.md`

---

#### 步骤 4: 执行阶段

```
/gsd-execute-phase 1
```

系统执行：

1. **Runs plans in waves** — 可能时并行，依赖时顺序
2. **Fresh context per plan** — 200k token 纯用于实现，零累积垃圾
3. **Commits per task** — 每个任务获得自己的原子提交
4. **Verifies against goals** — 检查代码库是否交付阶段承诺

离开，回来时已完成工作，有干净的 git 历史。

**Wave 执行如何工作:**

```
┌────────────────────────────────────────────────────────────────────┐
│  PHASE EXECUTION                                                   │
├────────────────────────────────────────────────────────────────────┤
│                                                                    │
│  WAVE 1 (parallel)          WAVE 2 (parallel)          WAVE 3      │
│  ┌─────────┐ ┌─────────┐    ┌─────────┐ ┌─────────┐    ┌─────────┐ │
│  │ Plan 01 │ │ Plan 02 │ →  │ Plan 03 │ │ Plan 04 │ →  │ Plan 05 │ │
│  │ User    │ │ Product │    │ Orders  │ │ Cart    │    │ Checkout│ │
│  │ Model   │ │ Model   │    │ API     │ │ API     │    │ UI      │ │
│  └─────────┘ └─────────┘    └─────────┘ └─────────┘    └─────────┘ │
│       │           │              ↑           ↑              ↑      │
│       └───────────┴──────────────┴───────────┘              │      │
│              Dependencies: Plan 03 needs Plan 01            │      │
│                          Plan 04 needs Plan 02              │      │
│                          Plan 05 needs Plans 03 + 04        │      │
└────────────────────────────────────────────────────────────────────┘
```

**创建文件:** `{phase_num}-{N}-SUMMARY.md`, `{phase_num}-VERIFICATION.md`

---

#### 步骤 5: 验证工作

```
/gsd-verify-work 1
```

这是你确认它实际工作的地方。自动化验证检查代码存在和测试通过。但功能是否按你预期工作？这是你使用它的机会。

系统执行：

1. **Extracts testable deliverables** — 你现在应该能做什么
2. **Walks you through one at a time** — "你能用 email 登录吗？" 是/否，或描述问题
3. **Diagnoses failures automatically** — 调用调试代理找根因
4. **Creates verified fix plans** — 准备立即重新执行

如果一切通过，继续。如果有问题，不需要手动调试 —— 只需用它创建的修复计划再次运行 `/gsd-execute-phase`。

**创建文件:** `{phase_num}-UAT.md`, 修复计划（如果发现问题）

---

#### 步骤 6: 循环 → 发布 → 完成 → 下一个里程碑

```
/gsd-discuss-phase 2
/gsd-plan-phase 2
/gsd-execute-phase 2
/gsd-verify-work 2
/gsd-ship 2                  # 创建 PR
...
/gsd-complete-milestone
/gsd-new-milestone
```

或让 GSD 自动确定下一步：

```
/gsd-next                    # 自动检测并运行下一步
```

---

### 3.3 Quick Mode（快速模式）

```
/gsd-quick
```

**用于不需要完整规划的临时任务。**

快速模式给你 GSD 保证（原子提交、状态追踪），但更快：
- **Same agents** — 规划器 + 执行器，相同质量
- **Skips optional steps** — 无研究、无计划检查器、无验证器
- **Separate tracking** — 存在于 `.planning/quick/`，不是阶段

**Flags:**

| Flag | 说明 |
|------|------|
| `--discuss` | 轻量级讨论，在规划前暴露灰色区域 |
| `--research` | 调用聚焦研究器在规划前调查 |
| `--full` | 启用所有阶段 —— 讨论 + 研究 + 计划检查 + 验证 |
| `--validate` | 启用计划检查 + 后执行验证 |

Flags 可组合: `--discuss --research --validate`

---

### 3.4 Brownfield Workflow（现有代码库）

```
/gsd-map-codebase
         │
         ├── Stack Mapper     -> codebase/STACK.md
         ├── Arch Mapper      -> codebase/ARCHITECTURE.md
         ├── Convention Mapper -> codebase/CONVENTIONS.md
         └── Concern Mapper   -> codebase/CONCERNS.md
                │
        ┌───────▼──────────┐
        │ /gsd-new-project │  <- 问题聚焦于你添加什么
        └──────────────────┘
```

---

## 四、命令列表（69 个）

### 4.1 核心工作流

| 命令 | 说明 |
|------|------|
| `/gsd-new-project [--auto]` | 完整初始化：问题 → 研究 → 需求 → roadmap |
| `/gsd-discuss-phase [N] [--auto] [--analyze] [--chain]` | 捕获实现决策 |
| `/gsd-plan-phase [N] [--auto] [--reviews]` | 研究 + 计划 + 验证 |
| `/gsd-execute-phase <N>` | 在并行 waves 中执行所有计划 |
| `/gsd-verify-work [N]` | 手动 UAT |
| `/gsd-ship [N] [--draft]` | 从验证阶段工作创建 PR |
| `/gsd-next` | 自动推进到下一个逻辑工作流步骤 |
| `/gsd-fast <text>` | 内联 trivial 任务 —— 跳过规划，立即执行 |
| `/gsd-audit-milestone` | 验证里程碑达到完成定义 |
| `/gsd-complete-milestone` | 归档里程碑，标记发布 |
| `/gsd-new-milestone [name]` | 启动下一个版本 |
| `/gsd-forensics [desc]` | 失败工作流运行的事后调查 |

### 4.2 导航

| 命令 | 说明 |
|------|------|
| `/gsd-progress` | 我在哪里？下一步是什么？ |
| `/gsd-help` | 显示所有命令 |
| `/gsd-update` | 更新 GSD |
| `/gsd-manager` | 交互式命令中心 |
| `/gsd-join-discord` | 加入 Discord 社区 |

### 4.3 Phase 管理

| 命令 | 说明 |
|------|------|
| `/gsd-add-phase` | 添加阶段到 roadmap |
| `/gsd-insert-phase [N]` | 在阶段之间插入紧急工作 |
| `/gsd-remove-phase [N]` | 移除未来阶段，重新编号 |
| `/gsd-list-phase-assumptions [N]` | 在规划前看 Claude 的预期方法 |

### 4.4 Brownfield

| 命令 | 说明 |
|------|------|
| `/gsd-map-codebase [area]` | 在 new-project 前分析现有代码库 |

### 4.5 Session

| 命令 | 说明 |
|------|------|
| `/gsd-pause-work` | 创建 handoff（写 HANDOFF.json） |
| `/gsd-resume-work` | 从上次会话恢复 |
| `/gsd-session-report` | 生成会话摘要 |

### 4.6 UI 设计

| 命令 | 说明 |
|------|------|
| `/gsd-ui-phase [N]` | 为前端阶段生成 UI 设计契约 |
| `/gsd-ui-review [N]` | 对实现的前端代码进行事后 6-pillar 视觉审计 |

### 4.7 代码质量

| 命令 | 说明 |
|------|------|
| `/gsd-review` | 当前阶段或分支的跨 AI peer review |
| `/gsd-secure-phase [N]` | 安全执行，威胁模型锚定验证 |
| `/gsd-code-review` | 代码审查 |
| `/gsd-code-review-fix` | 修复审查发现 |
| `/gsd-audit-fix` | 审计 + 分类 + 修复（自动） |
| `/gsd-pr-branch` | 创建干净 PR 分支，过滤 `.planning/` 提交 |
| `/gsd-audit-uat` | 审计验证债务 —— 找缺少 UAT 的阶段 |
| `/gsd-docs-update` | 验证文档生成 |

### 4.8 Backlog & Threads

| 命令 | 说明 |
|------|------|
| `/gsd-plant-seed <idea>` | 捕获前瞻想法，触发条件 —— 在正确的里程碑浮现 |
| `/gsd-add-backlog <desc>` | 添加想法到 backlog parking lot |
| `/gsd-review-backlog` | 审查 backlog 项目 |
| `/gsd-thread [name]` | 持久上下文线程 |
| `/gsd-add-todo [desc]` | 捕获稍后的想法 |
| `/gsd-check-todos` | 列出待办 todos |
| `/gsd-note <text>` | 无摩擦想法捕获 |

### 4.9 Workstreams

| 命令 | 说明 |
|------|------|
| `/gsd-workstreams list` | 显示所有 workstreams 及状态 |
| `/gsd-workstreams create <name>` | 创建 namespaced workstream |
| `/gsd-workstreams switch <name>` | 切换活动 workstream |
| `/gsd-workstreams complete <name>` | 完成并合并 workstream |

### 4.10 Multi-Project Workspaces

| 命令 | 说明 |
|------|------|
| `/gsd-new-workspace` | 创建隔离 workspace |
| `/gsd-list-workspaces` | 显示所有 GSD workspaces |
| `/gsd-remove-workspace` | 移除 workspace |

### 4.11 Utilities

| 命令 | 说明 |
|------|------|
| `/gsd-settings` | 配置模型 profile 和工作流 agents |
| `/gsd-set-profile <profile>` | 切换模型 profile |
| `/gsd-debug [desc]` | 系统化调试 |
| `/gsd-do <text>` | 自动路由自由文本到正确的 GSD 命令 |
| `/gsd-health [--repair]` | 验证 `.planning/` 目录完整性 |
| `/gsd-stats` | 显示项目统计 |
| `/gsd-profile-user` | 从会话分析生成开发者行为 profile |
| `/gsd-graphify` | 为规划 agents 带来知识图 |

### 4.12 Exploration & Discovery

| 命令 | 说明 |
|------|------|
| `/gsd-explore` | 苏格拉底式探索 |
| `/gsd-intel refresh/query/status/diff` | 代码库智能系统 |
| `/gsd-scan` | 快速技术 + 架构概览 |

---

## 五、Agents 列表（24 个）

| Agent | 职责 |
|-------|------|
| `gsd-project-researcher` | 新项目研究 |
| `gsd-phase-researcher` | 阶段研究 |
| `gsd-domain-researcher` | 领域研究 |
| `gsd-advisor-researcher` | 讨论阶段顾问研究 |
| `gsd-ui-researcher` | UI 设计契约研究 |
| `gsd-ai-researcher` | AI 集成研究 |
| `gsd-research-synthesizer` | 研究综合 |
| `gsd-planner` | 创建阶段计划 |
| `gsd-plan-checker` | 验证计划质量 |
| `gsd-roadmapper` | Roadmap 创建 |
| `gsd-executor` | 执行实现计划 |
| `gsd-verifier` | 后执行验证 |
| `gsd-debugger` | 诊断代理 |
| `gsd-debug-session-manager` | 调试会话管理 |
| `gsd-code-fixer` | 代码修复 |
| `gsd-code-reviewer` | 代码审查 |
| `gsd-codebase-mapper` | 代码库分析 |
| `gsd-pattern-mapper` | 模式映射 |
| `gsd-integration-checker` | 集成检查 |
| `gsd-ui-checker` | UI spec 验证 |
| `gsd-ui-auditor` | UI 视觉审计 |
| `gsd-security-auditor` | 安全审计 |
| `gsd-doc-writer` | 文档编写 |
| `gsd-doc-verifier` | 文档验证 |
| `gsd-user-profiler` | 用户行为 profiling |
| `gsd-nyquist-auditor` | Nyquist 验证层审计 |

---

## 六、配置参考

### 6.1 配置文件位置

`.planning/config.json`

### 6.2 核心设置

| 设置 | 选项 | 默认值 | 说明 |
|------|------|--------|------|
| `mode` | `yolo`, `interactive` | `interactive` | `yolo` 自动批准；`interactive` 每步确认 |
| `granularity` | `coarse`, `standard`, `fine` | `standard` | 控制阶段数量：coarse (3-5), standard (5-8), fine (8-12) |
| `model_profile` | `quality`, `balanced`, `budget`, `inherit` | `balanced` | 每个 agent 的模型层级 |

### 6.3 Model Profiles

| Profile | Planning | Execution | Verification |
|---------|----------|-----------|--------------|
| `quality` | Opus | Opus | Sonnet |
| `balanced` (default) | Opus | Sonnet | Sonnet |
| `budget` | Sonnet | Sonnet | Haiku |
| `inherit` | 继承 | 继承 | 继承 |

### 6.4 Workflow Toggles

所有 workflow toggles 遵循 **absent = enabled** 模式。

| 设置 | 默认值 | 说明 |
|------|--------|------|
| `workflow.research` | `true` | 每个阶段规划前的领域调查 |
| `workflow.plan_check` | `true` | 计划验证循环（最多 3 次） |
| `workflow.verifier` | `true` | 后执行验证 |
| `workflow.auto_advance` | `false` | 自动链 discuss → plan → execute |
| `workflow.nyquist_validation` | `true` | 规划阶段研究的测试覆盖映射 |
| `workflow.ui_phase` | `true` | 为前端阶段生成 UI 设计契约 |
| `workflow.tdd_mode` | `false` | TDD 管道作为一等执行模式 |
| `workflow.code_review` | `true` | 启用代码审查命令 |
| `workflow.use_worktrees` | `true` | 并行执行的 git worktree 隔离 |

### 6.5 Parallelization Settings

| 设置 | 默认值 | 说明 |
|------|--------|------|
| `parallelization.enabled` | `true` | 并行运行独立计划 |
| `parallelization.max_concurrent_agents` | `3` | 最大并发 agents |
| `parallelization.min_plans_for_parallel` | `2` | 触发并行执行的最小计划数 |

### 6.6 Git Branching

| 设置 | 选项 | 默认值 | 说明 |
|------|------|--------|------|
| `git.branching_strategy` | `none`, `phase`, `milestone` | `none` | 分支创建策略 |

**Strategies:**
- **`none`** — 提交到当前分支（默认）
- **`phase`** — 每阶段一个分支，阶段完成时合并
- **`milestone`** — 整个里程碑一个分支，完成时合并

### 6.7 Recommended Presets

| 场景 | mode | granularity | profile | research | plan_check | verifier |
|------|------|-------------|---------|----------|------------|----------|
| Prototyping | `yolo` | `coarse` | `budget` | `false` | `false` | `false` |
| Normal development | `interactive` | `standard` | `balanced` | `true` | `true` | `true` |
| Production release | `interactive` | `fine` | `quality` | `true` | `true` | `true` |

---

## 七、文件系统结构

### 7.1 `.planning/` 目录结构

```
.planning/
├── PROJECT.md              # 项目愿景，始终加载
├── REQUIREMENTS.md         # 分范围的 v1/v2 需求，阶段可追溯
├── ROADMAP.md              # 目标，已完成内容
├── STATE.md                # 决策、阻塞、位置 —— 跨会话记忆
├── config.json             # 项目设置
│
├── phases/                 # 阶段目录
│   ├── 01-setup/
│   │   ├── 01-01-PLAN.md
│   │   ├── 01-01-SUMMARY.md
│   │   ├── 01-CONTEXT.md
│   │   ├── 01-RESEARCH.md
│   │   ├── 01-VERIFICATION.md
│   │   └── 01-UAT.md
│   ├── 02-core-feature/
│   └── ...
│
├── research/               # 生态系统知识
│   ├── STACK.md
│   ├── FEATURES.md
│   ├── ARCHITECTURE.md
│   └── PITFALLS.md
│
├── todos/                  # 捕获的想法和任务
├── threads/                # 持久上下文线程
├── seeds/                  # 前瞻想法
├── quick/                  # Quick mode 任务
├── workstreams/            # Workstream 状态
├── intel/                  # 代码库智能索引
└── codebase/               # Brownfield 映射
```

### 7.2 Artifact 说明

| 文件 | 作用 |
|------|------|
| `PROJECT.md` | 项目愿景，始终加载 |
| `REQUIREMENTS.md` | 分范围的 v1/v2 需求，阶段可追溯 |
| `ROADMAP.md` | 目标，已完成内容 |
| `STATE.md` | 决策、阻塞、位置 —— 跨会话记忆 |
| `PLAN.md` | 原子任务，XML 结构，验证步骤 |
| `SUMMARY.md` | 发生了什么，改变了什么，提交历史 |
| `CONTEXT.md` | 阶段讨论决策 |
| `RESEARCH.md` | 阶段研究发现 |
| `VERIFICATION.md` | 后执行自动验证结果 |
| `UAT.md` | 用户验收测试结果 |
| `UI-SPEC.md` | UI 设计契约 |
| `VALIDATION.md` | Nyquist 验证层反馈契约 |

---

## 八、XML 提示格式化

每个计划是结构化 XML，为 Claude 优化：

```xml
<task type="auto">
  <name>Create login endpoint</name>
  <files>src/app/api/auth/login/route.ts</files>
  <action>
    Use jose for JWT (not jsonwebtoken - CommonJS issues).
    Validate credentials against users table.
    Return httpOnly cookie on success.
  </action>
  <verify>curl -X POST localhost:3000/api/auth/login returns 200 + Set-Cookie</verify>
  <done>Valid credentials return cookie, invalid return 401</done>
</task>
```

精确指令，无猜测，验证内置。

---

## 九、安全机制

### 9.1 Defense-in-Depth（v1.27+）

- **Path traversal prevention** — 所有用户提供的文件路径验证
- **Prompt injection detection** — `security.cjs` 模块扫描注入模式
- **PreToolUse prompt guard hook** — 扫描 `.planning/` 写入
- **Safe JSON parsing** — 捕获畸形参数
- **Shell argument validation** — 用户文本在 shell 插值前清理

### 9.2 保护敏感文件

在 `.claude/settings.json` 添加 deny 列表：

```json
{
  "permissions": {
    "deny": [
      "Read(.env)",
      "Read(.env.*)",
      "Read(**/secrets/*)",
      "Read(**/*credential*)",
      "Read(**/*.pem)",
      "Read(**/*.key)"
    ]
  }
}
```

---

## 十、卸载

```bash
# Global installs
npx get-shit-done-cc --claude --global --uninstall
npx get-shit-done-cc --opencode --global --uninstall
npx get-shit-done-cc --gemini --global --uninstall
npx get-shit-done-cc --codex --global --uninstall
# ... 其他运行时

# Local installs
npx get-shit-done-cc --claude --local --uninstall
# ... 其他运行时
```

---

## 十一、参考资源

| 资源 | 链接 |
|------|------|
| GitHub 仓库 | https://github.com/gsd-build/get-shit-done |
| npm 包 | https://www.npmjs.com/package/get-shit-done-cc |
| Discord | https://discord.gg/mYgfVNfA2r |
| X (Twitter) | https://x.com/gsd_foundation |

---

## 十二、统计数据

| 指标 | 数值 |
|------|------|
| Commands | 69 |
| Workflows | 68 |
| Agents | 24 |
| References | 35 |
| Templates | 多套 |
| 支持的运行时 | 15+ |

---

## 十三、总结

GSD 是为**描述想要什么并让它正确构建的人**设计的 —— 不需要假装在运营 50 人的工程组织。

**核心价值:**

| 特性 | 价值 |
|------|------|
| **Context Engineering** | 给 AI 每个任务所需的所有上下文 |
| **Fresh Context Per Agent** | 消除上下文腐烂，200K 干净上下文 |
| **Multi-Agent Orchestration** | 薄编排器 + 专业化代理 |
| **Spec-Driven Development** | 需求 → 研究 → 计划 → 执行 → 验证 |
| **Atomic Git Commits** | 每个任务独立提交，可追溯 |
| **State Management** | 跨会话持久化 |

---

*文档由 Claude Code 根据 GitHub 仓库内容生成*