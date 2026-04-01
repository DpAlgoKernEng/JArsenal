# JArsenal - 企业级 Spring Boot REST API 示例

Java Core Arsenal Collections - 企业级 Spring Boot MVC 分页示例项目

## 项目简介

基于 Spring Boot 3.2.0 + MyBatis + PageHelper 的企业级 REST API 示例，包含完整的 MVC 架构、企业级配置和 Vue 3 前端界面。

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

## 项目结构

```
├── src/main/java/com/example/demo/   # 后端代码
│   ├── annotation/          # 自定义注解（限流等）
│   ├── aspect/              # AOP 切面（性能、日志、限流）
│   ├── common/              # 公共类（统一响应 Result）
│   ├── config/              # 配置类（跨域、拦截器、Swagger）
│   ├── controller/          # REST 控制器
│   ├── dto/                 # 数据传输对象（请求/响应）
│   ├── entity/              # 实体类
│   ├── exception/           # 异常处理（全局异常处理器）
│   ├── interceptor/         # 拦截器（请求日志、认证）
│   ├── mapper/              # MyBatis Mapper 接口
│   ├── monitor/             # 监控指标（健康检查）
│   ├── security/            # 安全认证（JWT、用户上下文）
│   ├── service/             # 服务层接口和实现
│   └── util/                # 工具类（JwtUtil）
│
└── ui/                               # 前端代码
    ├── src/
    │   ├── api/             # API 调用封装
    │   ├── components/      # 公共组件
    │   ├── router/          # 路由配置
    │   ├── stores/          # Pinia 状态管理
    │   └── views/           # 页面组件
    └── vite.config.js       # Vite 配置
```

## 快速开始

### 1. 环境要求
- JDK 17+
- Maven 3.6+
- MySQL 8.x
- Node.js 18+ (前端)

### 2. 数据库配置
```bash
# 创建数据库并导入测试数据
mysql -h localhost -P 3306 -u root -proot demo < src/main/resources/schema.sql
```

### 3. 启动后端
```bash
# 编译
mvn clean compile

# 运行
mvn spring-boot:run

# 打包
mvn clean package -DskipTests
```

### 4. 启动前端
```bash
cd ui

# 安装依赖
npm install

# 开发模式
npm run dev

# 生产构建
npm run build
```

### 5. 访问地址

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

### 用户模块 `/api/users` (需要 JWT Token)

| 方法 | 赯径 | 说明 | 认证 |
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
    "pages": 3,
    "pageNum": 1,
    "pageSize": 5,
    "hasNextPage": true,
    "hasPreviousPage": false
  }
}
```

## 企业级配置

### 已集成功能

| 功能 | 说明 |
|-----|------|
| JWT 认证 | Token 无状态认证，BCrypt 密码加密 |
| Redis 限流 | `@RateLimit` 注解，滑动窗口算法，支持 IP/USER/DEFAULT |
| 全局异常处理 | 统一异常响应，参数校验异常捕获 |
| 请求日志拦截器 | 记录请求耗时、IP、traceId、慢请求告警（>3s） |
| 跨域配置 | CORS 白名单配置化 |
| 接口性能监控 | AOP 记录接口耗时 |
| 参数校验 | Jakarta Validation + `@Valid` |
| Swagger 文档 | OpenAPI 3.0 规范 |
| Actuator 监控 | 健康检查、性能指标 |

### 监控端点

| 地址 | 说明 |
|-----|------|
| `/swagger-ui.html` | Swagger UI 文档 |
| `/api-docs` | OpenAPI JSON |
| `/actuator/health` | 健康检查（数据库 + 内存） |
| `/actuator/metrics` | 性能指标 |
| `/actuator/env` | 环境变量 |

## PageHelper 使用说明

```java
// 1. 在查询前调用 PageHelper.startPage()
PageHelper.startPage(pageNum, pageSize);

// 2. 执行查询（只对紧接着的第一个查询生效）
List<User> users = userMapper.selectAll();

// 3. 用 PageInfo 包装获取分页信息
PageInfo<User> pageInfo = new PageInfo<>(users);

// 获取分页数据
pageInfo.getList();       // 数据列表
pageInfo.getTotal();      // 总记录数
pageInfo.getPages();      // 总页数
pageInfo.getPageNum();    // 当前页
```

**重要**: `PageHelper.startPage()` 必须在查询语句**紧挨着的前面**调用。

## 扩展建议

- **Redis 缓存** - 分布式缓存
- **Prometheus + Grafana** - 监控告警
- **链路追踪** - Spring Cloud Sleuth + Zipkin
- **消息队列** - RabbitMQ/Kafka
- **定时任务** - Quartz/XXL-Job
- **容器化** - Docker + Kubernetes

## License

MIT License