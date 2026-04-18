# JGuard - 企业级 DDD 权限管理系统

Java Guard - 基于 DDD 四层架构的企业级 RBAC 权限管理系统

## 项目简介

基于 **DDD (Domain-Driven Design) 四层架构** 的企业级权限管理系统，完整实现了 RBAC 权限模型、数据维度权限、字段级权限控制、审计日志等企业级功能。

### 架构亮点

- **DDD 四层架构**: 领域层、应用层、基础设施层、接口层严格分离
- **富血模型**: 聚合根封装业务行为，告别贫血模型
- **依赖倒置**: 领域层定义 Repository 接口，基础设施层实现
- **CQRS**: 读写分离，Command/Query Controller 独立
- **事件驱动**: 领域事件 + Outbox Pattern 保证可靠性
- **RBAC + 数据权限**: 功能权限 + 数据维度权限 + 字段权限

### 功能模块

| 模块 | 功能 |
|------|------|
| **认证** | 登录、注册、Token 刷新、登出、双 Token 机制 |
| **用户管理** | 用户 CRUD、状态管理、角色分配 |
| **角色管理** | 角色 CRUD、角色继承、权限分配 |
| **资源管理** | 菜单/操作/API 三级资源树、路径模式匹配 |
| **权限管理** | 角色权限、字段权限、数据维度权限 |
| **审计日志** | 基于 Kafka 的异步审计、权限变更追踪 |

## 技术栈

### 后端

| 技术 | 版本 | 说明 |
|-----|------|------|
| Spring Boot | 3.2.0 | 基础框架 (Jakarta EE 9+) |
| Java | 17 | LTS 版本 |
| MyBatis | 3.0.3 | ORM 框架 |
| PageHelper | 2.1.0 | 分页插件 |
| MySQL | 8.x | 数据库 |
| Flyway | 9.22.3 | 数据库迁移 |
| JWT (jjwt) | 0.12.3 | 双 Token 认证 |
| BCrypt | 12 | 密码加密 |
| Redis | 7.x | 限流 + 权限缓存 |
| Redisson | 3.27.2 | 分布式锁 |
| Kafka | 7.6.0 | 领域事件消息队列 |
| Caffeine | - | 本地缓存 |
| Springdoc | 2.3.0 | OpenAPI 3.0 文档 |

### 前端

| 技术 | 版本 | 说明 |
|-----|------|------|
| Vue | 3.4.x | Composition API |
| Element Plus | 2.4.x | UI 组件库 |
| Vite | 5.x | 构建工具 |
| Pinia | 2.x | 状态管理 |
| Vue Router | 4.x | 路由 + 权限守卫 |
| Axios | 1.x | HTTP + Token 自动刷新 |

## 项目结构 (DDD 四层架构)

```
src/main/java/com/jguard/
├── domain/                           # 领域层 (核心业务逻辑)
│   ├── shared/                       # 共享内核
│   │   ├── event/                    # 领域事件基类
│   │   ├── exception/                # 领域异常
│   │   └── common/                   # 基础实体类
│   ├── user/                         # 用户上下文
│   │   ├── aggregate/                # 聚合根 (User)
│   │   ├── valueobject/              # 值对象 (UserId, Username, Email...)
│   │   ├── repository/               # Repository 接口
│   │   ├── service/                  # 领域服务
│   │   └── event/                    # 领域事件
│   ├── auth/                         # 认证上下文
│   │   ├── aggregate/                # 聚合根 (Session)
│   │   ├── entity/                   # 实体 (RefreshToken)
│   │   ├── valueobject/              # 值对象 (TokenPair, AccessToken...)
│   │   ├── repository/               # Repository 接口
│   │   └── event/                    # 领域事件
│   ├── audit/                        # 审计上下文
│   │   ├── aggregate/                # 聚合根 (AuditLog)
│   │   └── valueobject/              # 值对象 (OperationType, ModuleType...)
│   └── permission/                   # 权限上下文
│       ├── aggregate/                # 聚合根 (Role, Resource, DataDimension)
│       ├── entity/                   # 实体 (Permission, FieldPermission...)
│       ├── valueobject/              # 值对象 (ActionType, PermissionBitmap...)
│       ├── repository/               # Repository 接口
│       └── service/                  # 领域服务
│
├── application/                      # 应用层 (用例协调)
│   ├── service/                      # 应用服务
│   ├── command/                      # 命令对象
│   ├── dto/                          # 数据传输对象
│   └── event/                        # 事件处理器
│
├── infrastructure/                   # 基础设施层 (技术实现)
│   ├── annotation/                   # 自定义注解 (@RateLimit, @AuditLog...)
│   ├── aspect/                       # AOP 切面 (限流, 审计, 性能监控...)
│   ├── common/                       # 公共组件 (Result 响应封装)
│   ├── config/                       # Spring 配置 (Redis, CORS, Swagger...)
│   ├── exception/                    # 异常处理 (BusinessException, GlobalHandler)
│   ├── filter/                       # 过滤器 (UserContext 清理)
│   ├── interceptor/                  # 拦截器 (认证, 权限, 请求日志)
│   ├── monitor/                      # 监控指标 (DB/Memory 健康检查)
│   ├── security/                     # 安全组件 (UserContext, UserInfo)
│   ├── util/                         # 工具类 (JwtUtil)
│   ├── persistence/                  # 挥化实现
│   │   ├── mapper/                   # MyBatis Mapper
│   │   ├── repository/               # Repository 实现
│   │   ├── converter/                # PO ↔ Domain 转换
│   │   ├── po/                       # 持久化对象
│   │   └── interceptor/              # 数据权限拦截器
│   └── outbox/                       # Outbox Pattern 实现
│
├── interfaces/                       # 接口层 (对外 API)
│   ├── controller/                   # REST 控制器 (CQRS 分离)
│   ├── dto/                          # 数据传输对象
│   └── assembler/                    # DTO 转换器
│
└── ui/                               # 前端代码
    └── src/
        ├── api/                      # API 调用封装
        ├── components/               # 公共组件
        ├── router/                   # 路由配置 + 权限守卫
        ├── stores/                   # Pinia 状态管理
        └── views/                    # 页面组件
```

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.x
- Redis 7.x
- Kafka (可选，用于审计日志)
- Node.js 18+ (前端)

### 2. 数据库初始化

```bash
# 创建数据库
mysql -u root -proot -e "CREATE DATABASE IF NOT EXISTS jguard DEFAULT CHARSET utf8mb4"

# Flyway 自动迁移 (启动应用时自动执行)
# 或手动执行迁移脚本:
mysql -u root -proot jguard < src/main/resources/db/migration/V0__Init_Core_Tables.sql
mysql -u root -proot jguard < src/main/resources/db/migration/V1__Init_RBAC_Tables.sql
mysql -u root -proot jguard < src/main/resources/db/migration/V2__RBAC_Triggers.sql
mysql -u root -proot jguard < src/main/resources/db/migration/V3__RBAC_Preset_Data.sql
mysql -u root -proot jguard < src/main/resources/db/migration/V4__RBAC_Preset_Permissions.sql
mysql -u root -proot jguard < src/main/resources/db/migration/V5__Resource_Data_Dimension.sql
mysql -u root -proot jguard < src/main/resources/db/migration/V6__Core_Test_Data.sql
```

### 3. 启动基础设施

```bash
# macOS Homebrew
brew services start mysql
brew services start redis
brew services start kafka  # 可选

# 或使用 Docker Compose
docker-compose up -d mysql redis kafka
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

# 测试覆盖率报告
mvn jacoco:report
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
| http://localhost:8080/api-docs | OpenAPI JSON |

## API 接口

### 认证模块 `/api/v1/auth`

| 方法 | 路径 | 说明 | 认证 |
|-----|------|------|------|
| POST | `/api/v1/auth/login` | 用户登录，返回 Access Token + Refresh Token Cookie | 无需 |
| POST | `/api/v1/auth/register` | 用户注册 | 无需 |
| POST | `/api/v1/auth/refresh` | 刷新 Token (Cookie) | 无需 |
| POST | `/api/v1/auth/logout` | 用户登出 | 无需 |
| GET | `/api/v1/auth/permissions` | 获取当前用户完整权限 | Bearer Token |
| GET | `/api/v1/auth/menus` | 获取用户菜单树 | Bearer Token |
| GET | `/api/v1/auth/actions` | 获取用户操作权限 | Bearer Token |
| GET | `/api/v1/auth/fields` | 获取用户字段权限 | Bearer Token |

### 用户模块 `/api/v1/users`

| 方法 | 路径 | 说明 | 认证 |
|-----|------|------|------|
| GET | `/api/v1/users` | 分页查询用户列表 | Bearer Token |
| GET | `/api/v1/users/{id}` | 查询单个用户 | Bearer Token |
| GET | `/api/v1/users/{id}/permissions` | 查询用户权限详情 | Bearer Token |
| GET | `/api/v1/users/{id}/roles` | 查询用户角色列表 | Bearer Token |
| POST | `/api/v1/users` | 创建用户 | Bearer Token |
| PUT | `/api/v1/users/{id}` | 更新用户 | Bearer Token |
| DELETE | `/api/v1/users/{id}` | 删除用户 | Bearer Token |

### 角色模块 `/api/v1/roles`

| 方法 | 路径 | 说明 | 认证 |
|-----|------|------|------|
| GET | `/api/v1/roles` | 查询角色树 | Bearer Token |
| GET | `/api/v1/roles/{id}` | 查询角色详情 | Bearer Token |
| GET | `/api/v1/roles/user/{userId}` | 查询用户角色 | Bearer Token |
| POST | `/api/v1/roles` | 创建角色 | Bearer Token |
| PUT | `/api/v1/roles/{id}` | 更新角色 | Bearer Token |
| DELETE | `/api/v1/roles/{id}` | 删除角色 | Bearer Token |
| POST | `/api/v1/roles/{id}/permissions` | 分配权限 | Bearer Token |
| POST | `/api/v1/roles/user/{userId}/roles` | 分配角色 | Bearer Token |

### 资源模块 `/api/v1/resources`

| 方法 | 路径 | 说明 | 认证 |
|-----|------|------|------|
| GET | `/api/v1/resources` | 查询资源树 | Bearer Token |
| GET | `/api/v1/resources/tree` | 查询资源树 V2 | Bearer Token |
| GET | `/api/v1/resources/{id}` | 查询资源详情 | Bearer Token |
| GET | `/api/v1/resources/{id}/fields` | 查询敏感字段 | Bearer Token |
| GET | `/api/v1/resources/apis` | 查询 API 资源 | Bearer Token |
| POST | `/api/v1/resources` | 创建资源 | Bearer Token |
| PUT | `/api/v1/resources/{id}` | 更新资源 | Bearer Token |
| DELETE | `/api/v1/resources/{id}` | 删除资源 | Bearer Token |

### 接口测试示例

```bash
# 用户登录 (密码: 123456，已 BCrypt 加密)
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"张三","password":"123456"}'

# 使用 Refresh Token 刷新 (Cookie)
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Cookie: refreshToken=<your-refresh-token>"

# 查询用户列表
curl "http://localhost:8080/api/v1/users?pageNum=1&pageSize=5" \
  -H "Authorization: Bearer <access-token>"

# 查询用户权限
curl "http://localhost:8080/api/v1/auth/permissions" \
  -H "Authorization: Bearer <access-token>"
```

## 安全机制

### JWT 双 Token 机制

| Token | 有效期 | 存储 | 用途 |
|-------|--------|------|------|
| Access Token | 30 分钟 | 响应体 | API 认证 |
| Refresh Token | 7 天 | HttpOnly Cookie | Token 刷新 |

### 密码安全

- **前端**: SHA256 哈希传输
- **后端**: BCrypt (strength=12) 加密存储
- **验证**: 双重哈希校验

### 限流机制

```java
@RateLimit(key = "login", time = 60, count = 5)  // 60秒内最多5次
@PostMapping("/login")
public Result<LoginResponse> login(...) { ... }
```

- **Redis + Lua**: 滑动窗口算法
- **类型**: DEFAULT / IP / USER
- **降级**: fail-closed (默认拒绝更安全)

## 部署指南

### Docker Compose (本地开发)

```bash
# 启动所有服务
docker-compose up -d

# 查看状态
docker-compose ps

# 查看日志
docker-compose logs -f backend

# 停止服务
docker-compose down
```

### Kubernetes (生产环境)

```bash
# 设置镜像
export REGISTRY=ghcr.io
export IMAGE_NAME=your-org/jguard
export IMAGE_TAG=v1.0.0

# 部署
./deploy/k8s-deploy.sh

# 检查状态
kubectl get pods -n jguard
kubectl get services -n jguard
```

### 环境变量

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `DB_URL` | MySQL 连接 | `jdbc:mysql://localhost:3306/jguard` |
| `DB_USERNAME` | 数据库用户 | `root` |
| `DB_PASSWORD` | 数据库密码 | `root` |
| `REDIS_HOST` | Redis 地址 | `localhost` |
| `REDIS_PORT` | Redis 端口 | `6379` |
| `REDIS_PASSWORD` | Redis 密码 | `root` |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka 地址 | `localhost:9092` |
| `JWT_SECRET` | JWT 密钥 (>=32字符) | **必须设置** |
| `CORS_ALLOWED_ORIGINS` | CORS 白名单 | `http://localhost:3000` |

## 数据库迁移 (Flyway)

| 版本 | 文件 | 说明 |
|------|------|------|
| V0 | `Init_Core_Tables` | 基础表 (user, refresh_token, audit_log, event_outbox) |
| V1 | `Init_RBAC_Tables` | RBAC 核心表 |
| V2 | `RBAC_Triggers` | 触发器 |
| V3 | `RBAC_Preset_Data` | 预置角色/资源 |
| V4 | `RBAC_Preset_Permissions` | 预置权限 |
| V5 | `Resource_Data_Dimension` | 数据维度 |
| V6 | `Core_Test_Data` | 测试用户数据 |

## 测试

```bash
# 运行所有测试 (325+)
mvn test

# 运行单个测试类
mvn test -Dtest=UserTest

# 运行单个测试方法
mvn test -Dtest=UserTest#testRegister

# 测试覆盖率
mvn jacoco:report
# 查看: target/site/jacoco/index.html
```

### 测试覆盖

| 测试类 | 测试数 | 覆盖范围 |
|--------|-------|---------|
| `UserTest` | 10 | User 聚合根行为 |
| `SessionTest` | 4 | Session 聚合根 |
| `UserIdTest` | 7 | 值对象验证 |
| `UsernameTest` | 4 | 值对象验证 |
| `EmailTest` | 4 | 值对象验证 |
| `JwtUtilTest` | 11 | JWT 工具类 |
| `AuthControllerTest` | 10 | 认证接口 |
| `UserCommandControllerTest` | 10 | 用户命令接口 |
| `UserQueryControllerTest` | 6 | 用户查询接口 |
| `RoleCommandControllerTest` | 9 | 角色命令接口 |
| `RoleQueryControllerTest` | 6 | 角色查询接口 |

## 监控端点

| 地址 | 说明 |
|-----|------|
| `/actuator/health` | 健康检查 (DB + Memory) |
| `/actuator/metrics` | 性能指标 |
| `/actuator/info` | 应用信息 |
| `/actuator/loggers` | 日志级别 |
| `/api/v1/cache/metrics` | 权限缓存命中率 |
| `/api/v1/cache/metrics/health` | 缓存健康状态 |

## License

MIT License