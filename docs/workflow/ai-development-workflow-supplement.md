# AI 全生命周期软件开发流程配置补充方案

> 创建日期：2026-04-14
> 版本：Supplement 1.0
> 用途：补充 `full-workflow-setup.md`，提供更多配置范式和工具选择

---

## 一、引言

本文档补充 `full-workflow-setup.md` 中未覆盖的 AI 全生命周期软件开发流程配置方式，包括：

- IDE Rules-Driven 模式
- 自主 Agent 执行模式
- MCP 集成生态模式
- Memory Bank 模式
- 多代理编排框架（LangGraph/AutoGen/CrewAI）
- Spec-Driven Development 进阶实践

---

## 二、AI 编程助手平台全景

### 2.1 平台分类矩阵

| 分类 | 平台 | 核心定位 | 工作流特色 |
|------|------|----------|------------|
| **CLI Agent** | Claude Code | 终端全栈 Agent | Skills/Agents/Hooks/MCP |
| **CLI Agent** | OpenAI Codex CLI | 终端自主执行 | Git 工作流 + 自动提交 |
| **CLI Agent** | Gemini CLI | Google 生态集成 | Android/Web 开发 |
| **CLI Agent** | OpenCode | 多模型 CLI | 灵活模型切换 |
| **IDE 编辑器** | Cursor | VS Code 衍生 | Rules for AI + Codebase Context |
| **IDE 编辑器** | Windsurf | Codeium 出品 | **Flows System** + Cascade |
| **IDE 插件** | Continue.dev | 开源 IDE 插件 | Rules + Prompt Files + Slash Commands |
| **IDE 插件** | Cline/Roo Code | VS Code 自主 Agent | 工具执行 + 自主任务 |
| **IDE 插件** | Sourcegraph Cody | 企业级上下文 | 代码搜索 + AI 问答 |
| **云端平台** | Factory AI | 企业自主 Agent | 多 Agent 协作 + CI/CD |
| **云端平台** | Kiro (AWS) | AWS IDE | 规格驱动 + AWS 集成 |
| **云端平台** | Devin (Cognition) | 自主工程师 | 端到端任务完成 |

### 2.2 选择决策树

```
                    你的需求是什么？
                          │
          ┌───────────────┼───────────────┐
          │               │               │
      快速编码        端到端任务        企业合规
          │               │               │
          ▼               ▼               ▼
    ┌─────────┐     ┌─────────┐     ┌─────────┐
    │ Cursor  │     │ Claude  │     │ Factory │
    │Windsurf │     │  Code   │     │   AI    │
    │Continue │     │  Cline  │     │ Spec Kit│
    └─────────┘     └─────────┘     └─────────┘
          │               │               │
          ▼               ▼               ▼
      IDE 内嵌       CLI 终端        云端托管
      实时建议       自主执行        企业流程
```

---

## 三、配置范式详解

### 3.1 IDE Rules-Driven 模式

#### 3.1.1 概念说明

通过规则文件（`.cursorrules`、`.continuerules` 等）指导 AI 行为，实现项目级定制。

#### 3.1.2 工具对比

| 工具 | 规则文件 | 上下文机制 | 特色功能 |
|------|----------|------------|----------|
| **Cursor** | `.cursorrules` | Codebase Indexing | Tab 自动补全、Cmd+K 编辑 |
| **Windsurf** | `.windsurf/rules` | **Cascade Engine** | Flows 多模式、深度上下文 |
| **Continue** | `.continue/rules.yaml` | Context Providers | Prompt Files、Quick Actions |

#### 3.1.3 Cursor Rules 配置示例

**文件位置**：项目根目录 `.cursorrules`

```markdown
# 项目规则

## 技术栈
- Backend: Java 17, Spring Boot 3.x, MyBatis
- Frontend: Vue 3, Element Plus, Vite
- Database: MySQL 8.0

## 编码规范
- 使用 TypeScript strict 模式
- 所有 API 需要 JWT 认证
- 测试覆盖率需 >80%
- 使用 Jakarta EE 9+ 命名空间 (jakarta.*)

## 架构原则
- RESTful API 设计
- 统一 Result<T> 响应包装
- PageHelper 分页插件在查询前调用

## 禁止行为
- 不要使用 javax.* 命名空间
- 不要在测试中 mock 数据库
- 不要跳过 pre-commit hooks
```

#### 3.1.4 Windsurf Flows System

```
Flows 工作模式：

┌─────────────────────────────────────────────────────┐
│                    Cascade Engine                    │
│              (深度代码库上下文理解)                   │
└─────────────────────┬───────────────────────────────┘
                      │
         ┌────────────┼────────────┐
         │            │            │
    ┌────▼────┐  ┌────▼────┐  ┌────▼────┐
    │ Command │  │  Write  │  │  Chat   │
    │  Mode   │  │  Mode   │  │  Mode   │
    │         │  │         │  │         │
    │ 指令驱动│  │ 协作编码│  │ 对话问答│
    └─────────┘  └─────────┘  └─────────┘

特色：
- 自动上下文感知（基于当前工作）
- 多文件编辑联动
- 依赖关系追踪
- 编码风格学习
```

#### 3.1.5 Continue.dev 配置结构

```
项目配置结构：

.continue/
├── config.json              # 主配置
├── rules.yaml               # 规则定义
└── prompts/                 # Prompt 文件
│   ├── review.prompt        # 代码审查模板
│   ├── test.prompt          # 测试生成模板
│   └── refactor.prompt      # 重构模板
│
.prompts/                    # 自定义 Prompts
├── api-design.prompt        # API 设计
└── bug-fix.prompt           # Bug 修复

Slash Commands 示例：
/review     → 代码审查
/test       → 生成测试
/refactor   → 重构建议
/docs       → 生成文档
```

#### 3.1.6 适用场景

| 场景 | 推荐工具 | 原因 |
|------|----------|------|
| 个人开发者 | Cursor | 轻量、快速、无需复杂配置 |
| 团队协作 | Continue | 开源、可定制、团队共享规则 |
| 深度项目上下文 | Windsurf | Cascade Engine 深度理解 |
| 隐私优先 | Continue + 本地模型 | 完全自主控制 |

---

### 3.2 自主 Agent 执行模式

#### 3.2.1 概念说明

Agent 自主规划和执行多步骤任务，无需持续人工干预。

#### 3.2.2 平台对比

| 平台 | 自主程度 | 执行范围 | 特色 |
|------|----------|----------|------|
| **Cline** | 中高 | IDE 内工具执行 | VS Code 插件、浏览器控制 |
| **Roo Code** | 高 | Cline Fork 增强 | 更多自主能力、API 集成 |
| **Factory AI** | 最高 | 端到端功能 | Planning→Execution→Review |
| **Devin** | 最高 | 全栈工程师 | 独立完成复杂项目 |
| **Claude Code Agent** | 高 | CLI 终端 | Skills、Hooks、MCP 集成 |

#### 3.2.3 Cline/Roo Code 工作流程

```
Cline 自主执行流程：

用户：实现一个用户管理模块，包含 CRUD 和分页

Cline：
┌─────────────────────────────────────────────────────┐
│ 1. Planning Phase                                   │
│    - 分析项目结构                                    │
│    - 理解现有架构（Spring Boot + MyBatis）          │
│    - 设计模块结构                                    │
└─────────────────────┬───────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────┐
│ 2. Execution Phase                                  │
│    - 创建 Entity: User.java                         │
│    - 创建 Mapper: UserMapper.java + XML            │
│    - 创建 Service: UserService.java                 │
│    - 创建 Controller: UserController.java           │
│    - 创建 DTO: UserDTO, UserCreateDTO              │
│    - 执行: mvn compile                              │
└─────────────────────┬───────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────┐
│ 3. Verification Phase                               │
│    - 运行测试                                        │
│    - 检查编译错误                                    │
│    - 启动应用验证                                    │
│    - 修复发现的问题                                  │
└─────────────────────┬───────────────────────────────┘
                      │
                      ▼
              完成报告 + 代码变更
```

#### 3.2.4 Factory AI 三阶段架构

```
Factory AI Self-driving Development：

┌─────────────────────────────────────────────────────┐
│                Planning Agent                        │
│   ┌─────────────────────────────────────────────┐   │
│   │ • 任务需求分析                               │   │
│   │ • 代码库搜索理解                             │   │
│   │ • 依赖关系梳理                               │   │
│   │ • 实施计划生成                               │   │
│   │ • 步骤分解排序                               │   │
│   └─────────────────────────────────────────────┘   │
└─────────────────────┬───────────────────────────────┘
                      │ 计划传递
                      ▼
┌─────────────────────────────────────────────────────┐
│                Execution Agent                       │
│   ┌─────────────────────────────────────────────┐   │
│   │ • 按计划编写代码                             │   │
│   │ • 跨文件同步编辑                             │   │
│   │ • 数据库迁移脚本                             │   │
│   │ • 配置文件更新                               │   │
│   │ • API 文档生成                               │   │
│   └─────────────────────────────────────────────┘   │
└─────────────────────┬───────────────────────────────┘
                      │ 代码传递
                      ▼
┌─────────────────────────────────────────────────────┐
│                Review Agent                          │
│   ┌─────────────────────────────────────────────┐   │
│   │ • 代码质量审查                               │   │
│   │ • 安全漏洞扫描（OWASP）                      │   │
│   │ • 测试生成执行                               │   │
│   │ • 性能基准检查                               │   │
│   │ • 反馈修复循环                               │   │
│   └─────────────────────────────────────────────┘   │
└─────────────────────┬───────────────────────────────┘
                      │
                      ▼
          输出：完整功能 + 测试 + 文档

关键特性：
- Self-healing Code：自动检测修复 bug
- Pattern Learning：学习团队编码模式
- CI/CD Integration：与现有管道集成
```

#### 3.2.5 适用场景

| 场景 | 推荐平台 | 原因 |
|------|----------|------|
| IDE 内自主任务 | Cline/Roo Code | 无需离开开发环境 |
| 企业级功能开发 | Factory AI | 端到端自主 + 企业流程 |
| 复杂独立项目 | Devin | 全栈工程师能力 |
| CLI 终端工作流 | Claude Code | Skills/Hooks/MCP 灵活组合 |

---

### 3.3 MCP 集成生态模式

#### 3.3.1 MCP 协议说明

**Model Context Protocol (MCP)** 是 Anthropic 于 2024 年 11 月推出的开放标准，用于连接 AI 助手和数据源/工具。

类比：**MCP之于 AI Agent，如同 USB-C 之于外设** —— 标准化连接接口。

#### 3.3.2 生态规模

```
MCP 生态系统：

┌─────────────────────────────────────────────────────┐
│                  行业支持                            │
│   Anthropic | Microsoft | Google | OpenAI          │
└─────────────────────────────────────────────────────┘

官方服务器：
├── GitHub      → 仓库操作、PR/Issue 管理
├── Slack       → 消息发送、频道管理
├── Postgres    → 数据库查询、表操作
├── Google Drive → 文件读写、搜索
├── Filesystem  → 本地文件系统操作
├── Memory      → 持久化记忆存储
├── Puppeteer   → 浏览器自动化
└── ...更多

社区服务器（1000+）：
├── Smithery Registry → 服务器发现和安装
├── Awesome MCP Servers → 分类精选列表
├── 企业定制服务器 → 内部系统集成
└── ...
```

#### 3.3.3 配置示例

**settings.json MCP 配置**：

```json
{
  "mcpServers": {
    "github": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-github"],
      "env": {
        "GITHUB_TOKEN": "${GITHUB_TOKEN}"
      }
    },
    "postgres": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-postgres"],
      "env": {
        "POSTGRES_CONNECTION_STRING": "postgresql://user:pass@localhost:5432/mydb"
      }
    },
    "filesystem": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-filesystem", "/path/to/project"]
    },
    "memory": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-memory"]
    },
    "slack": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-slack"],
      "env": {
        "SLACK_BOT_TOKEN": "${SLACK_BOT_TOKEN}"
      }
    }
  }
}
```

#### 3.3.4 工作流集成示例

```
MCP 集成工作流示例：

任务：创建 PR 并通知团队

Step 1: Claude Code 使用 GitHub MCP
├── mcp__github__create_branch
├── mcp__github__create_pull_request
└── mcp__github__add_reviewers

Step 2: Claude Code 使用 Slack MCP
├── mcp__slack__post_message
└── 自动通知相关频道

Step 3: Claude Code 使用 Memory MCP
├── 存储本次变更的模式
└── 更新项目记忆

完整流程自动化，无需人工介入
```

#### 3.3.5 Smithery Registry 使用

```bash
# 搜索 MCP 服务器
npx smithery search postgres

# 安装 MCP 服务器
npx smithery install @modelcontextprotocol/server-github

# 查看已安装服务器
npx smithery list
```

#### 3.3.6 适用场景

| 场景 | MCP 服务器组合 | 价值 |
|------|----------------|------|
| DevOps 自动化 | GitHub + Postgres + Kubernetes MCP | CI/CD 全流程自动化 |
| 团队协作 | GitHub + Slack + Google Drive MCP | 代码变更自动通知 |
| 数据库开发 | Postgres + Memory MCP | 查询 + 模式记忆 |
| 企业集成 | 内部定制 MCP 服务器 | ERP/CRM/API 集成 |

---

### 3.4 Memory Bank 模式

#### 3.4.1 概念说明

结构化记忆系统，跨会话保持项目上下文和关键决策。

#### 3.4.2 记忆类型分层

```
记忆分层架构：

┌─────────────────────────────────────────────────────┐
│              Long-term Memory (持久记忆)             │
│   ┌─────────────────────────────────────────────┐   │
│   │ • 项目核心需求                               │   │
│   │ • 架构决策记录                               │   │
│   │ • 用户编码偏好                               │   │
│   │ • 已学习的模式                               │   │
│   └─────────────────────────────────────────────┘   │
├─────────────────────────────────────────────────────┤
│              Working Memory (工作记忆)               │
│   ┌─────────────────────────────────────────────┐   │
│   │ • 当前任务上下文                             │   │
│   │ • 最近代码变更                               │   │
│   │ • 活动会话状态                               │   │
│   └─────────────────────────────────────────────┘   │
├─────────────────────────────────────────────────────┤
│              Short-term Memory (瞬时记忆)            │
│   ┌─────────────────────────────────────────────┐   │
│   │ • 当前对话内容                               │   │
│   │ • 即时问题回答                               │   │
│   │ • 临时计算结果                               │   │
│   └─────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────┘
```

#### 3.4.3 Memory Bank 文件结构

```
memory-bank/
├── projectbrief.md          # 项目核心需求和目标
│   内容：项目目的、关键功能、成功标准
│
├── productContext.md        # 产品上下文和特性
│   内容：用户画像、使用场景、产品范围
│
├── systemPatterns.md        # 系统架构和模式
│   内容：架构图、设计模式、技术决策
│
├── techContext.md           # 技术栈和环境配置
│   内容：框架版本、依赖、开发环境
│
├── activeContext.md         # 当前工作焦点
│   内容：正在做什么、下一步计划、阻塞问题
│
└── progress.md              # 实现进度追踪
│   内容：已完成、进行中、待办事项
```

#### 3.4.4 各文件内容模板

**projectbrief.md**：
```markdown
# Project Brief

## Purpose
构建一个全栈用户管理系统，支持 JWT 认证和 RBAC 权限控制。

## Core Goals
- 用户 CRUD 操作
- 安全的 JWT 认证
- 分页和搜索功能
- 80%+ 测试覆盖率

## Success Criteria
- 所有 API 通过 Swagger 测试
- 安全审计无高危漏洞
- 页面加载 < 3s
```

**systemPatterns.md**：
```markdown
# System Patterns

## Architecture Style
RESTful API + SPA 前端

## Key Design Patterns
- Repository Pattern (MyBatis Mapper)
- DTO Pattern (请求/响应分离)
- Interceptor Pattern (JWT 认证)
- AOP Pattern (日志、限流)

## Code Structure
com.example.demo/
├── controller/    # REST 接口
├── service/       # 业务逻辑
├── mapper/        # 数据访问
├── entity/        # 数据实体
├── dto/           # 数据传输对象
├── config/        # 配置类
└── util/          # 工具类
```

**activeContext.md**：
```markdown
# Active Context

## Current Focus
正在实现用户导出功能（Excel 导出）

## Recent Decisions
- 使用 Apache POI 生成 Excel
- 导出上限 10000 条记录
- 异步生成避免阻塞

## Next Steps
1. 实现 ExportService
2. 添加导出 API
3. 前端导出按钮
4. 测试导出功能

## Blockers
- 需要确认导出字段列表
```

#### 3.4.5 工作流程

```
Memory Bank 工作流程：

Session Start:
┌─────────────────────────────────────────────────────┐
│ 1. 加载 memory-bank/                                │
│    - 读取 projectbrief.md 理解项目目标              │
│    - 读取 systemPatterns.md 理解架构                │
│    - 读取 activeContext.md 知道当前进度              │
│    - 读取 progress.md 了解完成情况                   │
└─────────────────────┬───────────────────────────────┘
                      │
                      ▼
                开始工作（已理解上下文）

Session Work:
┌─────────────────────────────────────────────────────┐
│ 持续更新 activeContext.md                           │
│    - 记录关键决策                                    │
│    - 更新当前进度                                    │
│    - 标记阻塞问题                                    │
└─────────────────────┬───────────────────────────────┘
                      │
                      ▼
Session End:
┌─────────────────────────────────────────────────────┐
│ 保存关键信息到 memory-bank/                         │
│    - 更新 progress.md                               │
│    - 重要决策写入 systemPatterns.md                  │
│    - 学习模式提取                                    │
└─────────────────────────────────────────────────────┘
```

#### 3.4.6 技术实现方案

| 方案 | 优点 | 缺点 | 适用场景 |
|------|------|------|----------|
| **Markdown 文件** | 轻量、可版本控制、人可读 | 无语义检索 | 小型项目 |
| **JSON/YAML** | 结构化、易解析 | 不直观 | 自动化流程 |
| **向量数据库** | 语义检索、模糊搜索 | 需要基础设施 | 大型项目 |
| **知识图谱** | 实体关系、复杂推理 | 实现复杂 | 企业级 |
| **Mem0** | 开源、多 LLM 支持 | 需要部署 | 通用方案 |

#### 3.4.7 与 Claude Code Memory 系统整合

```
Claude Code Memory + Memory Bank 整合：

~/.claude/projects/<hash>/memory/
├── MEMORY.md              # Claude Code 系统记忆
└── 指向项目 memory-bank/

项目根目录:
memory-bank/
├── projectbrief.md
├── systemPatterns.md
├── ...

Claude Code 加载时：
→ 读取 ~/.claude/projects/<hash>/memory/MEMORY.md
→ 读取 项目 memory-bank/
→ 合并上下文，开始工作
```

---

### 3.5 多代理编排框架

#### 3.5.1 概念说明

使用专业框架编排多个 AI Agent 协作完成复杂任务。

#### 3.5.2 框架对比

| 框架 | 开发者 | 核心机制 | 特色 |
|------|--------|----------|------|
| **LangGraph** | LangChain | 状态机 + 图编排 | 精细控制、可观测性 |
| **AutoGen** | Microsoft | 多 Agent 对话 | 灵活交互、代码执行 |
| **CrewAI** | CrewAI | 角色 + 任务流程 | 角色扮演、顺序/并行 |
| **Claude Code Team** | Anthropic | TeamCreate + TaskUpdate | 内置集成、CLI原生 |
| **Agent Swarm** | OpenAI | 专业化分工 | Codex 集成 |

#### 3.5.3 LangGraph 状态机模型

```python
# LangGraph 状态机示例：代码审查流程

from langgraph.graph import StateGraph, END

# 定义状态
class CodeReviewState(TypedDict):
    code: str
    review_comments: list
    fixes: list
    approved: bool

# 定义节点（Agent）
def planning_node(state):
    """规划审查重点"""
    return {"review_comments": ["检查安全", "检查性能", "检查风格"]}

def security_review_node(state):
    """安全审查"""
    issues = check_security(state["code"])
    return {"review_comments": state["review_comments"] + issues}

def style_review_node(state):
    """代码风格审查"""
    issues = check_style(state["code"])
    return {"review_comments": state["review_comments"] + issues}

def fix_node(state):
    """自动修复"""
    fixes = auto_fix(state["review_comments"])
    return {"fixes": fixes, "approved": len(fixes) == 0}

# 构建图
graph = StateGraph(CodeReviewState)
graph.add_node("planning", planning_node)
graph.add_node("security_review", security_review_node)
graph.add_node("style_review", style_review_node)
graph.add_node("fix", fix_node)

# 定义边（流程）
graph.add_edge("planning", "security_review")
graph.add_edge("security_review", "style_review")
graph.add_edge("style_review", "fix")
graph.add_conditional_edge("fix", lambda s: "approved" if s["approved"] else "security_review")

# 运行
app = graph.compile()
result = app.invoke({"code": source_code})
```

**LangGraph 流程图**：
```
┌─────────────┐
│  planning   │
│   node      │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  security   │
│  review     │──────┐
└──────┬──────┘      │
       │             │ (修复后重新审查)
       ▼             │
┌─────────────┐      │
│   style     │      │
│  review     │      │
└──────┬──────┘      │
       │             │
       ▼             │
┌─────────────┐      │
│    fix      │◄─────┘
│   node      │
└──────┬──────┘
       │
    approved?
       │
       ▼
     [END]
```

#### 3.5.4 AutoGen 多 Agent 对话

```python
# AutoGen 示例：功能开发团队

from autogen import AssistantAgent, UserProxyAgent

# 创建专业化 Agent
planner = AssistantAgent(
    name="Planner",
    system_message="你负责规划功能实现步骤，输出详细计划。",
    llm_config={"model": "gpt-4"}
)

developer = AssistantAgent(
    name="Developer",
    system_message="你负责编写代码，按计划实现功能。",
    llm_config={"model": "gpt-4"}
)

reviewer = AssistantAgent(
    name="Reviewer",
    system_message="你负责审查代码，检查安全和质量。",
    llm_config={"model": "gpt-4"}
)

tester = AssistantAgent(
    name="Tester",
    system_message="你负责编写和运行测试。",
    llm_config={"model": "gpt-4"}
)

user_proxy = UserProxyAgent(
    name="User",
    human_input_mode="TERMINATE",
    code_execution_config={"work_dir": "./workspace"}
)

# 创建团队对话
groupchat = GroupChat(
    agents=[planner, developer, reviewer, tester, user_proxy],
    messages=[],
    max_round=50
)

manager = GroupChatManager(groupchat=groupchat)

# 启动任务
user_proxy.initiate_chat(
    manager,
    message="实现用户注册功能，包含邮箱验证和密码加密"
)
```

**AutoGen 对话流程**：
```
User: 实现用户注册功能

Planner: 规划步骤...
        1. 设计 User Entity
        2. 创建注册 API
        3. 实现邮箱验证服务
        4. 添加密码加密
        5. 编写测试

Developer: 按计划编写代码...
        [生成 User.java, RegisterService.java, ...]

Reviewer: 审查代码...
        发现：密码未使用 BCrypt
        建议：使用 BCryptPasswordEncoder

Developer: 修复问题...
        [更新密码加密方式]

Tester: 编写测试...
        [生成 RegisterTest.java]
        运行结果：全部通过

User: 确认完成
```

#### 3.5.5 CrewAI 角色任务流

```python
# CrewAI 示例：开发团队

from crewai import Agent, Task, Crew, Process

# 定义角色
architect = Agent(
    role="Software Architect",
    goal="设计系统架构和技术方案",
    backstory="资深架构师，擅长 Java/Spring 架构设计",
    allow_delegation=False
)

developer = Agent(
    role="Software Developer",
    goal="实现功能代码",
    backstory="高级 Java 开发者，熟悉 Spring Boot",
    allow_delegation=True  # 可委托测试任务
)

qa_engineer = Agent(
    role="QA Engineer",
    goal="测试和验证代码质量",
    backstory="测试工程师，专注于自动化测试",
    allow_delegation=False
)

# 定义任务
design_task = Task(
    description="设计用户认证系统架构",
    agent=architect,
    expected_output="架构设计文档，包含类图和流程图"
)

implement_task = Task(
    description="实现 JWT 认证功能",
    agent=developer,
    context=[design_task],  # 依赖设计任务
    expected_output="完整代码实现"
)

test_task = Task(
    description="编写认证系统测试",
    agent=qa_engineer,
    context=[implement_task],  # 依赖实现任务
    expected_output="测试代码和测试报告"
)

# 创建团队
crew = Crew(
    agents=[architect, developer, qa_engineer],
    tasks=[design_task, implement_task, test_task],
    process=Process.sequential  # 顺序执行
    # 或 Process.hierarchical  # 层级执行（经理模式）
)

# 启动执行
result = crew.kickoff()
```

**CrewAI 任务流**：
```
顺序执行模式：

┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  Architect  │───►│  Developer  │───►│    QA       │
│   Agent     │    │   Agent     │    │  Engineer   │
└─────────────┘    └─────────────┘    └─────────────┘
       │                  │                  │
       ▼                  ▼                  ▼
   设计文档          代码实现           测试报告

并行执行模式：

┌─────────────┐
│   Manager   │ (协调者)
│   Agent     │
└──────┬──────┘
       │
       ├─────────────┬─────────────┐
       │             │             │
┌──────▼──────┐ ┌────▼────┐ ┌──────▼──────┐
│  Frontend   │ │ Backend │ │   Tester    │
│  Developer  │ │Developer│ │   Agent     │
└─────────────┘ └─────────┘ └─────────────┘
       │             │             │
       ▼             ▼             ▼
   Vue 组件      REST API      测试代码
       │             │             │
       └─────────────┴─────────────┘
                     │
                     ▼
              Manager 整合
```

#### 3.5.6 Claude Code Team 模式

```python
# Claude Code 内置团队模式

# 创建团队
TeamCreate(
    team_name="dev-team",
    agent_type="general-purpose",
    description="开发团队"
)

# 创建任务
TaskCreate(subject="设计 API", description="设计用户 CRUD API")
TaskCreate(subject="实现后端", description="实现 Spring Boot Controller")
TaskCreate(subject="编写测试", description="编写单元测试和集成测试")
TaskCreate(subject="前端集成", description="Vue 页面对接 API")

# 启动 Agent 成员
Agent(subagent_type="general-purpose", name="api-designer", team_name="dev-team")
Agent(subagent_type="general-purpose", name="backend-dev", team_name="dev-team")
Agent(subagent_type="general-purpose", name="test-dev", team_name="dev-team")
Agent(subagent_type="general-purpose", name="frontend-dev", team_name="dev-team")

# 分配任务
TaskUpdate(taskId="1", owner="api-designer")
TaskUpdate(taskId="2", owner="backend-dev")
TaskUpdate(taskId="3", owner="test-dev")
TaskUpdate(taskId="4", owner="frontend-dev")

# 协调消息
SendMessage(to="api-designer", message="开始设计 API")
SendMessage(to="backend-dev", message="等待 API 设计完成后开始实现")

# 完成后关闭
TeamDelete()
```

#### 3.5.7 框架选择指南

| 场景 | 推荐框架 | 原因 |
|------|----------|------|
| **精细流程控制** | LangGraph | 状态机精确控制每个步骤 |
| **灵活对话协作** | AutoGen | 多 Agent 自然对话交互 |
| **角色分工明确** | CrewAI | 角色扮演、任务清晰分配 |
| **CLI 原生集成** | Claude Code Team | 无需额外框架，内置支持 |
| **并行快速开发** | CrewAI (hierarchical) | Manager 模式并行协调 |

---

### 3.6 Spec-Driven Development 进阶实践

#### 3.6.1 OpenSpec vs Spec Kit 深度对比

| 维度 | OpenSpec | Spec Kit |
|------|----------|----------|
| **哲学** | 流体迭代、无门槛 | 瀑布阶段、完整流程 |
| **工作流** | `/opsx:propose → apply → archive` | 6阶段刚性门槛 |
| **Delta Specs** | ADDED/MODIFIED/REMOVED | 同样支持 |
| **适用项目** | 现有代码库、敏捷迭代 | 新项目、企业合规 |
| **AI 工具** | 25+ 平台支持 | 35+ Agent 支持 |
| **离线支持** | 无 | 企业隔离环境支持 |

#### 3.6.2 Delta Specs 最佳实践

```markdown
# Delta for User Authentication

## ADDED Requirements

### Requirement: JWT Token Issuance
系统必须（MUST）在用户登录成功后颁发 JWT Token。

#### Scenario: Valid credentials
- GIVEN 用户输入正确的用户名和密码
- WHEN 提交登录表单
- THEN 返回有效 JWT Token
- AND Token 包含用户 ID 和角色信息
- AND Token 有效期为 24 小时

#### Scenario: Invalid credentials
- GIVEN 用户输入错误的密码
- WHEN 提交登录表单
- THEN 返回 401 错误
- AND 不颁发 Token

### Requirement: Token Validation
系统必须（MUST）验证所有 /api/users/* 端点的 Token。

#### Scenario: Valid token
- GIVEN 请求包含有效 Authorization header
- WHEN 调用 API
- THEN 允许访问
- AND 设置 UserContext

#### Scenario: Expired token
- GIVEN Token 已过期
- WHEN 调用 API
- THEN 返回 401 错误
- AND 提示重新登录

## MODIFIED Requirements

### Requirement: Rate Limiting
限制改为：每个 IP 每分钟最多 10 次登录尝试。
（之前：每分钟 5 次）

## REMOVED Requirements

### Requirement: Session-based Auth
移除 Session 认证方式，统一使用 JWT。
```

#### 3.6.3 Archive 合并规则

```
Archive 时 Delta Specs 合并逻辑：

主 Spec (specs/auth/spec.md):
┌─────────────────────────────────────────────────────┐
│ 原有内容                                            │
└─────────────────────────────────────────────────────┘

Delta Spec (changes/add-jwt/specs/auth/spec.md):
┌─────────────────────────────────────────────────────┐
│ ADDED: JWT Token Issuance                          │
│ ADDED: Token Validation                            │
│ MODIFIED: Rate Limiting (10次/分钟)                │
│ REMOVED: Session-based Auth                        │
└─────────────────────────────────────────────────────┘

合并后主 Spec:
┌─────────────────────────────────────────────────────┐
│ + JWT Token Issuance                               │
│ + Token Validation                                 │
│ Rate Limiting: 10次/分钟 (已更新)                   │
│ - Session-based Auth (已删除)                       │
└─────────────────────────────────────────────────────┘
```

---

## 四、完整配置策略

### 4.1 开发者类型匹配

| 开发者类型 | 推荐最小配置 | 推荐标准配置 | 推荐完整配置 |
|------------|--------------|--------------|--------------|
| **个人开发者** | Cursor + `.cursorrules` | Cursor + OpenSpec + GStack | Claude Code + MCP + Memory Bank |
| **小型团队** | Continue + 团队 Rules | Continue + OpenSpec + Superpowers | Claude Code Team + Spec Kit |
| **敏捷团队** | Windsurf + Flows | Windsurf + OpenSpec + GStack | Factory AI + MCP 集成 |
| **企业合规** | Spec Kit 基础 | Spec Kit + Superpowers + ECC | Factory AI + LangGraph + MCP |
| **DevOps/SRE** | Claude Code + Hooks | Claude Code + MCP + Hooks | Claude Code Team + MCP 全集成 |

### 4.2 项目类型匹配

| 项目类型 | 规格工具 | 执行工具 | 审查工具 | 集成工具 |
|----------|----------|----------|----------|----------|
| **新项目** | Spec Kit | Claude Code | Superpowers TDD | GitHub MCP |
| **现有代码库** | OpenSpec | Cursor/Windsurf | ECC Reviewers | Filesystem MCP |
| **微服务重构** | OpenSpec + Design | LangGraph | Factory AI | Postgres MCP |
| **前端 SPA** | OpenSpec | Windsurf | Superpowers | Memory MCP |
| **后端 API** | Spec Kit | Claude Code | ECC Java | GitHub MCP |

### 4.3 一键配置脚本补充

```bash
#!/bin/bash
# workflow-supplement-setup.sh
# 补充配置方案

echo "=== AI 工作流补充配置 ==="

# 方案 1: IDE Rules-Driven 模式
setup_ide_rules() {
    echo "配置 IDE Rules 模式..."
    
    # Cursor Rules
    cat > .cursorrules << 'EOF'
# 项目规则
## 技术栈: Java 17, Spring Boot 3.x, Vue 3
## 测试覆盖率: >80%
## API 认证: JWT required
EOF
    
    # Continue Rules
    mkdir -p .continue
    cat > .continue/rules.yaml << 'EOF'
rules:
  - name: java-patterns
    condition: "**/*.java"
    instructions: "使用 Jakarta EE 9+ 命名空间"
  - name: vue-patterns
    condition: "ui/**/*.vue"
    instructions: "使用 Composition API"
EOF
    
    echo "IDE Rules 配置完成"
}

# 方案 2: Memory Bank 模式
setup_memory_bank() {
    echo "配置 Memory Bank..."
    
    mkdir -p memory-bank
    
    cat > memory-bank/projectbrief.md << 'EOF'
# Project Brief
## Purpose
全栈用户管理系统，JWT 认证 + RBAC
EOF
    
    cat > memory-bank/systemPatterns.md << 'EOF'
# System Patterns
## Architecture
RESTful API + SPA
EOF
    
    cat > memory-bank/activeContext.md << 'EOF'
# Active Context
## Current Focus
初始化项目
EOF
    
    cat > memory-bank/techContext.md << 'EOF'
# Tech Context
- Java 17, Spring Boot 3.2.0
- Vue 3, Element Plus, Vite
- MySQL 8.0, Redis
EOF
    
    cat > memory-bank/progress.md << 'EOF'
# Progress
## Completed
- 项目初始化
EOF
    
    echo "Memory Bank 配置完成"
}

# 方案 3: MCP 集成
setup_mcp() {
    echo "配置 MCP 集成..."
    
    # 添加 MCP 服务器配置到 settings.json
    cat > ~/.claude/mcp-additions.json << 'EOF'
{
  "mcpServers": {
    "github": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-github"]
    },
    "postgres": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-postgres"]
    },
    "memory": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-memory"]
    }
  }
}
EOF
    
    echo "MCP 配置已生成，请手动合并到 settings.json"
}

# 方案 4: LangGraph 简化配置
setup_langgraph() {
    echo "配置 LangGraph 工作流..."
    
    mkdir -p workflows
    
    cat > workflows/code-review-graph.py << 'EOF'
"""LangGraph 代码审查流程"""
from langgraph.graph import StateGraph, END
from typing import TypedDict

class ReviewState(TypedDict):
    code: str
    issues: list
    fixes: list
    approved: bool

def security_check(state): ...
def style_check(state): ...
def auto_fix(state): ...

graph = StateGraph(ReviewState)
graph.add_node("security", security_check)
graph.add_node("style", style_check)
graph.add_node("fix", auto_fix)
EOF
    
    echo "LangGraph 工作流模板已创建"
}

# 主菜单
echo "请选择配置方案："
echo "1) IDE Rules-Driven 模式"
echo "2) Memory Bank 模式"
echo "3) MCP 集成模式"
echo "4) LangGraph 工作流"
echo "5) 全部配置"
read -p "选择: " choice

case $choice in
    1) setup_ide_rules ;;
    2) setup_memory_bank ;;
    3) setup_mcp ;;
    4) setup_langgraph ;;
    5) setup_ide_rules && setup_memory_bank && setup_mcp && setup_langgraph ;;
    *) echo "无效选择" ;;
esac

echo "=== 配置完成 ==="
```

---

## 五、整合建议

### 5.1 与现有文档整合

| 现有文档 | 补充整合点 |
|----------|------------|
| `full-workflow-setup.md` | 添加 IDE Rules 模式、Memory Bank、LangGraph/AutoGen/CrewAI |
| `claude-code-usage.md` | 添加 MCP 进阶配置、Team 模式详解 |
| `openspec-usage.md` | 添加 Delta Specs 最佳实践 |
| `gstack-usage.md` | 添加 MCP 替代手动操作的方案 |
| `superpowers-marketplace-usage.md` | 添加与 LangGraph 审查流程整合 |
| `everything-claude-code-usage.md` | 添加 Memory Bank 与 ECC Memory 整合 |
| `hermes-agent-usage.md` | 添加 MCP 通信服务器替代方案 |

### 5.2 配置优先级

```
配置优先级（从高到低）：

1. 【必配】项目规则文件
   └── .cursorrules / .continuerules / CLAUDE.md
   
2. 【推荐】Memory Bank
   └── memory-bank/ 目录
   
3. 【推荐】MCP 集成
   └── GitHub MCP + Filesystem MCP
   
4. 【按需】Spec 工具
   └── OpenSpec (敏捷) / Spec Kit (合规)
   
5. 【按需】审查工具
   └── GStack / Superpowers / ECC
   
6. 【高级】多代理编排
   └── LangGraph / AutoGen / CrewAI
   
7. 【企业】云端平台
   └── Factory AI / Devin
```

---

## 六、参考资源汇总

### 6.1 官方文档

| 资源 | 链接 |
|------|------|
| Cursor Rules | https://docs.cursor.com/context/rules-for-ai |
| Windsurf Flows | https://codeium.com/windsurf |
| Continue.dev | https://docs.continue.dev |
| MCP Protocol | https://www.anthropic.com/news/model-context-protocol |
| MCP Servers | https://github.com/modelcontextprotocol/servers |
| Smithery Registry | https://smithery.ai/ |
| LangGraph | https://langchain-ai.github.io/langgraph/ |
| AutoGen | https://microsoft.github.io/autogen/ |
| CrewAI | https://docs.crewai.com/ |
| Factory AI | https://www.factory.ai/ |

### 6.2 社区资源

| 资源 | 链接 |
|------|------|
| Awesome Cursor Rules | https://github.com/awesome-cursor-rules/awesome-cursor-rules |
| Awesome MCP Servers | https://github.com/punkpeye/awesome-mcp-servers |
| Memory Bank Templates | https://github.com/context-hub/memory-bank |
| LangGraph Examples | https://github.com/langchain-ai/langgraph/tree/main/examples |

---

## 七、总结

### 7.1 配置范式全景

```
AI 全生命周期软件开发流程配置全景：

┌─────────────────────────────────────────────────────┐
│                  配置范式总览                        │
├─────────────────────────────────────────────────────┤
│                                                     │
│  ┌─────────────────┐    ┌─────────────────┐        │
│  │ IDE Rules-Driven│    │ 自主 Agent 执行 │        │
│  │ (轻量快速)      │    │ (端到端自主)    │        │
│  └─────────────────┘    └─────────────────┘        │
│                                                     │
│  ┌─────────────────┐    ┌─────────────────┐        │
│  │ MCP 集成生态    │    │ Memory Bank     │        │
│  │ (标准化连接)    │    │ (跨会话记忆)    │        │
│  └─────────────────┘    └─────────────────┘        │
│                                                     │
│  ┌─────────────────┐    ┌─────────────────┐        │
│  │ 多代理编排框架  │    │ Spec-Driven     │        │
│  │ (LangGraph等)   │    │ (规范驱动)      │        │
│  └─────────────────┘    └─────────────────┘        │
│                                                     │
└─────────────────────────────────────────────────────┘

选择原则：
- 个人开发者：轻量优先（IDE Rules + Memory Bank）
- 团队协作：规范优先（Spec-Driven + MCP）
- 企业合规：完整优先（Factory AI + LangGraph）
- DevOps：集成优先（MCP + Hooks + Memory Bank）
```

### 7.2 核心价值

| 范式 | 核心价值 |
|------|----------|
| **IDE Rules-Driven** | 轻量配置、即时生效、团队共享 |
| **自主 Agent 执行** | 减少人工干预、端到端完成、自动化程度高 |
| **MCP 集成生态** | 标准化接口、丰富生态、企业集成 |
| **Memory Bank** | 跨会话上下文、持续学习、项目记忆 |
| **多代理编排** | 专业分工、并行执行、复杂任务 |
| **Spec-Driven** | 人机对齐、规范先行、可追溯性 |

---

*文档补充 `full-workflow-setup.md`，提供更多配置选择和实践指南*