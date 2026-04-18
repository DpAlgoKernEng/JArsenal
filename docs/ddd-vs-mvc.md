# DDD vs MVC 架构对比详解

## 一、核心概念对比

### 1.1 MVC (Model-View-Controller)

MVC 是一种**表现层架构模式**，最初用于图形用户界面设计，后被引入 Web 开发。

```
┌─────────────────────────────────────────────────────────────────┐
│                        MVC 架构                                  │
├─────────────────────────────────────────────────────────────────┤
│  Controller ──────▶ Service ──────▶ DAO/Mapper ──────▶ Database│
│      │                │                 │                        │
│      │                ▼                 │                        │
│      │             Model               │                        │
│      │              (Entity)            │                        │
│      │                │                 │                        │
│      ▼                ▼                 │                        │
│    View ◀────────────────               │                        │
└─────────────────────────────────────────────────────────────────┘
```

**特点：**
- 关注点分离：数据(Model)、展示(View)、控制(Controller)
- Model 通常是"贫血模型"，只有 getter/setter
- 业务逻辑分散在 Service 层

### 1.2 DDD (Domain-Driven Design)

DDD 是一种**领域驱动设计方法论**，强调以业务领域为核心进行建模。

```
┌─────────────────────────────────────────────────────────────────┐
│                        DDD 四层架构                              │
├─────────────────────────────────────────────────────────────────┤
│  Interfaces Layer     │ Controller, DTO, Assembler              │
│  (接口层)             │ 对外暴露 API，处理请求响应                 │
├─────────────────────────────────────────────────────────────────┤
│  Application Layer    │ Application Service, Command, Handler   │
│  (应用层)             │ 协调领域对象，编排业务流程                  │
├─────────────────────────────────────────────────────────────────┤
│  Domain Layer         │ Aggregate, Value Object, Domain Service │
│  (领域层 - 核心)       │ 业务逻辑核心，封装业务规则                │
├─────────────────────────────────────────────────────────────────┤
│  Infrastructure Layer │ Repository Impl, Mapper, Converter      │
│  (基础设施层)         │ 技术实现，持久化，外部服务集成              │
└─────────────────────────────────────────────────────────────────┘
```

**特点：**
- 领域模型是"富血模型"，包含数据和行为
- 业务逻辑集中在领域层
- 依赖倒置：领域层定义接口，基础设施层实现

---

## 二、核心区别详解

### 2.1 模型设计：贫血模型 vs 富血模型

#### MVC - 贫血模型

```java
// Entity - 只有 getter/setter，无业务行为
public class User {
    private Long id;
    private String username;
    private Integer status;  // 1=启用, 0=禁用

    // 只有 getter/setter，没有任何业务逻辑
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}

// Service - 业务逻辑在这里
@Service
public class UserService {
    public void disableUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 0) {
            throw new BusinessException("用户已禁用");
        }
        user.setStatus(0);
        userMapper.update(user);
    }
}
```

**问题：**
- Entity 只是数据容器，业务逻辑暴露在 Service
- 状态变更不受控，任何代码都可以 `user.setStatus(0)`
- 业务规则分散，难以维护

#### DDD - 富血模型

```java
// Aggregate Root - 封装数据和行为
public class User extends BaseEntity<UserId> {
    private Username username;
    private Email email;
    private UserStatus status;  // 值对象，非原始类型

    // 工厂方法
    public static User register(Username username, Email email,
                                EncryptedPassword password) {
        User user = new User();
        user.username = username;
        user.email = email;
        user.status = UserStatus.ENABLED;  // 业务规则：注册默认启用
        user.registerEvent(new UserRegistered(user.username));
        return user;
    }

    // 业务行为：禁用账号
    public void disable() {
        if (this.status == UserStatus.DISABLED) {
            throw new DomainException("账号已处于禁用状态");  // 自我保护
        }
        this.status = UserStatus.DISABLED;
        this.updateTime = LocalDateTime.now();
        this.registerEvent(new UserStatusChanged(this.id, this.status));
    }

    // 业务行为：检查可登录
    public void validateCanLogin() {
        if (this.status != UserStatus.ENABLED) {
            throw new DomainException("账号已被禁用");
        }
    }

    // 没有 setStatus() 方法！状态变更只能通过业务方法
}
```

**优势：**
- 业务逻辑内聚于领域对象
- 状态变更受控，只能通过业务方法
- 自我验证，不依赖外部校验

---

### 2.2 依赖方向

#### MVC - 自上而下依赖

```
Controller → Service → Mapper/DAO → Database
    ↓           ↓
   DTO       Entity
```

- 所有层都依赖数据库和 ORM
- 领域逻辑无法独立于基础设施

#### DDD - 依赖倒置

```
┌────────────────────────────────────────────┐
│  Domain Layer (核心，无外部依赖)            │
│  ┌─────────────────────────────────────┐  │
│  │ public interface UserRepository {    │  │
│  │     User findById(UserId id);       │  │
│  │     void save(User user);           │  │
│  │ }                                   │  │
│  └─────────────────────────────────────┘  │
└────────────────────────────────────────────┘
                    ▲
                    │ 实现
┌────────────────────────────────────────────┐
│  Infrastructure Layer                      │
│  ┌─────────────────────────────────────┐  │
│  │ @Repository                          │  │
│  │ public class UserRepositoryImpl      │  │
│  │     implements UserRepository {      │  │
│  │     private final UserMapper mapper; │  │
│  │     // ...                           │  │
│  │ }                                   │  │
│  └─────────────────────────────────────┘  │
└────────────────────────────────────────────┘
```

**优势：**
- 领域层不依赖任何外部框架
- 可以脱离 Spring、数据库进行单元测试
- 基础设施可替换（换数据库不影响领域逻辑）

---

### 2.3 数据验证

#### MVC - 外部验证

```java
// DTO 注解验证
public class UserCreateRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 50, message = "用户名长度2-50")
    private String username;

    @Email(message = "邮箱格式不正确")
    private String email;
}

// Service 层 if-else 验证
@Service
public class UserService {
    public void createUser(UserCreateRequest request) {
        if (userMapper.countByUsername(request.getUsername()) > 0) {
            throw new BusinessException("用户名已存在");
        }
        // ...
    }
}
```

**问题：**
- 验证逻辑分散：DTO 注解 + Service if-else
- 重复验证，难以维护

#### DDD - 值对象自验证

```java
// 值对象 - 构造即验证
public class Username {
    private final String value;  // 不可变

    public Username(String value) {
        if (value == null || value.isEmpty()) {
            throw new DomainException("用户名不能为空");
        }
        if (value.length() < 2 || value.length() > 50) {
            throw new DomainException("用户名长度必须在2-50字符之间");
        }
        this.value = value;
    }

    public String value() { return this.value; }
}

// 使用 - 无法创建无效对象
Username username = new Username("a");  // 抛出异常
Username username = new Username(null); // 抛出异常
```

**优势：**
- 验证逻辑内聚于值对象
- 任何地方创建都需要验证
- 不可变，线程安全

---

### 2.4 事务与业务逻辑

#### MVC - Service 层事务

```java
@Service
public class UserService {
    @Transactional
    public void register(UserCreateRequest request) {
        // 所有逻辑都在 Service
        User user = new User();
        user.setUsername(request.getUsername());
        user.setStatus(1);
        userMapper.insert(user);

        // 审计日志也在 Service
        AuditLog log = new AuditLog();
        log.setOperation("REGISTER");
        auditLogMapper.insert(log);

        // 发送邮件也在 Service
        emailService.sendWelcomeEmail(user.getEmail());
    }
}
```

**问题：**
- Service 职责过重
- 业务逻辑与技术细节混杂

#### DDD - 应用服务协调，领域模型承载逻辑

```java
// Application Service - 编排流程
@Service
public class UserApplicationService {
    @Transactional
    public Long register(RegisterCommand command) {
        // 1. 创建值对象
        Username username = new Username(command.getUsername());
        Email email = new Email(command.getEmail());

        // 2. 领域服务校验唯一性
        userDomainService.ensureUsernameUnique(username);

        // 3. 聚合根创建（业务逻辑在聚合根内部）
        User user = User.register(username, email, password);

        // 4. 持久化
        userRepository.save(user);

        // 5. 发布领域事件（解耦）
        eventPublisher.publishAll(user.pendingEvents());

        return user.getId().value();
    }
}

// Domain - 业务逻辑
public class User extends BaseEntity<UserId> {
    public static User register(Username username, Email email,
                                EncryptedPassword password) {
        User user = new User();
        user.username = username;
        user.email = email;
        user.status = UserStatus.ENABLED;
        user.registerEvent(new UserRegistered(username));  // 事件
        return user;
    }
}

// Event Handler - 异步处理
@Component
public class AuditLogEventHandler {
    @KafkaListener(topics = "domain-events")
    public void handleUserRegistered(UserRegistered event) {
        // 审计日志处理
        auditLogRepository.save(AuditLog.fromEvent(event));
    }
}
```

**优势：**
- 应用服务职责单一：协调流程
- 领域逻辑集中在聚合根
- 事件驱动实现解耦

---

## 三、代码对比实例

### 3.1 用户禁用功能

#### MVC 实现

```java
// Controller
@RestController
public class UserController {
    @PutMapping("/users/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @RequestParam Integer status) {
        userService.updateStatus(id, status);
        return Result.success();
    }
}

// Service
@Service
public class UserServiceImpl implements UserService {
    public void updateStatus(Long id, Integer status) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (status != 0 && status != 1) {
            throw new BusinessException("状态值无效");
        }
        if (user.getStatus().equals(status)) {
            throw new BusinessException("状态未变化");
        }
        user.setStatus(status);
        userMapper.update(user);

        // 记录审计日志
        AuditLog log = new AuditLog();
        log.setOperation("UPDATE_STATUS");
        log.setTargetId(id);
        auditLogMapper.insert(log);
    }
}

// Entity - 贫血
public class User {
    private Long id;
    private Integer status;
    // getter/setter
}
```

#### DDD 实现

```java
// Controller (Interfaces Layer)
@RestController
public class UserCommandController {
    @PutMapping("/users/{id}")
    public Result<UserResponse> updateUser(@PathVariable Long id,
                                           @RequestBody UserUpdateRequest request) {
        UpdateUserCommand command = new UpdateUserCommand(
            id, request.getUsername(), request.getEmail(), request.getStatus()
        );
        userApplicationService.updateUser(command);
        return Result.success(userAssembler.toResponse(...));
    }
}

// Application Service (Application Layer)
@Service
public class UserApplicationService {
    public void updateUser(UpdateUserCommand command) {
        User user = userRepository.findById(new UserId(command.getUserId()));
        if (user == null) {
            throw new DomainException("用户不存在");
        }

        if (command.getStatus() != null) {
            UserStatus status = UserStatus.fromCode(command.getStatus());
            if (status == UserStatus.ENABLED) {
                user.enable();   // 业务行为
            } else {
                user.disable();  // 业务行为
            }
        }

        userRepository.save(user);
        eventPublisher.publishAll(user.pendingEvents());
    }
}

// Aggregate Root (Domain Layer) - 业务逻辑封装
public class User extends BaseEntity<UserId> {
    private UserStatus status;

    public void disable() {
        // 业务规则：已禁用不能重复禁用
        if (this.status == UserStatus.DISABLED) {
            throw new DomainException("账号已处于禁用状态");
        }
        this.status = UserStatus.DISABLED;
        this.updateTime = LocalDateTime.now();
        this.registerEvent(new UserStatusChanged(this.id, UserStatus.DISABLED));
    }

    public void enable() {
        if (this.status == UserStatus.ENABLED) {
            throw new DomainException("账号已处于启用状态");
        }
        this.status = UserStatus.ENABLED;
        this.updateTime = LocalDateTime.now();
        this.registerEvent(new UserStatusChanged(this.id, UserStatus.ENABLED));
    }
}

// Event Handler (Application Layer) - 异步审计
@Component
public class AuditLogEventHandler {
    @KafkaListener(topics = "domain-events")
    public void handleUserStatusChanged(UserStatusChanged event) {
        AuditLog log = AuditLog.success(
            event.getUserId(), null,
            OperationType.UPDATE, ModuleType.USER,
            "用户状态变更为: " + event.getNewStatus(),
            null, null, 0
        );
        auditLogRepository.save(log);
    }
}
```

---

## 四、架构对比总结表

| 维度 | MVC | DDD |
|-----|-----|-----|
| **模型类型** | 贫血模型 (只有数据) | 富血模型 (数据+行为) |
| **业务逻辑位置** | Service 层 | 领域层 (聚合根) |
| **依赖方向** | Controller → Service → DAO | 领域层定义接口，基础设施层实现 |
| **验证方式** | DTO 注解 + Service if-else | 值对象自验证 |
| **状态变更** | 任意 setter | 通过业务方法 |
| **可测试性** | 需要 Spring 容器 | 领域层可独立测试 |
| **事务边界** | Service 层方法 | 应用服务层方法 |
| **代码复用** | 较低 (逻辑分散) | 较高 (领域对象可复用) |
| **学习曲线** | 低 | 中高 |
| **适用场景** | CRUD 为主 | 复杂业务逻辑 |

---

## 五、适用场景分析

### 5.1 适合 MVC 的场景

- 业务逻辑简单的 CRUD 应用
- 团队对 DDD 不熟悉
- 项目周期短，快速交付
- 需求变化不频繁
- 以数据展示为主的管理后台

### 5.2 适合 DDD 的场景

- 业务逻辑复杂，规则多变
- 需要长期维护和演进
- 团队具备 DDD 知识
- 高可测试性要求
- 需要与业务专家协作建模
- 微服务架构，需要清晰的领域边界

---

## 六、本项目重构对比

### 6.1 重构前 (MVC)

```
com.jguard/
├── controller/       # UserController
├── service/          # UserService, UserServiceImpl
├── mapper/           # UserMapper
├── entity/           # User (贫血)
├── dto/              # UserCreateRequest, UserResponse
└── enums/            # UserStatus
```

**问题：**
- User 只有 getter/setter
- UserServiceImpl 包含所有业务逻辑
- 验证分散在 DTO 和 Service

### 6.2 重构后 (DDD)

```
com.jguard/
├── domain/                        # 领域层
│   ├── user/
│   │   ├── aggregate/User.java    # 聚合根 (富血模型)
│   │   ├── valueobject/           # 值对象 (自验证)
│   │   ├── repository/            # Repository 接口
│   │   ├── service/               # 领域服务
│   │   └── event/                 # 领域事件
│   └── auth/
│       └── aggregate/Session.java # Session 聚合根
├── application/                   # 应用层
│   ├── service/                   # 应用服务
│   └── command/                   # 命令对象
├── infrastructure/                # 基础设施层
│   ├── persistence/repository/    # Repository 实现
│   └── outbox/                    # Outbox Pattern
└── interfaces/                    # 接口层
    └── controller/                # CQRS 分离
```

**改进：**
- User 包含业务行为：`enable()`, `disable()`, `validateCanLogin()`
- 值对象自验证：`Username`, `Email`, `UserId`
- 依赖倒置：`UserRepository` 接口定义在领域层
- 事件驱动：`UserRegistered`, `UserLoggedIn`

---

## 七、总结

| 对比项 | MVC | DDD |
|-------|-----|-----|
| **核心理念** | 分离数据、展示、控制 | 以领域模型为核心 |
| **模型设计** | 贫血模型 | 富血模型 |
| **业务逻辑** | Service 层 | 领域层 |
| **可维护性** | 中 | 高 |
| **可测试性** | 中 | 高 |
| **学习成本** | 低 | 高 |
| **适用复杂度** | 简单到中等 | 中等到复杂 |

**选择建议：**
- 简单 CRUD → MVC
- 复杂业务 → DDD
- 团队能力不足 → MVC
- 长期演进项目 → DDD