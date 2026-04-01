# JArsenal - 企业级 Spring Boot REST API 示例

Java Core Arsenal Collections - 企业级 Spring Boot MVC 分页示例项目

## 项目简介

基于 Spring Boot 3.2.0 + MyBatis + PageHelper 的企业级 REST API 示例，包含完整的 MVC 架构和企业级配置。

## 技术栈

| 技术 | 版本 | 说明 |
|-----|------|------|
| Spring Boot | 3.2.0 | 基础框架 |
| Java | 17 | 运行环境 |
| MyBatis | 3.0.3 | ORM 框架 |
| PageHelper | 2.1.0 | 分页插件 |
| MySQL | 8.x | 数据库 |
| Springdoc | 2.3.0 | OpenAPI 文档 |
| Lombok | - | 简化代码 |

**注意**: Spring Boot 3.x 使用 Jakarta EE 9+，所有 `javax.*` 包名改为 `jakarta.*`

## 项目结构

```
src/main/java/com/example/demo/
├── annotation/          # 自定义注解（限流等）
├── aspect/              # AOP 切面（性能、日志、限流）
├── common/              # 公共类（统一响应 Result）
├── config/              # 配置类（跨域、拦截器、Swagger）
├── controller/          # REST 控制器
├── dto/                 # 数据传输对象（请求/响应）
├── entity/              # 实体类
├── exception/           # 异常处理（全局异常处理器）
├── interceptor/         # 拦截器（请求日志、认证）
├── mapper/              # MyBatis Mapper 接口
├── monitor/             # 监控指标（健康检查）
└── service/             # 服务层接口和实现
```

## 快速开始

### 1. 环境要求
- JDK 17+
- Maven 3.6+
- MySQL 8.x

### 2. 数据库配置
```bash
# 创建数据库并导入测试数据
mysql -h localhost -P 3306 -u root -proot < src/main/resources/schema.sql
```

### 3. 修改配置
编辑 `src/main/resources/application.yml` 中的数据库连接信息：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/demo
    username: root
    password: root
```

### 4. 运行项目
```bash
# 编译
mvn clean compile

# 运行
mvn spring-boot:run

# 打包
mvn clean package -DskipTests
```

## API 接口

| 方法 | 路径 | 说明 |
|-----|------|------|
| GET | `/api/users` | 分页查询用户列表 |
| GET | `/api/users/{id}` | 查询单个用户 |
| POST | `/api/users` | 创建用户 |
| PUT | `/api/users/{id}` | 更新用户 |
| DELETE | `/api/users/{id}` | 删除用户 |

### 分页查询示例
```bash
# 基础分页
curl "http://localhost:8080/api/users?pageNum=1&pageSize=5"

# 条件查询
curl "http://localhost:8080/api/users?pageNum=1&pageSize=5&username=张&status=1"
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
| 全局异常处理 | 统一异常响应，参数校验异常捕获 |
| 请求日志拦截器 | 记录请求耗时、IP、慢请求告警（>3s） |
| 认证拦截器 | Token 验证示例（可扩展 JWT） |
| 跨域配置 | CORS 跨域支持 |
| 接口性能监控 | AOP 记录接口耗时 |
| 请求限流 | `@RateLimit` 注解（内存版，生产建议 Redis） |
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
- **Redis 限流** - 分布式限流
- **Spring Security** - 安全认证框架
- **JWT** - 无状态认证
- **链路追踪** - Spring Cloud Sleuth + Zipkin
- **消息队列** - RabbitMQ/Kafka
- **定时任务** - Quartz/XXL-Job

## License

MIT License