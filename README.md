# JArsenal - 企业级 DDD 架构示例

Java Core Arsenal Collections - 基于 DDD 四层架构的企业级 Spring Boot 示例项目

## 项目简介

基于 **DDD (Domain-Driven Design) 四层架构** 的企业级 REST API 示例，采用聚合根、值对象、领域事件等 DDD 核心概念，结合 CQRS、Outbox Pattern 等架构模式。

### 架构亮点

- **富血模型**: 聚合根封装业务行为，告别贫血模型
- **依赖倒置**: 领域层定义 Repository 接口，基础设施层实现
- **事件驱动**: 领域事件 + Outbox Pattern 保证可靠性
- **CQRS**: 读写分离，UserCommandController / UserQueryController
- **值对象自验证**: 业务规则内聚于值对象构造函数

## 技术栈

### 后端

| 技术 | 版本 | 说明 |
|-----|------|------|
| Spring Boot | 3.2.0 | 基础框架 |
| Java | 17 | 运行环境 |
| MyBatis | 3.0.3 | ORM 框架 |
| PageHelper | 2.1.0 | 分页插件 |
| MySQL | 8.x | 数据库 |
| JWT (jjwt) | 0.12.3 | Token 认证 |
| Redis | - | 分布式限流 |
| Kafka | - | 领域事件消息队列 |
| Springdoc | 2.3.0 | OpenAPI 文档 |
| Lombok | - | 简化代码 |

### 前端

| 技术 | 版本 | 说明 |
|-----|------|------|
| Vue | 3.4.x | 前端框架 |
| Element Plus | 2.4.x | UI 组件库 |
| Vite | 5.x | 构建工具 |
| Pinia | 2.x | 状态管理 |
| Vue Router | 4.x | 路由管理 |
| Axios | 1.x | HTTP 请求 |

**注意**: Spring Boot 3.x 使用 Jakarta EE 9+，所有 `javax.*` 包名改为 `jakarta.*`

## 项目结构 (DDD 四层架构)

```
├── src/main/java/com/example/demo/
│   ├── domain/                        # 领域层 (核心)
│   │   ├── shared/                    # 共享内核
│   │   │   ├── event/                 # 领域事件基类
│   │   │   ├── exception/             # 领域异常
│   │   │   └── common/                # 基础实体类
│   │   ├── user/                      # 用户上下文
│   │   │   ├── aggregate/             # 聚合根 (User)
│   │   │   ├── valueobject/           # 值对象 (UserId, Username, Email...)
│   │   │   ├── repository/            # Repository 接口
│   │   │   ├── service/               # 领域服务
│   │   │   └── event/                 # 领域事件
│   │   ├── auth/                      # 认证上下文
│   │   │   ├── aggregate/             # 聚合根 (Session)
│   │   │   ├── entity/                # 实体 (RefreshToken)
│   │   │   ├── valueobject/           # 值对象
│   │   │   ├── repository/            # Repository 接口
│   │   │   └── event/                 # 领域事件
│   │   └── audit/                     # 审计上下文
│   │       ├── aggregate/             # 聚合根 (AuditLog)
│   │       └── valueobject/           # 值对象
│   │
│   ├── application/                   # 应用层
│   │   ├── service/                   # 应用服务
│   │   ├── command/                   # 命令对象
│   │   └── event/                     # 事件处理器
│   │
│   ├── infrastructure/                # 基础设施层
│   │   ├── persistence/               # 持久化
│   │   │   ├── mapper/                # MyBatis Mapper
│   │   │   ├── repository/            # Repository 实现
│   │   │   ├── converter/             # PO ↔ Domain 转换
│   │   │   └── po/                    # 持久化对象
│   │   ├── outbox/                    # Outbox Pattern
│   │   ├── security/                  # 安全组件
│   │   └── config/                    # 配置类
│   │
│   ├── interfaces/                    # 接口层
│   │   ├── controller/                # REST 控制器 (CQRS 分离)
│   │   ├── dto/                       # 数据传输对象
│   │   └── assembler/                 # DTO 转换器
│   │
│   └── [legacy]                       # 保留的通用组件
│       ├── annotation/                # 自定义注解
│       ├── aspect/                    # AOP 切面
│       ├── common/                    # 公共类
│       ├── exception/                 # 异常处理
│       ├── interceptor/               # 拦截器
│       └── monitor/                   # 监控指标
│
└── ui/                                # 前端代码
    └── src/
        ├── api/                       # API 调用封装
        ├── components/                # 公共组件
        ├── router/                    # 路由配置
        ├── stores/                    # Pinia 状态管理
        └── views/                     # 页面组件
```

## 快速开始

### 1. 环境要求
- JDK 17+
- Maven 3.6+
- MySQL 8.x
- Redis
- Kafka (领域事件)
- Node.js 18+ (前端)

### 2. 数据库配置
```bash
# 创建数据库并导入测试数据
mysql -h localhost -P 3306 -u root -proot demo < src/main/resources/schema.sql

# 创建 Outbox 表 (事件驱动)
mysql -h localhost -P 3306 -u root -proot demo < src/main/resources/schema_outbox.sql
```

### 3. 启动基础设施服务
```bash
# 启动 Kafka (macOS Homebrew)
brew services start kafka

# 启动 Redis
brew services start redis

# 启动 MySQL
brew services start mysql
```

### 4. 启动后端
```bash
# 编译
mvn clean compile

# 运行
mvn spring-boot:run

# 打包
mvn clean package -DskipTests

# 运行测试
mvn test
```

### 5. 启动前端
```bash
cd ui

# 安装依赖
npm install

# 开发模式
npm run dev

# 生产构建
npm run build
```

### 6. 访问地址

| 地址 | 说明 |
|------|------|
| http://localhost:3000 | 前端界面 |
| http://localhost:8080/swagger-ui.html | Swagger API 文档 |
| http://localhost:8080/actuator/health | 健康检查 |

## API 接口

### 认证模块 `/api/auth`

| 方法 | 路径 | 说明 | 认证 |
|-----|------|------|------|
| POST | `/api/auth/login` | 用户登录，返回 JWT Token | 无需 |
| POST | `/api/auth/register` | 用户注册 | 无需 |
| POST | `/api/auth/refresh` | 刷新 Token | 无需 |
| POST | `/api/auth/logout` | 用户登出 | 无需 |

### 用户模块 `/api/users` (需要 JWT Token)

| 方法 | 路径 | 说明 | 认证 |
|-----|------|------|------|
| GET | `/api/users` | 分页查询用户列表 | Bearer Token |
| GET | `/api/users/{id}` | 查询单个用户 | Bearer Token |
| POST | `/api/users` | 创建用户 | Bearer Token |
| PUT | `/api/users/{id}` | 更新用户 | Bearer Token |
| DELETE | `/api/users/{id}` | 删除用户 | Bearer Token |

### 接口测试示例

```bash
# 用户登录 (密码: 123456)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"张三","password":"123456"}'

# 用户注册
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"newuser","password":"123456","email":"user@example.com"}'

# 查询用户列表 (需要 Token)
curl "http://localhost:8080/api/users?pageNum=1&pageSize=5" \
  -H "Authorization: Bearer <token>"

# 条件查询
curl "http://localhost:8080/api/users?pageNum=1&pageSize=5&username=张&status=1" \
  -H "Authorization: Bearer <token>"
```

### 响应格式
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [...],
    "total": 15,
    "pageNum": 1,
    "pageSize": 5
  }
}
```

## DDD 架构设计

### 聚合根业务行为

#### User Aggregate
```java
// 工厂方法
User.register(username, email, password)    // 注册新用户
User.rebuild(id, username, ...)             // 从数据库重建

// 业务行为
user.updateProfile(username, email)         // 更新资料
user.enable()                               // 启用账号
user.disable()                              // 禁用账号
user.validatePassword(encoder, rawPwd)      // 验证密码
user.validateCanLogin()                     // 检查可登录状态
```

#### Session Aggregate
```java
Session.create(userId, username, generator) // 创建会话
session.refresh(generator)                   // 刷新 Token
session.logout()                             // 登出
```

### 领域事件

| 事件 | 触发点 | 说明 |
|-----|-------|-----|
| `UserRegistered` | `User.register()` | 用户注册成功 |
| `UserLoggedIn` | `Session.create()` | 用户登录成功 |
| `UserLoggedOut` | `Session.logout()` | 用户登出 |
| `TokenRefreshed` | `Session.refresh()` | Token 刷新 |

### 事件驱动架构

```
业务操作 → 领域事件 → Outbox表 → Kafka → 审计日志
           (同一事务)  (定时发布)  (消费)
```

## 企业级配置

### 已集成功能

| 功能 | 说明 |
|-----|------|
| JWT 认证 | Token 无状态认证，BCrypt 密码加密 |
| Redis 限流 | `@RateLimit` 注解，滑动窗口算法 |
| 全局异常处理 | 统一异常响应，参数校验异常捕获 |
| 请求日志拦截器 | 记录请求耗时、IP、traceId |
| 审计日志 | 基于 Kafka 的异步审计日志 |
| Outbox Pattern | 保证事件发布与业务操作的原子性 |
| Swagger 文档 | OpenAPI 3.0 规范 |
| Actuator 监控 | 健康检查、性能指标 |

### 监控端点

| 地址 | 说明 |
|-----|------|
| `/swagger-ui.html` | Swagger UI 文档 |
| `/api-docs` | OpenAPI JSON |
| `/actuator/health` | 健康检查 |
| `/actuator/metrics` | 性能指标 |

## 测试

```bash
# 运行所有测试
mvn test

# 运行单个测试类
mvn test -Dtest=UserTest

# 测试覆盖
mvn jacoco:report
```

### 测试覆盖

| 测试类 | 测试数 | 覆盖范围 |
|--------|-------|---------|
| `UserTest` | 10 | User 聚合根行为 |
| `SessionTest` | 4 | Session 聚合根行为 |
| `UserIdTest` | 7 | 值对象验证 |
| `UsernameTest` | 4 | 值对象验证 |
| `EmailTest` | 4 | 值对象验证 |

## License

MIT License