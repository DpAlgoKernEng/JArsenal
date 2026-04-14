# OpenSpec 使用文档

> 创建日期：2026-04-14
> 版本：OpenSpec latest
> 开发者：Fission AI

## 一、概述

**OpenSpec** 是由 Fission AI 开发的轻量级规格框架，为 AI 编程助手添加一个规格层，让你和 AI 在编写代码前先对齐要构建的内容。

### 项目定位

> "The most loved spec framework."

AI 编程助手很强大，但当需求只存在于聊天历史中时，结果不可预测。OpenSpec 在代码编写前添加一个轻量级规格层，确保人机对齐。

### 设计哲学

```text
→ fluid not rigid          — 无阶段门槛，工作有意义的内容
→ iterative not waterfall  — 构建中学习，迭代中细化
→ easy not complex         — 轻量设置，最小仪式
→ built for brownfield     — 适用于现有代码库，不仅是新项目
→ scalable from personal to enterprises — 从个人项目到企业级可扩展
```

### 核心价值

| 特点 | 说明 |
|------|------|
| **Agree before you build** | 人机在写代码前先对齐规格 |
| **Stay organized** | 每个变更有自己的文件夹：proposal、specs、design、tasks |
| **Work fluidly** | 随时更新任何 artifact，无刚性阶段门槛 |
| **Use your tools** | 支持 25+ AI 助手，通过斜杠命令集成 |

### 与其他工具对比

| 对比 | 说明 |
|------|------|
| **vs. Spec Kit (GitHub)** | Spec Kit 详尽但重量级，刚性阶段门槛。OpenSpec 更轻量灵活。 |
| **vs. Kiro (AWS)** | Kiro 强大但锁定 IDE 和模型。OpenSpec 与你现有工具配合。 |
| **vs. nothing** | 无规格的 AI 编程意味着模糊提示和不可预测结果。OpenSpec 带来可预测性。 |

---

## 二、安装配置

### 2.1 系统要求

| 要求 | 版本 |
|------|------|
| Node.js | ≥20.19.0 |
| npm/pnpm/yarn/bun | 任选其一 |

### 2.2 安装

```bash
# npm 全局安装
npm install -g @fission-ai/openspec@latest

# 或使用其他包管理器
pnpm add -g @fission-ai/openspec@latest
yarn global add @fission-ai/openspec@latest
bun add -g @fission-ai/openspec@latest
```

### 2.3 初始化项目

```bash
cd your-project
openspec init
```

初始化后创建的目录结构：

```
openspec/
├── specs/              # 系统行为规格（真理来源）
│   └── <domain>/
│       └── spec.md
├── changes/            # 提议的修改（每个变更一个文件夹）
│   └── <change-name>/
│       ├── proposal.md
│       ├── design.md
│       ├── tasks.md
│       └── specs/      # Delta specs
└── config.yaml         # 项目配置（可选）

.claude/skills/         # Claude Code skills
.cursor/skills/         # Cursor skills
...                     # 其他工具配置
```

### 2.4 非交互式初始化

```bash
# 配置特定工具
openspec init --tools claude,cursor

# 配置所有支持的工具
openspec init --tools all

# 跳过工具配置
openspec init --tools none

# 指定 profile
openspec init --profile core

# 强制清理旧文件
openspec init --force
```

### 2.5 支持的工具（25+）

| Tool ID | Skills 路径 | Commands 路径 |
|---------|-------------|---------------|
| `claude` | `.claude/skills/openspec-*/SKILL.md` | `.claude/commands/opsx/<id>.md` |
| `cursor` | `.cursor/skills/openspec-*/SKILL.md` | `.cursor/commands/opsx-<id>.md` |
| `codex` | `.codex/skills/openspec-*/SKILL.md` | `$CODEX_HOME/prompts/opsx-<id>.md` |
| `windsurf` | `.windsurf/skills/openspec-*/SKILL.md` | `.windsurf/workflows/opsx-<id>.md` |
| `gemini` | `.gemini/skills/openspec-*/SKILL.md` | `.gemini/commands/opsx-<id>.toml` |
| `kiro` | `.kiro/skills/openspec-*/SKILL.md` | `.kiro/prompts/opsx-<id>.prompt.md` |
| `opencode` | `.opencode/skills/openspec-*/SKILL.md` | `.opencode/commands/opsx-<id>.md` |
| `cline` | `.cline/skills/openspec-*/SKILL.md` | `.clinerules/workflows/opsx-<id>.md` |
| `factory` | `.factory/skills/openspec-*/SKILL.md` | `.factory/commands/opsx-<id>.md` |
| `continue` | `.continue/skills/openspec-*/SKILL.md` | `.continue/prompts/opsx-<id>.prompt` |
| `github-copilot` | `.github/skills/openspec-*/SKILL.md` | `.github/prompts/opsx-<id>.prompt.md` |
| `amazon-q` | `.amazonq/skills/openspec-*/SKILL.md` | `.amazonq/prompts/opsx-<id>.md` |
| `antigravity` | `.agent/skills/openspec-*/SKILL.md` | `.agent/workflows/opsx-<id>.md` |
| `auggie` | `.augment/skills/openspec-*/SKILL.md` | `.augment/commands/opsx-<id>.md` |
| `roocode` | `.roo/skills/openspec-*/SKILL.md` | `.roo/commands/opsx-<id>.md` |
| `junie` | `.junie/skills/openspec-*/SKILL.md` | `.junie/commands/opsx-<id>.md` |
| `kilocode` | `.kilocode/skills/openspec-*/SKILL.md` | `.kilocode/workflows/opsx-<id>.md` |
| `trae` | `.trae/skills/openspec-*/SKILL.md` | Not generated |
| `forgecode` | `.forge/skills/openspec-*/SKILL.md` | Not generated |
| `pi` | `.pi/skills/openspec-*/SKILL.md` | `.pi/prompts/opsx-<id>.md` |
| `qoder` | `.qoder/skills/openspec-*/SKILL.md` | `.qoder/commands/opsx/<id>.md` |
| `qwen` | `.qwen/skills/openspec-*/SKILL.md` | `.qwen/commands/opsx-<id>.toml` |
| `codebuddy` | `.codebuddy/skills/openspec-*/SKILL.md` | `.codebuddy/commands/opsx/<id>.md` |
| `crush` | `.crush/skills/openspec-*/SKILL.md` | `.crush/commands/opsx/<id>.md` |
| `iflow` | `.iflow/skills/openspec-*/SKILL.md` | `.iflow/commands/opsx-<id>.md` |
| `bob` | `.bob/skills/openspec-*/SKILL.md` | `.bob/commands/opsx-<id>.md` |
| `costrict` | `.cospec/skills/openspec-*/SKILL.md` | `.cospec/openspec/commands/opsx-<id>.md` |

### 2.6 更新

```bash
# 升级 CLI
npm install -g @fission-ai/openspec@latest

# 在项目中更新 AI 指导文件
openspec update
```

---

## 三、OPSX Workflow

### 3.1 核心理念

**传统工作流 vs OPSX**：

```
传统（阶段锁定）：

  PLANNING ────────► IMPLEMENTING ────────► DONE
      │                    │
      │   "Can't go back"  │
      └────────────────────┘

OPSX（流体行动）：

  proposal ──► specs ──► design ──► tasks ──► implement
       ▲          ▲          ▲                    │
       └──────────┴──────────┴────────────────────┘
                update as you learn
```

**关键原则**：
- **Actions, not phases** — 命令是你能做的事，不是你被困的阶段
- **Dependencies are enablers** — 依赖关系显示什么是可能的，不是下一步必须做什么

### 3.2 两种模式

#### 默认快速路径（core profile）

```text
/opsx:propose ──► /opsx:apply ──► /opsx:archive
```

包含命令：
- `/opsx:propose`
- `/opsx:explore`
- `/opsx:apply`
- `/opsx:archive`

#### 扩展工作流（custom profile）

```text
/opsx:new ──► /opsx:ff or /opsx:continue ──► /opsx:apply ──► /opsx:verify ──► /opsx:archive
```

启用扩展命令：

```bash
openspec config profile
openspec update
```

---

## 四、Commands 参考

### 4.1 默认路径命令（core profile）

| 命令 | 功能 |
|------|------|
| `/opsx:propose` | 创建变更并一步生成规划 artifacts |
| `/opsx:explore` | 思考想法、调查问题，在承诺变更前澄清需求 |
| `/opsx:apply` | 实现变更中的任务 |
| `/opsx:archive` | 归档已完成的变更 |

### 4.2 扩展工作流命令（custom profile）

| 命令 | 功能 |
|------|------|
| `/opsx:new` | 启动新的变更 scaffold |
| `/opsx:continue` | 根据依赖关系创建下一个 artifact |
| `/opsx:ff` | Fast-forward：一次创建所有规划 artifacts |
| `/opsx:verify` | 验证实现与 artifacts 匹配 |
| `/opsx:sync` | 将 delta specs 合并到主 specs |
| `/opsx:bulk-archive` | 批量归档多个变更 |
| `/opsx:onboard` | 完整工作流引导教程 |

---

## 五、Commands 详细说明

### 5.1 `/opsx:propose`

**语法**：
```text
/opsx:propose [change-name-or-description]
```

**功能**：
- 创建 `openspec/changes/<change-name>/`
- 生成实现前需要的 artifacts（proposal, specs, design, tasks）
- 变更准备好后停止，等待 `/opsx:apply`

**示例**：
```text
You: /opsx:propose add-dark-mode

AI:  Created openspec/changes/add-dark-mode/
     ✓ proposal.md — why we're doing this, what's changing
     ✓ specs/       — requirements and scenarios
     ✓ design.md    — technical approach
     ✓ tasks.md     — implementation checklist
     Ready for implementation!
```

### 5.2 `/opsx:explore`

**语法**：
```text
/opsx:explore [topic]
```

**功能**：
- 无结构的探索性对话
- 调查代码库回答问题
- 比较选项和方法
- 创建可视化图表澄清思考
- 想法明确时可转向 `/opsx:propose`

**示例**：
```text
You: /opsx:explore

AI:  What would you like to explore?

You: How should we handle authentication for the mobile app?

AI:  Let me investigate your current auth setup...
     [Analyzes codebase]

     I see you're using session-based auth for the web app.
     For mobile, we have three main options:
     1. JWT tokens (stateless, works offline)
     2. OAuth2 (if you need third-party login)
     3. Extend current sessions

You: Let's go with JWT.

AI:  Ready when you are. Run /opsx:propose add-jwt-auth to begin.
```

### 5.3 `/opsx:new`

**语法**：
```text
/opsx:new [change-name] [--schema <schema-name>]
```

**功能**：
- 创建 `openspec/changes/<change-name>/` 目录
- 创建 `.openspec.yaml` 元数据文件
- 显示第一个 artifact 模板待创建

**示例**：
```text
You: /opsx:new add-dark-mode

AI:  Created openspec/changes/add-dark-mode/
     Schema: spec-driven

     Ready to create: proposal
     Use /opsx:continue to create it, or /opsx:ff to create all.
```

### 5.4 `/opsx:continue`

**语法**：
```text
/opsx:continue [change-name]
```

**功能**：
- 查询 artifact 依赖图
- 显示哪些 artifacts 已就绪 vs 阻塞
- 创建第一个就绪的 artifact
- 读取依赖文件获取上下文

**示例**：
```text
You: /opsx:continue

AI:  Change: add-dark-mode
     Ready: specs, design
     Blocked: tasks (needs design)

     Creating: specs

     ✓ Created specs/ui/spec.md
     Next ready: design
```

### 5.5 `/opsx:ff`

**语法**：
```text
/opsx:ff [change-name]
```

**功能**：
- 创建所有规划 artifacts 一步完成
- 从 proposal → specs → design → tasks

**示例**：
```text
You: /opsx:ff

AI:  Creating all planning artifacts...
     ✓ proposal.md
     ✓ specs/ui/spec.md
     ✓ design.md
     ✓ tasks.md
     Ready for implementation!
```

### 5.6 `/opsx:apply`

**语法**：
```text
/opsx:apply [change-name]
```

**功能**：
- 实现变更中的任务
- 按顺序执行 tasks.md 中的检查项
- 原子提交每个完成的任务

**示例**：
```text
You: /opsx:apply

AI:  Working through tasks...
     ✓ 1.1 Add theme context provider
     ✓ 1.2 Create toggle component
     ✓ 2.1 Add CSS variables
     ✓ 2.2 Wire up localStorage
     All tasks complete!
```

### 5.7 `/opsx:verify`

**语法**：
```text
/opsx:verify [change-name]
```

**功能**：
- 验证实现与 artifacts 匹配
- 检查每个任务是否正确实现
- 报告偏差和缺失

### 5.8 `/opsx:archive`

**语法**：
```text
/opsx:archive [change-name]
```

**功能**：
- 合并 delta specs 到主 specs
- 移动变更到 archive 目录

**示例**：
```text
You: /opsx:archive

AI:  ✓ Merged specs
     ✓ Archived to openspec/changes/archive/2025-01-23-add-dark-mode/
     Specs updated. Ready for the next feature.
```

---

## 六、工作流模式

### 6.1 Quick Feature（快速功能）

**适用场景**：知道要构建什么，只需执行

```text
/opsx:propose ──► /opsx:apply ──► /opsx:archive
```

或扩展模式：

```text
/opsx:new ──► /opsx:ff ──► /opsx:apply ──► /opsx:verify ──► /opsx:archive
```

### 6.2 Exploratory（探索性）

**适用场景**：需求不清晰或需要先调查

```text
/opsx:explore ──► /opsx:propose ──► /opsx:apply ──► /opsx:archive
```

### 6.3 Parallel Changes（并行变更）

**适用场景**：多个变更同时进行

```text
/opsx:new feature-a ──► /opsx:ff
/opsx:new feature-b ──► /opsx:ff
/opsx:apply feature-a
/opsx:apply feature-b
/opsx:archive feature-a
/opsx:archive feature-b
```

---

## 七、Artifacts 说明

### 7.1 Artifact 类型

| Artifact | 目的 |
|----------|------|
| `proposal.md` | "为什么"和"什么" — 意图、范围、方法 |
| `specs/` | Delta specs：ADDED/MODIFIED/REMOVED 需求 |
| `design.md` | "如何" — 技术方法和架构决策 |
| `tasks.md` | 实现检查清单 |

### 7.2 Artifact 依赖图

```
proposal ──► specs ──► design ──► tasks ──► implement
    ▲           ▲          ▲                    │
    └───────────┴──────────┴────────────────────┘
             update as you learn
```

---

## 八、Delta Specs 格式

### 8.1 Delta Specs 结构

```markdown
# Delta for Auth

## ADDED Requirements

### Requirement: Two-Factor Authentication
The system MUST require a second factor during login.

#### Scenario: OTP required
- GIVEN a user with 2FA enabled
- WHEN the user submits valid credentials
- THEN an OTP challenge is presented

## MODIFIED Requirements

### Requirement: Session Timeout
The system SHALL expire sessions after 30 minutes of inactivity.
(Previously: 60 minutes)

## REMOVED Requirements

### Requirement: Remember Me
(Deprecated in favor of 2FA)
```

### 8.2 Archive 时的处理

1. **ADDED** 需求追加到主 spec
2. **MODIFIED** 需求替换现有版本
3. **REMOVED** 需求从主 spec 删除

---

## 九、项目配置

### 9.1 配置文件

```yaml
# openspec/config.yaml
schema: spec-driven

context: |
  Tech stack: TypeScript, React, Node.js, PostgreSQL
  API style: RESTful, documented in docs/api.md
  Testing: Jest + React Testing Library
  We value backwards compatibility for all public APIs

rules:
  proposal:
    - Include rollback plan
    - Identify affected teams
  specs:
    - Use Given/When/Then format
    - Reference existing patterns before inventing new ones
  design:
    - Include sequence diagrams for complex flows
  tasks:
    - Add Windows CI verification when changes involve file paths
```

### 9.2 配置字段

| 字段 | 类型 | 说明 |
|------|------|------|
| `schema` | string | 新变更的默认 schema |
| `context` | string | 注入到所有 artifact 的项目上下文 |
| `rules` | object | 每个 artifact 的特定规则 |

### 9.3 Schema 解析顺序

1. CLI flag：`--schema <name>`
2. Change metadata（`.openspec.yaml`）
3. Project config（`openspec/config.yaml`）
4. Default：`spec-driven`

---

## 十、CLI 命令参考

### 10.1 命令分类

| 类别 | 命令 | 目的 |
|------|------|------|
| **Setup** | `init`, `update` | 初始化和更新项目 |
| **Browse** | `list`, `view`, `show` | 浏览变更和 specs |
| **Validation** | `validate` | 检查变更和 specs 问题 |
| **Lifecycle** | `archive` | 完成变更 |
| **Workflow** | `status`, `instructions`, `templates`, `schemas` | Artifact-driven 工作流支持 |
| **Schemas** | `schema init`, `schema fork`, `schema validate` | 创建和管理自定义工作流 |
| **Config** | `config` | 查看和修改设置 |

### 10.2 常用 CLI 命令

```bash
# 初始化
openspec init

# 更新
openspec update

# 列出变更
openspec list

# 查看变更
openspec show add-dark-mode

# 验证
openspec validate

# 状态
openspec status

# 配置 profile
openspec config profile

# 配置编辑
openspec config edit
```

### 10.3 Agent 兼容命令（JSON 输出）

```bash
openspec list --json
openspec show add-dark-mode --json
openspec validate --all --json
openspec status --json
openspec instructions --json
openspec templates --json
openspec schemas --json
```

---

## 十一、自定义 Schema

### 11.1 Fork 现有 Schema

```bash
openspec schema fork spec-driven my-workflow
```

创建结构：
```
openspec/schemas/my-workflow/
├── schema.yaml           # 工作流定义
└── templates/
    ├── proposal.md       # Proposal 模板
    ├── spec.md           # Specs 模板
    ├── design.md         # Design 模板
    └── tasks.md          # Tasks 模板
```

### 11.2 创建新 Schema

```bash
# 交互式
openspec schema init research-first

# 非交互式
openspec schema init rapid \
  --description "Rapid iteration workflow" \
  --artifacts "proposal,tasks" \
  --default
```

### 11.3 Schema 结构

```yaml
# schema.yaml
name: my-workflow
description: Custom workflow for my team

artifacts:
  - id: proposal
    description: "Why we're doing this change"
    template: proposal.md
    dependencies: []

  - id: specs
    description: "What requirements change"
    template: spec.md
    dependencies: [proposal]

  - id: design
    description: "How we implement it"
    template: design.md
    dependencies: [proposal, specs]

  - id: tasks
    description: "Implementation checklist"
    template: tasks.md
    dependencies: [design]
```

---

## 十二、实战示例

### 12.1 完整功能开发流程

```text
# 1. 提议功能
You: /opsx:propose add-dark-mode

AI:  Created openspec/changes/add-dark-mode/
     ✓ proposal.md — why we're doing this, what's changing
     ✓ specs/       — requirements and scenarios
     ✓ design.md    — technical approach
     ✓ tasks.md     — implementation checklist
     Ready for implementation!

# 2. 实现任务
You: /opsx:apply

AI:  Working through tasks...
     ✓ 1.1 Add theme context provider
     ✓ 1.2 Create toggle component
     ✓ 2.1 Add CSS variables
     ✓ 2.2 Wire up localStorage
     All tasks complete!

# 3. 归档
You: /opsx:archive

AI:  ✓ Merged specs
     ✓ Archived to openspec/changes/archive/2025-01-23-add-dark-mode/
     Specs updated. Ready for the next feature.
```

### 12.2 探索性工作流

```text
# 1. 探索问题
You: /opsx:explore

AI:  What would you like to explore?

You: I want to improve page load performance but I'm not sure
     where the bottlenecks are.

AI:  Let me help investigate...
     [Analyzes bundle sizes, identifies slow queries]

     I found three main bottlenecks:
     1. Large unoptimized images
     2. Synchronous data fetching in ProductList
     3. Re-renders from context changes

# 2. 创建变更
You: Let's tackle the data fetching issue.
     /opsx:propose optimize-product-list-fetching

# 3. 实现
You: /opsx:apply

# 4. 归档
You: /opsx:archive
```

### 12.3 扩展模式工作流

```text
# 1. 创建 scaffold
You: /opsx:new add-logout-button

AI:  Created openspec/changes/add-logout-button/
     Ready to create: proposal

# 2. 快速创建所有 artifacts
You: /opsx:ff

AI:  Creating all planning artifacts...
     ✓ proposal.md
     ✓ specs/
     ✓ design.md
     ✓ tasks.md
     Ready for implementation!

# 3. 实现
You: /opsx:apply

# 4. 验证
You: /opsx:verify

# 5. 归档
You: /opsx:archive
```

---

## 十三、Spec 格式最佳实践

### 13.1 Spec 结构

```markdown
# Auth Specification

## Purpose
Authentication and session management for the application.

## Requirements

### Requirement: User Authentication
The system SHALL issue a JWT token upon successful login.

#### Scenario: Valid credentials
- GIVEN a user with valid credentials
- WHEN the user submits login form
- THEN a JWT token is returned
- AND the user is redirected to dashboard

#### Scenario: Invalid credentials
- GIVEN invalid credentials
- WHEN the user submits login form
- THEN an error message is displayed
- AND no token is issued

### Requirement: Session Expiration
The system MUST expire sessions after 30 minutes of inactivity.
```

### 13.2 Spec 元素

| 元素 | 目的 |
|------|------|
| `## Purpose` | 高层次领域描述 |
| `### Requirement:` | 系统必须有的具体行为 |
| `#### Scenario:` | 需求的具体示例 |
| `SHALL/MUST/SHOULD` | RFC 2119 关键字指示需求强度 |

### 13.3 Spec 应包含/不包含

**应包含**：
- 用户或下游系统依赖的可观察行为
- 输入、输出、错误条件
- 外部约束（安全、隐私、可靠性）

**不应包含**：
- 内部类/函数名
- 库或框架选择
- 步骤实现细节（这些属于 design.md 或 tasks.md）

---

## 十四、遥测配置

### 14.1 遥测说明

OpenSpec 收集匿名使用统计：
- 仅收集命令名和版本
- 无参数、路径、内容或 PII
- CI 中自动禁用

### 14.2 退出遥测

```bash
export OPENSPEC_TELEMETRY=0
# 或
export DO_NOT_TRACK=1
```

---

## 十五、模型选择建议

| 建议 | 说明 |
|------|------|
| **推荐模型** | Opus 4.5 和 GPT 5.2 用于规划和实现 |
| **上下文清洁** | 开始实现前清理上下文窗口 |
| **良好卫生** | 整个会话保持良好的上下文卫生 |

---

## 十六、常见问题

### Q1: 如何查看当前变更？

```bash
openspec list
openspec show <change-name>
```

### Q2: 如何启用扩展命令？

```bash
openspec config profile  # 选择 custom
openspec update
```

### Q3: 如何切换 profile？

```bash
openspec config profile
# 选择 core 或 custom
```

### Q4: 如何验证变更？

```bash
openspec validate
openspec validate --all --json
```

### Q5: 如何创建自定义工作流？

```bash
openspec schema fork spec-driven my-workflow
# 编辑 openspec/schemas/my-workflow/schema.yaml
```

---

## 十七、参考链接

| 资源 | 链接 |
|------|------|
| GitHub 仓库 | https://github.com/Fission-AI/OpenSpec |
| npm 包 | https://www.npmjs.com/package/@fission-ai/openspec |
| Discord 社区 | https://discord.gg/YctCnvvshC |
| 作者 Twitter | https://x.com/0xTab |
| Teams 邮箱 | teams@openspec.dev |

---

## 十八、License

MIT License

---

*文档由 Claude Code 根据实际仓库内容生成*