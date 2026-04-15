# Prompt Engineering 完全指南

> 创建日期：2026-04-15
> 版本：Prompt Engineering 1.0
> 适用：AI 编程助手、LLM 应用开发

---

## 一、概述

### 1.1 什么是 Prompt Engineering

**Prompt Engineering（提示工程）** 是设计和优化 AI 模型输入提示的技术实践，目的是引导模型产生高质量、准确、有用的输出。

```
Prompt Engineering 定义：

┌─────────────────────────────────────────────────────────────┐
│                    Prompt Engineering                        │
│                                                             │
│  输入：精心设计的提示（Prompt）                              │
│  处理：LLM 模型理解并生成响应                                │
│  输出：高质量、符合预期的结果                                │
│                                                             │
│  核心目标：                                                  │
│  ├── 让 AI 理解你的意图                                     │
│  ├── 让 AI 遵循特定格式                                     │
│  ├── 让 AI 执行复杂任务                                     │
│  ├── 让 AI 提供准确信息                                     │
│  └── 让 AI 避免错误输出                                     │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 1.2 为什么重要

| 问题 | Prompt Engineering 解决方案 |
|------|----------------------------|
| **AI 理解偏差** | 明确角色和上下文，减少误解 |
| **输出格式混乱** | 指定格式模板，结构化输出 |
| **任务执行失败** | 分步引导，提供示例 |
| **信息不准确** | 提供参考资料，要求验证 |
| **安全风险** | 添加约束和边界 |

### 1.3 Prompt Engineering vs Context Engineering vs Harness Engineering

| 工程类型 | 范围 | 核心目标 | 侧重点 |
|----------|------|----------|--------|
| **Prompt Engineering** | 单次交互 | 设计单个提示获得理想输出 | 提示措辞、格式、示例 |
| **Context Engineering** | 多次交互 | 结构化上下文保持一致性 | artifact、记忆、文档 |
| **Harness Engineering** | 系统层面 | 构建可靠的 AI 执行系统 | 流程、验证、安全、监控 |

---

## 二、Prompt Engineering 基础原则

### 2.1 六大核心原则

```
Prompt Engineering 六大原则：

┌─────────────────────────────────────────────────────────────┐
│                                                             │
│  1. CLEAR (清晰)                                            │
│     ├── 使用简单明了的语言                                  │
│     ├── 避免歧义和模糊表述                                  │
│     └── 一个提示只做一件事                                  │
│                                                             │
│  2. SPECIFIC (具体)                                         │
│     ├── 明确任务目标                                        │
│     ├── 指定输出格式                                        │
│     ├── 提供具体约束                                        │
│     └── 给出数量限制                                        │
│                                                             │
│  3. CONTEXT (上下文)                                        │
│     ├── 提供背景信息                                        │
│     ├── 说明为什么                                          │
│     ├── 提供相关文件                                        │
│     └── 解释项目环境                                        │
│                                                             │
│  4. ROLE (角色)                                             │
│     ├── 定义 AI 角色                                        │
│     ├── 设定专业领域                                        │
│     ├── 指定行为风格                                        │
│     └── 明确责任边界                                        │
│                                                             │
│  5. EXAMPLE (示例)                                          │
│     ├── 提供成功示例                                        │
│     ├── 展示期望输出                                        │
│     ├── 对比好坏示例                                        │
│     └── Few-shot 学习                                       │
│                                                             │
│  6. ITERATE (迭代)                                          │
│     ├── 测试并优化                                          │
│     ├── 收集反馈                                            │
│     ├── 版本化管理                                          │
│     └── 模式提取                                            │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 CLEAR 原则详解

**差示例**：
```
帮我写一个函数
```

**好示例**：
```
写一个 TypeScript 函数 `formatDate`，接收 Date 对象，返回 "YYYY-MM-DD" 格式的字符串。使用toISOString()方法，提取日期部分。
```

| 维度 | 差提示 | 好提示 |
|------|--------|--------|
| 语言 | 模糊 | 明确 |
| 歧义 | 多种理解方式 | 单一理解 |
| 任务 | 多任务混合 | 单一任务 |

### 2.3 SPECIFIC 原则详解

**差示例**：
```
列出一些 API 设计建议
```

**好示例**：
```
列出 5 条 RESTful API 设计最佳实践，每条不超过 50 字，格式：
1. [建议标题]：[具体内容]
```

| 维度 | 差提示 | 好提示 |
|------|--------|--------|
| 目标 | 不明确 | 明确（5条） |
| 格式 | 无 | 有模板 |
| 约束 | 无 | 有字数限制 |

### 2.4 CONTEXT 原则详解

**差示例**：
```
修复这个 bug
```

**好示例**：
```
修复 UserController.java:145 的 bug：
错误信息：NullPointerException at line 145
背景：用户登录后，userContext 未初始化
相关文件：UserController.java, UserContext.java, AuthService.java
期望：添加 null 检查，抛出 BusinessException
```

| 维度 | 差提示 | 好提示 |
|------|--------|--------|
| 背景 | 无 | 有错误信息、背景 |
| 原因 | 无 | 解释了问题来源 |
| 相关 | 无 | 提供相关文件 |
| 期望 | 无 | 明确修复方案 |

### 2.5 ROLE 原则详解

**差示例**：
```
分析这段代码
```

**好示例**：
```
你是一位资深 Java 安全工程师，专注于 OWASP 安全漏洞分析。
分析以下代码的安全风险：
- 检查 SQL 注入风险
- 检查 XSS 风险
- 检查认证漏洞
- 检查敏感数据暴露
输出格式：漏洞名称 | 风险等级 | 修复建议
```

| 维度 | 差提示 | 好提示 |
|------|--------|--------|
| 角色 | 无 | 安全工程师 |
| 领域 | 无 | OWASP 安全 |
| 行为 | 无 | 指定检查项 |
| 格式 | 无 | 表格格式 |

### 2.6 EXAMPLE 原则详解（Few-shot Learning）

**零-shot（无示例）**：
```
将以下句子翻译成 JSON 格式：
"用户名为 admin，密码为 123456"
```

**Few-shot（有示例）**：
```
将句子翻译成 JSON 格式：

示例：
输入："名称为商品A，价格为100元"
输出：{"name": "商品A", "price": 100}

示例：
输入："标题是新闻，作者是张三"
输出：{"title": "新闻", "author": "张三"}

现在翻译：
输入："用户名为 admin，密码为 123456"
输出：
```

### 2.7 ITERATE 原则详解

```
Prompt 迭代流程：

┌─────────────────────────────────────────────────────────────┐
│                                                             │
│  Version 1: 初始提示                                       │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ "写一个登录功能"                                     │   │
│  └─────────────────────────────────────────────────────┘   │
│                          ↓                                  │
│                      测试输出                               │
│                          ↓                                  │
│                      问题分析                               │
│                          ↓                                  │
│  Version 2: 优化提示                                       │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ "写一个 Spring Boot 登录 API，使用 JWT 认证，        │   │
│  │  接收 username 和 password，返回 token 或错误信息"   │   │
│  └─────────────────────────────────────────────────────┘   │
│                          ↓                                  │
│                      测试输出                               │
│                          ↓                                  │
│                      问题分析                               │
│                          ↓                                  │
│  Version 3: 最终版本                                       │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ "你是一位 Spring Boot 安全专家。                      │   │
│  │  写一个登录 API 端点：                                 │   │
│  │  - 路径: POST /api/auth/login                        │   │
│  │  - 请求体: {"username": "xxx", "password": "xxx"}   │   │
│  │  - 验证: BCrypt 密码验证                              │   │
│  │  - 响应: 成功返回 JWT token，失败返回 401            │   │
│  │  - 使用 @PostMapping, @RequestBody                  │   │
│  │  - 添加 @RateLimit 注解                              │   │
│  │  参考项目中 AuthService.java 的模式"                  │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 三、Prompt 结构模式

### 3.1 标准结构模板

```
标准 Prompt 结构：

┌─────────────────────────────────────────────────────────────┐
│                                                             │
│  1. ROLE（角色设定）                                        │
│     你是一位 [角色]，专注于 [领域]                           │
│                                                             │
│  2. CONTEXT（上下文）                                       │
│     背景：[项目背景、问题来源]                               │
│     相关：[相关文件、代码、文档]                             │
│                                                             │
│  3. TASK（任务）                                            │
│     请 [动词] [对象]                                        │
│     目标：[期望结果]                                         │
│                                                             │
│  4. CONSTRAINTS（约束）                                     │
│     必须：[必须遵守的规则]                                   │
│     禁止：[禁止的行为]                                       │
│     格式：[输出格式要求]                                     │
│                                                             │
│  5. EXAMPLES（示例）                                        │
│     示例：[成功示例]                                         │
│                                                             │
│  6. OUTPUT（输出要求）                                      │
│     输出：[具体输出格式]                                     │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 3.2 完整示例

```markdown
# ROLE
你是一位资深 Java/Spring Boot 架构师，专注于 RESTful API 设计和安全最佳实践。

# CONTEXT
项目：JArsenal 用户管理系统
技术栈：Spring Boot 3.2.0, Java 17, MyBatis, JWT
现有代码：
- AuthService.java 处理认证逻辑
- JwtUtil.java 生成和验证 token
- UserController.java 用户 CRUD API
- RateLimitAspect.java 限流切面

# TASK
设计一个用户注册 API 端点，包括：
1. 接收用户注册信息（用户名、密码、邮箱）
2. 验证输入（用户名唯一性、密码强度、邮箱格式）
3. 创建用户记录
4. 返回成功或错误响应

# CONSTRAINTS
必须：
- 使用 Jakarta EE 9+ 命名空间（jakarta.*）
- 密码使用 BCrypt 加密
- 输入验证使用 Jakarta Validation
- 响应使用 Result<T> 包装器
- 添加 @RateLimit 注解（每 IP 每分钟 5 次）

禁止：
- 不使用 javax.* 命名空间
- 不返回明文密码
- 不硬编码错误消息

格式：
- 代码文件完整路径
- 每个方法添加简要注释

# EXAMPLES
参考现有登录 API 结构：

```java
@PostMapping("/login")
@RateLimit(key = "login", time = 60, count = 5, limitType = LimitType.IP)
public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    // ...
}
```

# OUTPUT
输出：
1. RegisterController.java - 控制器代码
2. RegisterRequest.java - DTO 代码
3. RegisterService.java - 服务代码（如果需要新增）
4. API 文档说明
```

### 3.3 简化结构（适合日常使用）

对于日常简单任务，使用简化模板：

```
[角色]：[角色描述]
[任务]：[具体任务]
[约束]：[关键约束]
[输出]：[输出格式]
```

**示例**：
```
[角色]：Java 开发专家
[任务]：修复 UserController.java 的 NullPointerException
[约束]：保持现有 API 签名不变
[输出]：修复后的代码 + 问题说明
```

---

## 四、高级 Prompt 技术

### 4.1 Chain-of-Thought（思维链）

引导 AI 展示推理过程，提高复杂问题解决质量。

```
Chain-of-Thought 示例：

┌─────────────────────────────────────────────────────────────┐
│                                                             │
│  任务：设计一个 RBAC 权限系统                                │
│                                                             │
│  Prompt：                                                   │
│  "设计一个 RBAC 权限系统。请按以下步骤思考：                 │
│                                                             │
│  步骤 1：分析需求                                           │
│  - 需要哪些角色？                                           │
│  - 需要哪些权限？                                           │
│  - 权限粒度是什么？                                         │
│                                                             │
│  步骤 2：设计数据模型                                       │
│  - 角色表结构                                               │
│  - 权限表结构                                               │
│  - 关联表结构                                               │
│                                                             │
│  步骤 3：设计 API                                           │
│  - 权限检查 API                                             │
│  - 角色管理 API                                             │
│  - 权限分配 API                                             │
│                                                             │
│  步骤 4：实现安全机制                                       │
│  - 如何防止权限绕过？                                       │
│  - 如何处理权限变更？                                       │
│                                                             │
│  请展示你的思考过程，然后给出最终设计。"                     │
│                                                             │
│  效果：                                                     │
│  - AI 会按步骤思考                                          │
│  - 输出更有逻辑性                                           │
│  - 减少遗漏和错误                                           │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 4.2 Self-Consistency（自一致性）

生成多个解决方案，选择最佳或合并。

```
Self-Consistency 示例：

Prompt：
"为用户认证系统设计三个不同的方案：
方案 A：纯 JWT 无状态方案
方案 B：JWT + Redis 会话方案
方案 C：Session + Redis 方案

对每个方案：
1. 描述实现细节
2. 分析优点
3. 分析缺点
4. 评估适用场景

最后，比较三个方案，推荐最适合本项目（Spring Boot + Vue SPA + 分布式部署）的方案，并说明原因。"
```

### 4.3 Tree-of-Thought（思维树）

探索多个分支，评估和选择最佳路径。

```
Tree-of-Thought 示例：

Prompt：
"解决性能瓶颈问题。按以下思维树探索：

        性能瓶颈
           │
     ┌─────┼─────┐
     │     │     │
  数据库  网络  代码
     │     │     │
  ┌──┼──┐ │  ┌──┼──┐
索引 查询│ │ 算法 IO
     │     │
  评估每个分支：
  - 可能原因？
  - 检查方法？
  - 解决方案？
  - 成本评估？

选择最可能的分支，给出诊断和修复方案。"
```

### 4.4 Prompt Chaining（提示链）

将复杂任务分解为多个提示，顺序执行。

```
Prompt Chaining 示例：

┌─────────────────────────────────────────────────────────────┐
│                    Prompt Chain                              │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Prompt 1: 需求分析                                         │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ 分析用户需求文档，提取关键功能和约束                  │   │
│  │ 输出：功能列表 + 约束列表                              │   │
│  └─────────────────────────────────────────────────────┘   │
│                          ↓                                  │
│  Prompt 2: 架构设计                                         │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ 基于功能列表设计系统架构                              │   │
│  │ 输入：功能列表 + 约束列表                              │   │
│  │ 输出：架构图 + 模块划分                                │   │
│  └─────────────────────────────────────────────────────┘   │
│                          ↓                                  │
│  Prompt 3: API 设计                                         │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ 设计 RESTful API 端点                                 │   │
│  │ 输入：架构图 + 模块划分                                │   │
│  │ 输出：API 规格文档                                     │   │
│  └─────────────────────────────────────────────────────┘   │
│                          ↓                                  │
│  Prompt 4: 数据模型                                         │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ 设计数据库表结构                                       │   │
│  │ 输入：API 规格文档                                     │   │
│  │ 输出：SQL DDL                                          │   │
│  └─────────────────────────────────────────────────────┘   │
│                          ↓                                  │
│  Prompt 5: 代码实现                                         │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ 实现 Spring Boot 代码                                  │   │
│  │ 输入：API 规格 + SQL DDL                               │   │
│  │ 输出：Java 代码                                         │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  优势：                                                     │
│  - 每个提示专注单一任务                                     │
│  - 输出可验证和修正                                         │
│  - 可并行执行某些步骤                                       │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 4.5 Role Prompting（角色扮演）

设定多个角色进行协作模拟。

```
Role Prompting 示例：

Prompt：
"模拟一个开发团队讨论：

角色 1：架构师 Alex
- 关注：系统设计、可扩展性、性能
- 语气：专业、分析型

角色 2：开发工程师 Bob
- 关注：实现细节、代码质量、调试
- 语气：务实、技术型

角色 3：安全工程师 Carol
- 关注：安全漏洞、认证授权、数据保护
- 语气：谨慎、强调风险

角色 4：QA 工程师 Dave
- 关注：测试覆盖、边界情况、回归测试
- 语气：质疑、追求验证

讨论主题：是否应该使用 Redis 存储 JWT token？

请每个角色发表观点，然后给出最终决策建议。"
```

### 4.6 Negative Prompting（反向提示）

明确指出不要做什么，避免错误。

```
Negative Prompting 示例：

Prompt：
"实现一个用户查询 API。

必须做到：
- 使用 PageHelper 分页
- 返回 Result<PageResult<User>> 格式
- 添加 @RateLimit 注解

**千万不要**：
- 不要使用 SELECT * 查询（只查询必要字段）
- 不要在 Service 层调用 PageHelper（Controller 层调用）
- 不要返回明文密码字段
- 不要硬编码分页参数
- 不要忽略用户不存在的情况

如果不确定，宁可先询问，不要猜测。"
```

---

## 五、Prompt 文件化

### 5.1 Prompt 文件结构

将 Prompt 保存为文件，便于复用和管理。

```
Prompt 文件目录结构：

prompts/
├── development/
│   ├── code-review.prompt       # 代码审查 Prompt
│   ├── bug-fix.prompt           # Bug 修复 Prompt
│   ├── refactor.prompt          # 重构 Prompt
│   ├── test-generation.prompt   # 测试生成 Prompt
│   └── api-design.prompt        # API 设计 Prompt
│
├── architecture/
│   ├── system-design.prompt     # 系统设计 Prompt
│   ├── database-schema.prompt   # 数据库设计 Prompt
│   └── security-review.prompt   # 安全审查 Prompt
│
├── documentation/
│   ├── api-docs.prompt          # API 文档 Prompt
│   ├── readme.prompt            # README Prompt
│   └── changelog.prompt         # 变更日志 Prompt
│
└── templates/
    ├── base.prompt              # 基础模板
    ├── java.prompt              # Java 专用模板
    ├── spring-boot.prompt       # Spring Boot 专用模板
    └── vue.prompt               # Vue 专用模板
```

### 5.2 Prompt 文件格式

```markdown
---
name: code-review
description: Spring Boot 代码审查 Prompt
version: 2.0
tags: [java, spring-boot, review]
---

# ROLE
你是一位资深 Java/Spring Boot 代码审查专家。

# CONTEXT
项目：{project_name}
技术栈：{tech_stack}
审查范围：{scope}

# TASK
审查以下代码，重点关注：

1. 代码质量
   - 是否遵循 Java 最佳实践？
   - 是否有重复代码？
   - 命名是否清晰？

2. Spring Boot 规范
   - 是否正确使用注解？
   - 是否遵循分层架构？
   - 是否正确处理异常？

3. 安全问题
   - 是否有注入风险？
   - 是否有认证漏洞？
   - 是否暴露敏感信息？

4. 性能问题
   - 是否有 N+1 查询？
   - 是否有阻塞操作？
   - 是否缺少缓存？

# CONSTRAINTS
必须：
- 使用 Result<T> 包装响应
- 添加适当的日志
- 处理边界情况

禁止：
- 禁止硬编码配置
- 禁止忽略异常
- 禁止返回敏感数据

# OUTPUT
输出格式：

| 问题类型 | 位置 | 描述 | 严重程度 | 建议 |
|----------|------|------|----------|------|

然后提供修复代码示例。

---

# VARIABLES
{project_name} = JArsenal
{tech_stack} = Spring Boot 3.2.0, Java 17, MyBatis
{scope} = src/main/java/com/example/demo/controller/
```

### 5.3 Continue.dev Prompt 文件

```
.continue/prompts/

├── review.prompt
```markdown
---
name: review
description: Code review for Spring Boot
---

Review the selected code for:
- Security vulnerabilities (OWASP)
- Performance issues
- Spring Boot best practices
- Code style consistency

Output in table format with severity levels.
```

├── test.prompt
```markdown
---
name: test
description: Generate unit tests
---

Generate unit tests for the selected code:
- Use JUnit 5
- Use Mockito for dependencies
- Cover edge cases
- Aim for 80%+ coverage

Follow the existing test patterns in the project.
```
```

---

## 六、Prompt 版本管理

### 6.1 Prompt 版本控制

```
Prompt 版本演变：

prompts/versions/
├── code-review/
│   ├── v1.0.prompt    # 基础审查
│   ├── v1.1.prompt    # 添加安全检查
│   ├── v2.0.prompt    # 添加性能检查
│   └── v2.1.prompt    # 当前版本（Spring Boot 3.x）
│   └── CHANGELOG.md
│
└── api-design/
│   ├── v1.0.prompt    # RESTful 基础
│   ├── v2.0.prompt    # 添加 JWT 认证
│   ├── v2.1.prompt    # 当前版本
│   └── CHANGELOG.md
```

### 6.2 CHANGELOG 示例

```markdown
# Code Review Prompt CHANGELOG

## v2.1 - 2026-04-15
### Added
- Spring Boot 3.x Jakarta EE 9+ 命名空间检查
- @RateLimit 注解使用检查

### Changed
- 更新 OWASP Top 10 检查项
- 性能检查项细化

## v2.0 - 2026-04-01
### Added
- 性能问题检查（N+1、阻塞操作）
- 缓存使用检查

### Changed
- 输出格式从列表改为表格

## v1.1 - 2026-03-15
### Added
- OWASP Top 10 安全检查
- 认证授权审查

## v1.0 - 2026-03-01
### Initial
- 基础代码质量审查
- Spring Boot 注解使用检查
```

---

## 七、Prompt 测试和评估

### 7.1 Prompt 测试方法

```
Prompt 测试流程：

┌─────────────────────────────────────────────────────────────┐
│                    Prompt Testing                            │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1. 定义测试案例                                            │
│     ├── 输入案例集                                          │
│     ├── 期望输出集                                          │
│     └── 边界情况集                                          │
│                                                             │
│  2. 执行测试                                                │
│     ├── 用 Prompt 处理每个输入                              │
│     ├── 记录输出                                            │
│     └── 记录响应时间                                        │
│                                                             │
│  3. 评估输出                                                │
│     ├── 准确性评估                                          │
│     ├── 格式符合性                                          │
│     ├── 完整性检查                                          │
│     └── 安全性检查                                          │
│                                                             │
│  4. 分析失败案例                                            │
│     ├── 识别失败模式                                        │
│     ├── 分析原因                                            │
│     └── 提出改进                                            │
│                                                             │
│  5. 优化 Prompt                                             │
│     ├── 应用改进                                            │
│     ├── 重新测试                                            │
│     └── 比较结果                                            │
│                                                             │
│  6. 文档化                                                  │
│     ├── 记录有效模式                                        │
│     ├── 记录避免模式                                        │
│     └── 更新版本                                            │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 7.2 Prompt 评估指标

| 指标 | 说明 | 测量方法 |
|------|------|----------|
| **准确性** | 输出信息正确率 | 专家审查 / 自动验证 |
| **格式符合率** | 输出格式符合要求的比例 | 格式解析成功率 |
| **完整性** | 输出覆盖所有要求的比例 | 检查项覆盖率 |
| **一致性** | 多次运行结果一致的比例 | 重复运行比较 |
| **效率** | 响应时间和 token 使用 | 时间测量 / token 计数 |
| **安全性** | 输出不包含风险的比例 | 安全扫描 |

### 7.3 Prompt 测试案例示例

```markdown
# Code Review Prompt 测试案例

## Test Case 1: 基础代码审查
Input:
```java
public String getName(Long id) {
    User user = userDao.findById(id);
    return user.getName();
}
```

Expected Output:
| 问题类型 | 位置 | 描述 | 严重程度 | 建议 |
|----------|------|------|----------|------|
| 安全 | userDao.findById(id) | 缺少 null 检查，可能导致 NPE | 高 | 添加 Optional 或 null 检查 |
| 性能 | userDao.findById(id) | 单条查询，无问题 | - | - |

## Test Case 2: SQL 注入风险
Input:
```java
public List<User> search(String keyword) {
    String sql = "SELECT * FROM users WHERE name LIKE '%" + keyword + "%'";
    return jdbcTemplate.query(sql, ...);
}
```

Expected Output:
| 问题类型 | 位置 | 描述 | 严重程度 | 建议 |
|----------|------|------|----------|------|
| 安全 | sql 拼接 | SQL 注入风险 | 严重 | 使用预编译参数 #{keyword} |

## Test Case 3: Spring Boot 注解
Input:
```java
@Service
public class UserService {
    @Autowired
    UserDao userDao;
    
    public User getUser(Long id) {
        return userDao.findById(id);
    }
}
```

Expected Output:
| 问题类型 | 位置 | 描述 | 严重程度 | 建议 |
|----------|------|------|----------|------|
| 最佳实践 | @Autowired 字段注入 | 推荐使用构造器注入 | 中 | 改为 @Autowired 构造器 |
```

---

## 八、Prompt 最佳实践清单

### 8.1 编写清单

```markdown
Prompt 编写清单：

## 结构检查
- [ ] 包含角色设定
- [ ] 包含上下文说明
- [ ] 包含明确任务
- [ ] 包含约束条件
- [ ] 包含输出格式要求

## 内容检查
- [ ] 使用清晰语言
- [ ] 避免歧义表述
- [ ] 提供足够背景
- [ ] 指定具体约束
- [ ] 给出示例（如需要）

## 安全检查
- [ ] 添加禁止项
- [ ] 设置边界约束
- [ ] 指明验证方法
- [ ] 提供参考资料

## 可维护性检查
- [ ] 版本化管理
- [ ] 变量化设计
- [ ] 文档化说明
- [ ] 测试案例准备

## 优化检查
- [ ] 测试有效性
- [ ] 收集反馈
- [ ] 迭代改进
- [ ] 模式提取
```

### 8.2 避免清单

```markdown
Prompt 避免清单：

## 不要做
❌ 使用模糊语言："做一些改进"
❌ 混合多个任务："重构并优化并添加测试"
❌ 过长提示：超过必要信息量
❌ 过短提示：缺少关键上下文
❌ 依赖 AI 常识：不提供项目特定信息

## 不要假设
❌ AI 知道项目背景
❌ AI 知道编码规范
❌ AI 知道文件路径
❌ AI 知道依赖关系
❌ AI 知道安全要求

## 不要忽略
❌ 输出格式要求
❌ 错误处理说明
❌ 边界情况处理
❌ 禁止项列表
❌ 验证方法
```

---

## 九、Prompt 工具集成

### 9.1 Claude Code 集成

```
Claude Code Prompt 集成方式：

┌─────────────────────────────────────────────────────────────┐
│                                                             │
│  1. CLAUDE.md 文件                                          │
│     ┌─────────────────────────────────────────────────────┐│
│     │ 项目级指导文件                                       ││
│     │ 包含：技术栈、规范、禁止项                           ││
│     │ 自动加载到每个会话                                   ││
│     └─────────────────────────────────────────────────────┘│
│                                                             │
│  2. Skills (.claude/skills/)                                │
│     ┌─────────────────────────────────────────────────────┐│
│     │ SKILL.md 文件                                        ││
│     │ 包含：角色、任务、约束、输出                         ││
│     │ 通过 /skill-name 触发                                ││
│     └─────────────────────────────────────────────────────┘│
│                                                             │
│  3. Rules (.claude/rules/)                                  │
│     ┌─────────────────────────────────────────────────────┐│
│     │ 规则文件                                             ││
│     │ 包含：编码规范、禁止项、最佳实践                     ││
│     │ 自动应用到相关文件                                   ││
│     └─────────────────────────────────────────────────────┘│
│                                                             │
│  4. Commands (.claude/commands/)                            │
│     ┌─────────────────────────────────────────────────────┐│
│     │ 斜杠命令                                             ││
│     │ 包含：完整 Prompt 模板                               ││
│     │ 通过 /command-name 触发                              ││
│     └─────────────────────────────────────────────────────┘│
│                                                             │
│  5. Hooks (PreToolUse/PostToolUse)                          │
│     ┌─────────────────────────────────────────────────────┐│
│     │ 自动注入上下文                                       ││
│     │ 自动检查输出                                         ││
│     │ 自动格式化                                           ││
│     └─────────────────────────────────────────────────────┘│
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 9.2 Continue.dev 集成

```
.continue/
├── config.json
├── rules.yaml
└── prompts/
    ├── review.prompt
    ├── test.prompt
    └── refactor.prompt
```

### 9.3 Cursor Rules 集成

```
.cursorrules 文件：

# 项目规则（自动应用到所有交互）

## 角色
你是一位资深 Java/Spring Boot 开发专家。

## 技术栈
- Backend: Java 17, Spring Boot 3.2.0, MyBatis
- Frontend: Vue 3, Element Plus, Vite
- Database: MySQL 8.0

## 编码规范
- 使用 Jakarta EE 9+ 命名空间（jakarta.*）
- 所有 API 使用 Result<T> 包装响应
- PageHelper.startPage() 在查询前立即调用
- 密码使用 BCrypt 加密

## 禁止
- 不要使用 javax.* 命名空间
- 不要在测试中 mock 数据库
- 不要返回明文密码
- 不要硬编码配置
- 不要忽略异常
```

---

## 十、参考资源

### 10.1 学习资源

| 资源 | 链接 |
|------|------|
| OpenAI Prompt Engineering Guide | https://platform.openai.com/docs/guides/prompt-engineering |
| Anthropic Prompt Engineering | https://docs.anthropic.com/claude/docs/prompt-engineering |
| Google Prompt Engineering | https://ai.google.dev/docs/prompt_best_practices |
| Learn Prompting | https://learnprompting.org/ |
| Prompt Engineering Guide | https://www.promptingguide.ai/ |

### 10.2 模板资源

| 资源 | 链接 |
|------|------|
| Awesome ChatGPT Prompts | https://github.com/f/awesome-chatgpt-prompts |
| Prompt Engineering Templates | https://github.com/snorkelai/prompt-engineering-templates |
| Continue.dev Prompts | https://github.com/continuedev/continue/tree/main/prompts |

---

## 十一、总结

### 11.1 Prompt Engineering 核心价值

```
┌─────────────────────────────────────────────────────────────┐
│                  Prompt Engineering 价值                     │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1. 提高准确性                                              │
│     明确指令 → 减少误解 → 正确输出                          │
│                                                             │
│  2. 提高一致性                                              │
│     标准模板 → 稳定输出 → 可预测结果                        │
│                                                             │
│  3. 提高效率                                                │
│     一次成功 → 减少迭代 → 节省时间                          │
│                                                             │
│  4. 提高安全性                                              │
│     明确边界 → 避免风险 → 安全输出                          │
│                                                             │
│  5. 提高可维护性                                            │
│     版本管理 → 文档化 → 团队协作                            │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 11.2 与其他 Engineering 的关系

```
三大 Engineering 关系：

┌─────────────────────────────────────────────────────────────┐
│                                                             │
│   Prompt Engineering                                        │
│   ┌─────────────────────────────────────────────────────┐   │
│   │ 单次交互优化                                         │   │
│   │ 如何设计一个提示获得理想输出                         │   │
│   └─────────────────────────────────────────────────────┘   │
│                          ↓                                  │
│                    输出质量稳定后                            │
│                          ↓                                  │
│   Context Engineering                                       │
│   ┌─────────────────────────────────────────────────────┐   │
│   │ 多次交互一致性                                       │   │
│   │ 如何结构化上下文保持跨会话一致                       │   │
│   └─────────────────────────────────────────────────────┘   │
│                          ↓                                  │
│                    系统化后                                  │
│                          ↓                                  │
│   Harness Engineering                                       │
│   ┌─────────────────────────────────────────────────────┐   │
│   │ 系统可靠性                                           │   │
│   │ 如何构建可靠的 AI 执行系统                           │   │
│   └─────────────────────────────────────────────────────┘   │
│                                                             │
│   关系：                                                    │
│   Prompt Engineering 是基础                                 │
│   Context Engineering 是扩展                                │
│   Harness Engineering 是系统化                              │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

*Prompt Engineering 完全指南 - AI 编程助手核心技术*