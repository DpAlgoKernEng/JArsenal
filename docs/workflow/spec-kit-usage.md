# GitHub Spec Kit 使用指南

## 项目概述

**GitHub Spec Kit** 是 GitHub 开源的规范驱动开发（Spec-Driven Development, SDD）工具包。它提供了一套完整的命令行工具和 AI 代理集成，帮助开发团队通过规范化流程从需求到实现的全流程管理。

### 核心特点

- **规范驱动开发**: 规范成为可执行的，代码服务于规范
- **多 AI 代理支持**: 支持 35+ AI 编码代理
- **社区扩展丰富**: 50+ 社区扩展可用
- **跨平台支持**: 同时提供 Bash (.sh) 和 PowerShell (.ps1) 脚本
- **离线安装支持**: 支持企业隔离环境部署

---

## 安装指南

### 前置条件

- **操作系统**: Linux/macOS（Windows 通过 PowerShell 支持）
- **AI 编码代理**: Claude Code、GitHub Copilot、Codebuddy CLI、Gemini CLI 或 Pi Coding Agent
- **包管理器**: uv（Python 包管理）
- **Python**: 3.11+
- **Git**: 版本控制系统

### 安装方式

#### 方式一：初始化新项目

```bash
# 使用特定稳定版本（推荐）
uvx --from git+https://github.com/github/spec-kit.git@vX.Y.Z specify init <PROJECT_NAME>

# 或使用最新主分支版本
uvx --from git+https://github.com/github/spec-kit.git specify init <PROJECT_NAME>
```

#### 方式二：在当前目录初始化

```bash
uvx --from git+https://github.com/github/spec-kit.git@vX.Y.Z specify init .
# 或使用 --here 标志
uvx --from git+https://github.com/github/spec-kit.git@vX.Y.Z specify init --here
```

#### 指定 AI 代理

```bash
uvx --from git+https://github.com/github/spec-kit.git@vX.Y.Z specify init <project_name> --ai claude
uvx --from git+https://github.com/github/spec-kit.git@vX.Y.Z specify init <project_name> --ai gemini
uvx --from git+https://github.com/github/spec-kit.git@vX.Y.Z specify init <project_name> --ai copilot
uvx --from git+https://github.com/github/spec-kit.git@vX.Y.Z specify init <project_name> --ai codebuddy
uvx --from git+https://github.com/github/spec-kit.git@vX.Y.Z specify init <project_name> --ai pi
```

#### 指定脚本类型

```bash
# 强制使用 Shell 脚本
uvx --from git+https://github.com/github/spec-kit.git@vX.Y.Z specify init <project_name> --script sh

# 强制使用 PowerShell 脚本
uvx --from git+https://github.com/github/spec-kit.git@vX.Y.Z specify init <project_name> --script ps
```

#### 忽略代理工具检查

```bash
uvx --from git+https://github.com/github/spec-kit.git@vX.Y.Z specify init <project_name> --ai claude --ignore-agent-tools
```

### 离线/企业隔离环境安装

#### 步骤 1: 在联网机器上构建 wheel 包

```bash
# 克隆仓库
git clone https://github.com/github/spec-kit.git
cd spec-kit

# 构建 wheel
pip install build
python -m build --wheel --outdir dist/

# 下载 wheel 及所有依赖
pip download -d dist/ dist/specify_cli-*.whl
```

**重要提示**: 必须在与目标机器相同的操作系统和 Python 版本上执行此步骤。

#### 步骤 2: 传输到隔离机器

将 `dist/` 目录复制到目标机器。

#### 步骤 3: 在隔离机器上安装

```bash
pip install --no-index --find-links=./dist specify-cli
```

#### 步骤 4: 初始化项目（无需网络）

```bash
specify init my-project --ai claude --offline
```

**注意**: 从 v0.6.0 开始，`--offline` 标志将被移除，默认使用内置资源。

---

## 核心工作流程

### 6 步流程概览

| 步骤 | 命令 | 描述 |
|------|------|------|
| 1 | `/speckit.constitution` | 定义项目宪法（核心规则和原则） |
| 2 | `/speckit.specify` | 创建功能规范（描述要构建的内容） |
| 3 | `/speckit.clarify` | 消除歧义，细化规范 |
| 4 | `/speckit.plan` | 创建技术实现计划 |
| 5 | `/speckit.tasks` | 分解为可执行任务 |
| 6 | `/speckit.implement` | 执行实现 |

### 上下文感知特性

Spec Kit 命令自动检测当前 Git 分支来确定活动的功能规范。切换分支即可在不同规范之间切换。

---

## 命令详解

### `/speckit.constitution` - 定义项目宪法

建立项目的核心规则和原则。

**使用示例**:

```markdown
/speckit.constitution This project follows a "Library-First" approach. All features must be implemented as standalone libraries first. We use TDD strictly. We prefer functional programming patterns.
```

**典型宪法内容**:
- 架构原则（如微服务、单体）
- 测试策略（如 TDD、BDD）
- 代码风格偏好
- 安全要求
- 性能标准

---

### `/speckit.specify` - 创建功能规范

描述要构建的功能，关注"是什么"和"为什么"，而非技术栈。

**使用示例**:

```markdown
/speckit.specify Build an application that can help me organize my photos in separate photo albums. Albums are grouped by date and can be re-organized by dragging and dropping on the main page. Albums are never in other nested albums. Within each album, photos are previewed in a tile-like interface.
```

**规范要点**:
- 功能描述清晰
- 用户场景明确
- 不涉及技术实现细节
- 可包含非功能性需求

---

### `/speckit.clarify` - 消除歧义

交互式地识别和解决规范中的歧义。

**使用示例**:

```bash
/speckit.clarify Focus on security and performance requirements.
```

**详细示例**:

```bash
/speckit.clarify I want to clarify the task card details. For each task in the UI for a task card, you should be able to change the current status of the task between the different columns in the Kanban work board. You should be able to leave an unlimited number of comments for a particular card. You should be able to, from that task card, assign one of the valid users.
```

可多次调用以逐步细化规范。

---

### `/speckit.checklist` - 验证规范

验证规范检查清单的完整性。

**使用示例**:

```bash
/speckit.checklist
```

---

### `/speckit.plan` - 创建技术计划

提供技术栈和架构选择，生成实现计划。

**使用示例**:

```bash
/speckit.plan We are going to generate this using .NET Aspire, using Postgres as the database. The frontend should use Blazor server with drag-and-drop task boards, real-time updates. There should be a REST API created with a projects API, tasks API, and a notifications API.
```

**技术计划应包含**:
- 技术栈选择
- 数据库设计
- API 设计
- 前端架构
- 部署策略

---

### `/speckit.tasks` - 分解任务

生成可执行的任务列表。

**使用示例**:

```bash
/speckit.tasks
```

---

### `/speckit.analyze` - 分析验证

审查实现计划，识别潜在问题。

**使用示例**:

```bash
/speckit.analyze
```

---

### `/speckit.implement` - 执行实现

开始实际代码实现。

**使用示例**:

```bash
/speckit.implement
```

**重要提示**: 对于复杂项目，建议分阶段实现以避免代理上下文饱和。先实现核心功能，验证后逐步添加新功能。

---

### `/speckit.taskstoissues` - 任务转 Issue

将任务转换为 GitHub Issues。

**使用示例**:

```bash
/speckit.taskstoissues
```

---

## 完整实战示例：构建 Taskify

### 步骤 1: 定义宪法

```markdown
/speckit.constitution Taskify is a "Security-First" application. All user inputs must be validated. We use a microservices architecture. Code must be fully documented.
```

### 步骤 2: 定义需求

```text
/speckit.specify Develop Taskify, a team productivity platform. It should allow users to create projects, add team members, assign tasks, comment and move tasks between boards in Kanban style. In this initial phase for this feature, let's call it "Create Taskify," let's have multiple users but the users will be declared ahead of time, predefined. I want five users in two different categories, one product manager and four engineers. Let's create three different sample projects. Let's have the standard Kanban columns for the status of each task, such as "To Do," "In Progress," "In Review," and "Done." There will be no login for this application as this is just the very first testing thing to ensure that our basic features are set up.
```

### 步骤 3: 细化规范（第一次）

```bash
/speckit.clarify I want to clarify the task card details. For each task in the UI for a task card, you should be able to change the current status of the task between the different columns in the Kanban work board. You should be able to leave an unlimited number of comments for a particular card. You should be able to, from that task card, assign one of the valid users.
```

### 步骤 4: 细化规范（第二次）

```bash
/speckit.clarify When you first launch Taskify, it's going to give you a list of the five users to pick from. There will be no password required. When you click on a user, you go into the main view, which displays the list of projects. When you click on a project, you open the Kanban board for that project. You're going to see the columns. You'll be able to drag and drop cards back and forth between different columns. You will see any cards that are assigned to you, the currently logged in user, in a different color from all the other ones, so you can quickly see yours. You can edit any comments that you make, but you can't edit comments that other people made. You can delete any comments that you made, but you can't delete comments anybody else made.
```

### 步骤 5: 验证规范

```bash
/speckit.checklist
```

### 步骤 6: 生成技术计划

```bash
/speckit.plan We are going to generate this using .NET Aspire, using Postgres as the database. The frontend should use Blazor server with drag-and-drop task boards, real-time updates. There should be a REST API created with a projects API, tasks API, and a notifications API.
```

### 步骤 7: 定义任务

```bash
/speckit.tasks
```

### 步骤 8: 分析计划

```bash
/speckit.analyze
```

### 步骤 9: 实现项目

```bash
/speckit.implement
```

**分阶段建议**: 对于大型项目如 Taskify，建议分阶段实现：
- Phase 1: 基础项目/任务结构
- Phase 2: Kanban 功能
- Phase 3: 评论和分配功能

---

## 规范驱动开发（SDD）方法论

### 核心哲学

**规范成为可执行，代码服务于规范**

传统开发流程中，规范文档往往在项目开始后就被遗忘。SDD 方法论通过以下方式改变这一点：

1. **规范是活的**: 规范文件持续更新并驱动开发
2. **代码验证规范**: 实现代码验证规范是否正确执行
3. **规范即测试**: 规范本身可以转化为可执行的验证

### SDD vs 传统开发

| 特性 | 传统开发 | SDD |
|------|----------|-----|
| 规范状态 | 静态文档 | 可执行文件 |
| 规范位置 | 项目开始后遗忘 | 持续驱动开发 |
| 验证方式 | 人工检查 | 自动化验证 |
| 变更追踪 | 版本控制文档 | 规范版本与代码版本同步 |

### Delta Specs（差异规范）

用于追踪需求变更：

```
ADDED: 新增的需求
MODIFIED: 修改的需求
REMOVED: 删除的需求
```

---

## 支持的 AI 代理

### 完整支持列表（35+）

| AI 代理 | 类型 |
|---------|------|
| Claude Code | CLI |
| GitHub Copilot | IDE 扩展 |
| Cursor | IDE |
| Gemini CLI | CLI |
| Codex CLI | CLI |
| Windsurf | IDE |
| Codebuddy CLI | CLI |
| Pi Coding Agent | Agent |
| Aider | CLI |
| Continue | IDE 扩展 |
| Sourcegraph Cody | IDE 扩展 |
| Amazon Q Developer | IDE 扩展 |
| Google AI Studio | Web |
| OpenAI ChatGPT | Web |
| Anthropic Claude Web | Web |
| JetBrains AI | IDE 扩展 |
| VS Code GitHub Copilot Chat | IDE 扩展 |
| Zed AI | IDE |
| Replit AI | Web |
| Tabnine | IDE 扩展 |
| Mintlify | Documentation |
| Warp AI | Terminal |
| Raycast AI | Desktop |
| ... | ... |

---

## 社区扩展

### 扩展生态系统（50+）

Spec Kit 支持丰富的社区扩展，涵盖：

- **领域扩展**: 特定业务领域的规范模板
- **语言扩展**: 特定编程语言的实现指南
- **框架扩展**: 特定框架的最佳实践
- **工具扩展**: 特定开发工具的集成

### 扩展结构

每个扩展包含：
- `commands/` - 斜杠命令定义
- `templates/` - 规范模板
- `scripts/` - 自动化脚本

### 使用扩展

扩展会在项目初始化时根据选择自动集成。

---

## 项目文件结构

### 初始化后的目录结构

```
<PROJECT_NAME>/
├── .specify/
│   ├── commands/
│   │   ├── analyze.md
│   │   ├── checklist.md
│   │   ├── clarify.md
│   │   ├── constitution.md
│   │   ├── implement.md
│   │   ├── plan.md
│   │   ├── specify.md
│   │   ├── tasks.md
│   │   └── taskstoissues.md
│   ├── scripts/
│   │   ├── *.sh          # Bash 脚本
│   │   └── *.ps1         # PowerShell 脚本
│   ├── templates/
│   │   └── constitution/
│   │   └── specify/
│   └── config.json       # 配置文件
├── specs/
│   └── 001-feature-name/
│       ├── specification.md
│       ├── plan.md
│       └── tasks.md
└── .claude/
    └── commands/
        └── speckit.constitution.md
        └── speckit.specify.md
        └── speckit.clarify.md
        └── speckit.plan.md
        └── speckit.tasks.md
        └── speckit.implement.md
        └── speckit.analyze.md
        └── speckit.checklist.md
```

---

## 常见问题解答

### Q: 如何切换不同的功能规范？

A: 切换 Git 分支即可。Spec Kit 通过分支名（如 `001-feature-name`）自动检测活动规范。

### Q: 规范文件存储在哪里？

A: 在 `specs/<feature-number>-<feature-name>/` 目录下，包含 `specification.md`、`plan.md` 和 `tasks.md`。

### Q: 支持哪些脚本类型？

A: 同时支持 Bash (.sh) 和 PowerShell (.ps1)。默认按操作系统自动选择，可通过 `--script sh|ps` 强制指定。

### Q: 如何在企业隔离环境使用？

A: 使用离线安装模式，先在联网机器构建 wheel 包，传输后在隔离机器安装。

### Q: 版本稳定性如何保证？

A: 建议使用特定发布标签（如 `@vX.Y.Z`）而非主分支，以获得稳定版本。

---

## 核心原则总结

1. **明确描述** 要构建的内容及其原因
2. **规范阶段** 不关注技术栈
3. **迭代细化** 规范在实现前充分细化
4. **验证优先** 编码前验证计划
5. **代理执行** 让 AI 代理处理实现细节

---

## 相关资源

- **GitHub 仓库**: https://github.com/github/spec-kit
- **完整方法论**: https://github.com/github/spec-kit/blob/main/spec-driven.md
- **模板示例**: https://github.com/github/spec-kit/tree/main/templates
- **发布版本**: https://github.com/github/spec-kit/releases

---

## 总结

GitHub Spec Kit 是一个强大的规范驱动开发工具包，通过 6 步流程将需求转化为高质量代码：

1. **Constitution** → 建立项目原则
2. **Specify** → 定义功能需求
3. **Clarify** → 消除歧义
4. **Plan** → 技术架构设计
5. **Tasks** → 任务分解
6. **Implement** → 执行实现

配合丰富的社区扩展和多 AI 代理支持，Spec Kit 为现代软件开发提供了一套完整的规范化解决方案。