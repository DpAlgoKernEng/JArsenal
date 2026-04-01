# 项目初始化指南

## 环境要求

| 组件 | 版本 | 说明 |
|------|------|------|
| JDK | 17+ | Java 运行环境 |
| Maven | 3.6+ | 构建工具 |
| MySQL | 8.x | 数据库 |
| Redis | 6.x+ | 分布式限流（可选，未启动时自动降级） |
| Node.js | 18+ | 前端运行环境 |

## 初始化步骤

### 1. 克隆项目

```bash
git clone git@github.com:DpAlgoKernEng/JArsenal.git
cd JArsenal
```

### 2. 创建数据库

```bash
# 登录 MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE IF NOT EXISTS demo DEFAULT CHARACTER SET utf8mb4;

# 导入表结构和测试数据
USE demo;
SOURCE src/main/resources/schema.sql;
```

或直接执行：
```bash
mysql -u root -proot demo < src/main/resources/schema.sql
```

### 3. 配置数据库连接

编辑 `src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/demo?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root      # 你的 MySQL 用户名
    password: root      # 你的 MySQL 密码
```

### 4. 启动后端

```bash
# 编译项目
mvn clean compile

# 运行测试
mvn test

# 启动应用
mvn spring-boot:run
```

### 5. 启动前端

```bash
cd ui

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

### 6. 验证启动

```bash
# 后端健康检查
curl http://localhost:8080/actuator/health
# 期望输出: {"status":"UP"}

# 前端访问
open http://localhost:3000
```

### 7. 测试登录

在浏览器打开 http://localhost:3000，使用以下账号登录：

| 用户名 | 密码 |
|--------|------|
| 张三 | 123456 |
| 李四 | 123456 |

或使用 curl 测试 API：
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"张三","password":"123456"}'
```

## 测试数据

初始化后包含 15 个测试用户：

| ID | 用户名 | 密码 | 状态 |
|----|--------|------|------|
| 1 | 张三 | 123456 | 正常 |
| 2 | 李四 | 123456 | 正常 |
| 3 | 王五 | 123456 | 禁用 |
| ... | ... | 123456 | ... |

密码使用 BCrypt 加密存储。

## 常见问题

### 数据库连接失败

```
Error: Access denied for user 'dev_user'@'localhost'
```

解决：修改 `application-dev.yml` 中的数据库用户名和密码为你的实际配置。

### 端口被占用

```bash
# 查看 8080 端口占用
lsof -i :8080

# 终止进程
kill -9 <PID>

# 或修改端口
export SERVER_PORT=8081
mvn spring-boot:run
```

### Redis 未启动

Redis 未启动时，限流功能自动降级为放行，不影响其他功能。

```bash
# 启动 Redis（macOS）
brew services start redis

# 启动 Redis（Linux）
redis-server
```

## 访问地址

| 地址 | 说明 |
|------|------|
| http://localhost:3000 | 前端管理界面 |
| http://localhost:8080/swagger-ui.html | Swagger API 文档 |
| http://localhost:8080/actuator/health | 健康检查 |
| http://localhost:8080/actuator/metrics | 性能指标 |