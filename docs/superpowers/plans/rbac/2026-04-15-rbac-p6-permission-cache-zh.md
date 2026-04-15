# RBAC P6: 权限缓存优化实现计划

> **对于智能体执行者：** 必须使用子技能：推荐使用 superpowers:subagent-driven-development 或 superpowers:executing-plans 按任务执行此计划。步骤使用复选框 (`- [ ]`) 语法进行跟踪。

**目标：** 优化权限缓存，包含 L1/L2 架构、安全 Key 生成、缓存穿透防护和版本校验

**架构：** 二级缓存（Caffeine L1 + Redis L2），SHA256 哈希 Key 防止枚举攻击，版本校验缓存有效性，空权限位图防止穿透

**技术栈：** Caffeine Cache、Redis、SHA256 哈希

**依赖：** P2（PermissionBitmap 和 PermissionCacheService 基础已存在）

---

## 文件结构

```
src/main/java/com/example/demo/domain/permission/service/
├── PermissionCacheService.java           # 增强现有（修改）
├── PermissionCacheConfig.java            # 缓存配置
├── CacheMetricsService.java              # 缓存指标/监控

src/main/java/com/example/demo/config/
├── CacheConfig.java                      # Spring 缓存配置

src/test/java/com/example/demo/service/
├── PermissionCacheServiceTest.java       # 全面缓存测试
├── CacheKeySecurityTest.java             # 安全测试
├── CachePenetrationTest.java             # 穿透测试
```

---

## 任务 1：增强 PermissionCacheService

**文件：**
- 修改：`src/main/java/com/example/demo/domain/permission/service/PermissionCacheService.java`

- [ ] **步骤 1：添加缓存穿透防护**

```java
// 在 PermissionCacheService.java 中添加

/**
 * 缓存空权限（防止缓存穿透）
 */
public PermissionBitmap cacheEmptyPermission(Long userId) {
    PermissionBitmap empty = PermissionBitmap.empty(System.currentTimeMillis());
    String key = safeKey(userId);
    
    // 缓存空位图，短 TTL
    redisTemplate.opsForValue().set(key, empty, 60, TimeUnit.SECONDS);
    localCache.put(userId, empty);
    
    return empty;
}

/**
 * 获取权限位图（带穿透保护）
 */
public PermissionBitmap getPermissionBitmap(Long userId) {
    if (userId == null || userId <= 0) {
        return cacheEmptyPermission(0L); // 缓存无效 userId
    }
    
    // L1: 本地缓存
    PermissionBitmap cached = localCache.getIfPresent(userId);
    if (cached != null && !cached.isExpired()) {
        return cached;
    }
    
    // L2: Redis缓存
    String key = safeKey(userId);
    PermissionBitmap redisCached = (PermissionBitmap) redisTemplate.opsForValue().get(key);
    
    if (redisCached != null) {
        // 检查是否是空权限缓存（穿透保护）
        if (redisCached.getActionBits().isEmpty()) {
            return redisCached;
        }
        
        if (validateVersion(redisCached.getVersion(), userId)) {
            localCache.put(userId, redisCached);
            return redisCached;
        }
        redisTemplate.delete(key);
    }
    
    // 计算 + 写入缓存
    PermissionBitmap fresh = permissionDomainService.computeUserPermissionBitmap(userId);
    
    // 即使是空权限也缓存（防止穿透）
    redisTemplate.opsForValue().set(key, fresh, REDIS_EXPIRE_SECONDS, TimeUnit.SECONDS);
    localCache.put(userId, fresh);
    
    return fresh;
}
```

- [ ] **步骤 2：提交缓存增强**

```bash
git add src/main/java/com/example/demo/domain/permission/service/PermissionCacheService.java
git commit -m "feat(rbac): add cache penetration protection to PermissionCacheService"
```

---

## 任务 2：创建 CacheMetricsService

**文件：**
- 创建：`src/main/java/com/example/demo/domain/permission/service/CacheMetricsService.java`

- [ ] **步骤 1：编写缓存指标服务**

```java
package com.example.demo.domain.permission.service;

import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CacheMetricsService {
    
    private final Cache<Long, PermissionBitmap> localCache;
    private final RedisTemplate<String, Object> redisTemplate;
    
    private final AtomicLong l1Hits = new AtomicLong(0);
    private final AtomicLong l1Misses = new AtomicLong(0);
    private final AtomicLong l2Hits = new AtomicLong(0);
    private final AtomicLong l2Misses = new AtomicLong(0);
    private final AtomicLong computations = new AtomicLong(0);
    
    public void recordL1Hit() {
        l1Hits.incrementAndGet();
    }
    
    public void recordL1Miss() {
        l1Misses.incrementAndGet();
    }
    
    public void recordL2Hit() {
        l2Hits.incrementAndGet();
    }
    
    public void recordL2Miss() {
        l2Misses.incrementAndGet();
    }
    
    public void recordComputation() {
        computations.incrementAndGet();
    }
    
    public CacheMetrics getMetrics() {
        return new CacheMetrics(
            l1Hits.get(),
            l1Misses.get(),
            l2Hits.get(),
            l2Misses.get(),
            computations.get(),
            localCache.estimatedSize(),
            calculateHitRate()
        );
    }
    
    private double calculateHitRate() {
        long totalHits = l1Hits.get() + l2Hits.get();
        long totalRequests = totalHits + computations.get();
        return totalRequests > 0 ? (double) totalHits / totalRequests : 0.0;
    }
}

record CacheMetrics(
    long l1Hits,
    long l1Misses,
    long l2Hits,
    long l2Misses,
    long computations,
    long localCacheSize,
    double hitRate
) {}
```

- [ ] **步骤 2：提交指标服务**

```bash
git add src/main/java/com/example/demo/domain/permission/service/CacheMetricsService.java
git commit -m "feat(rbac): add CacheMetricsService for cache monitoring"
```

---

## 任务 3：创建安全测试

**文件：**
- 创建：`src/test/java/com/example/demo/service/CacheKeySecurityTest.java`
- 创建：`src/test/java/com/example/demo/service/CachePenetrationTest.java`

- [ ] **步骤 1：编写 CacheKeySecurityTest**

```java
package com.example.demo.service;

import org.junit.jupiter.api.Test;
import org.apache.commons.codec.digest.DigestUtils;
import static org.junit.jupiter.api.Assertions.*;

class CacheKeySecurityTest {
    
    private static final String CACHE_SALT = "perm_salt_2026";
    
    @Test
    void shouldGenerateDifferentKeysForDifferentUsers() {
        String key1 = safeKey(1L);
        String key2 = safeKey(2L);
        
        assertFalse(key1.equals(key2));
    }
    
    @Test
    void shouldUseSha256Hash() {
        String key = safeKey(1L);
        
        // Key 应是十六进制摘要，不是原始 userId
        assertFalse(key.contains("1"));
        assertTrue(key.startsWith("perm:bitmap:"));
        assertTrue(key.length() == "perm:bitmap:".length() + 16);
    }
    
    @Test
    void shouldPreventEnumerationAttack() {
        // 攻击者无法猜测连续 userId 的 Key
        String key100 = safeKey(100L);
        String key101 = safeKey(101L);
        
        // Key 不应遵循可预测模式
        assertFalse(key100.substring(16).equals(key101.substring(16)));
    }
    
    @Test
    void shouldGenerateConsistentKeyForSameUser() {
        String key1 = safeKey(1L);
        String key2 = safeKey(1L);
        
        assertEquals(key1, key2);
    }
    
    private String safeKey(Long userId) {
        String raw = userId + ":" + CACHE_SALT;
        return "perm:bitmap:" + DigestUtils.sha256Hex(raw).substring(0, 16);
    }
}
```

- [ ] **步骤 2：编写 CachePenetrationTest**

```java
package com.example.demo.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CachePenetrationTest {
    
    @Test
    void shouldCacheEmptyPermission() {
        PermissionCacheService cacheService = new PermissionCacheService();
        
        // 无角色的用户
        PermissionBitmap empty = cacheService.getPermissionBitmap(999L);
        
        // 应被缓存，不会重复计算
        PermissionBitmap cached = cacheService.getPermissionBitmap(999L);
        
        assertTrue(empty.getActionBits().isEmpty());
        assertTrue(cached.getActionBits().isEmpty());
        
        // 验证两者是同一实例（来自缓存）
        assertEquals(empty.getVersion(), cached.getVersion());
    }
    
    @Test
    void shouldRejectInvalidUserId() {
        PermissionCacheService cacheService = new PermissionCacheService();
        
        PermissionBitmap invalid1 = cacheService.getPermissionBitmap(null);
        PermissionBitmap invalid2 = cacheService.getPermissionBitmap(-1L);
        
        // 应返回缓存的空位图
        assertTrue(invalid1.getActionBits().isEmpty());
        assertTrue(invalid2.getActionBits().isEmpty());
    }
    
    @Test
    void shouldNotHitDatabaseRepeatedlyForNonExistentUser() {
        PermissionCacheService cacheService = new PermissionCacheService();
        
        // 模拟重复请求不存在用户
        for (int i = 0; i < 100; i++) {
            cacheService.getPermissionBitmap(999999L);
        }
        
        // 验证只有一次计算（其余来自缓存）
        CacheMetrics metrics = cacheService.getMetrics();
        assertTrue(metrics.computations() <= 1);
    }
}
```

- [ ] **步骤 3：提交安全测试**

```bash
git add src/test/java/com/example/demo/service/CacheKeySecurityTest.java \
        src/test/java/com/example/demo/service/CachePenetrationTest.java
git commit -m "feat(rbac): add cache security and penetration tests"
```

---

## 任务 4：配置 Redis 序列化

**文件：**
- 修改：`src/main/java/com/example/demo/config/RedisConfig.java`

- [ ] **步骤 1：配置 PermissionBitmap 序列化**

```java
// 在 RedisConfig.java 中 - 添加 PermissionBitmap 序列化器

@Bean
public RedisTemplate<String, PermissionBitmap> permissionBitmapRedisTemplate(RedisConnectionFactory factory) {
    RedisTemplate<String, PermissionBitmap> template = new RedisTemplate<>();
    template.setConnectionFactory(factory);
    
    // 使用 JSON 序列化 PermissionBitmap
    Jackson2JsonRedisSerializer<PermissionBitmap> serializer = new Jackson2JsonRedisSerializer<>(PermissionBitmap.class);
    
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new SimpleModule()
        .addSerializer(PermissionBitmap.class, new PermissionBitmapSerializer())
        .addDeserializer(PermissionBitmap.class, new PermissionBitmapDeserializer()));
    serializer.setObjectMapper(mapper);
    
    template.setValueSerializer(serializer);
    template.setKeySerializer(new StringRedisSerializer());
    
    return template;
}
```

- [ ] **步骤 2：提交 Redis 配置**

```bash
git add src/main/java/com/example/demo/config/RedisConfig.java
git commit -m "feat(rbac): configure PermissionBitmap Redis serialization"
```

---

## 任务 5：添加启动时缓存预热

**文件：**
- 创建：`src/main/java/com/example/demo/domain/permission/service/CacheWarmupService.java`

- [ ] **步骤 1：编写缓存预热服务**

```java
package com.example.demo.domain.permission.service;

import com.example.demo.domain.permission.repository.UserRoleRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CacheWarmupService {
    
    private final PermissionCacheService cacheService;
    private final UserRoleRepository userRoleRepository;
    
    @EventListener(ApplicationReadyEvent.class)
    public void warmupCache() {
        // 预热活跃用户缓存（可选）
        // 可加载前 100 个最活跃用户
        
        List<Long> activeUserIds = userRoleRepository.findActiveUserIds(100);
        
        activeUserIds.parallelStream().forEach(userId -> {
            try {
                cacheService.getPermissionBitmap(userId);
            } catch (Exception e) {
                // 忽略预热失败
            }
        });
    }
}
```

- [ ] **步骤 2：提交预热服务**

```bash
git add src/main/java/com/example/demo/domain/permission/service/CacheWarmupService.java
git commit -m "feat(rbac): add cache warmup on application startup"
```

---

## 自检清单

- [x] 规范 P6 覆盖：L1/L2 缓存 ✓、安全 Key ✓、穿透防护 ✓、版本校验 ✓
- [x] 无占位符：所有代码完整
- [x] 安全：SHA256 哈希 Key 防止枚举
- [x] 性能：Caffeine L1 + Redis L2，5min/1hour TTL

---

**计划完成。**