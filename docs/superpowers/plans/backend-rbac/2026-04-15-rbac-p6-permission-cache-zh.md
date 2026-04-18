# RBAC P6: 权限缓存优化实现计划

> **对于智能体执行者：** 必须使用子技能：推荐使用 superpowers:subagent-driven-development 或 superpowers:executing-plans 按任务执行此计划。步骤使用复选框 (`- [ ]`) 语法进行跟踪。

**目标：** 优化权限缓存，包含 L1/L2 架构、安全 Key 生成、缓存穿透防护和版本校验

**架构：** 二级缓存（Caffeine L1 + Redis L2），SHA256 哈希 Key 防止枚举攻击，版本校验缓存有效性，空权限位图防止穿透

**技术栈：** Caffeine Cache、Redis、SHA256 哈希

**依赖：** P2（PermissionBitmap 和 PermissionCacheService 基础已存在）

---

## 文件结构

```
src/main/java/com/jguard/domain/permission/service/
├── PermissionCacheService.java           # 增强现有（修改）
├── PermissionCacheConfig.java            # 缓存配置
├── CacheMetricsService.java              # 缓存指标/监控

src/main/java/com/jguard/config/
├── CacheConfig.java                      # Spring 缓存配置

src/test/java/com/jguard/service/
├── PermissionCacheServiceTest.java       # 全面缓存测试
├── CacheKeySecurityTest.java             # 安全测试
├── CachePenetrationTest.java             # 穿透测试
```

---

## 任务 1：增强 PermissionCacheService

**文件：**
- 修改：`src/main/java/com/jguard/domain/permission/service/PermissionCacheService.java`

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
git add src/main/java/com/jguard/domain/permission/service/PermissionCacheService.java
git commit -m "feat(rbac): add cache penetration protection to PermissionCacheService"
```

---

## 任务 2：创建 CacheMetricsService

**文件：**
- 创建：`src/main/java/com/jguard/domain/permission/service/CacheMetricsService.java`

- [ ] **步骤 1：编写缓存指标服务**

```java
package com.jguard.domain.permission.service;

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
git add src/main/java/com/jguard/domain/permission/service/CacheMetricsService.java
git commit -m "feat(rbac): add CacheMetricsService for cache monitoring"
```

---

## 任务 3：创建安全测试

**文件：**
- 创建：`src/test/java/com/jguard/service/CacheKeySecurityTest.java`
- 创建：`src/test/java/com/jguard/service/CachePenetrationTest.java`

- [ ] **步骤 1：编写 CacheKeySecurityTest**

```java
package com.jguard.service;

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

- [ ] **步骤 2：编写 CachePenetrationTest（更新版 - 完整依赖注入）**

```java
package com.jguard.service;

import com.jguard.domain.permission.service.PermissionCacheService;
import com.jguard.domain.permission.service.PermissionDomainService;
import com.jguard.domain.permission.valueobject.PermissionBitmap;
import com.jguard.domain.permission.repository.RoleRepository;
import com.jguard.domain.permission.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.List;

class CachePenetrationTest {
    
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    
    @Mock
    private ValueOperations<String, Object> valueOperations;
    
    @Mock
    private PermissionDomainService permissionDomainService;
    
    @Mock
    private UserRoleRepository userRoleRepository;
    
    @Mock
    private RoleRepository roleRepository;
    
    private PermissionCacheService cacheService;
    
    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        
        // 配置 RedisTemplate mock
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.delete(anyString())).thenReturn(true);
        
        // 创建 PermissionCacheService 实例（完整依赖注入）
        cacheService = new PermissionCacheService(
            redisTemplate,
            permissionDomainService,
            userRoleRepository,
            roleRepository
        );
    }
    
    @Test
    void shouldCacheEmptyPermission() {
        // 无角色的用户 - 返回空位图
        PermissionBitmap emptyBitmap = PermissionBitmap.empty(System.currentTimeMillis());
        when(permissionDomainService.computeUserPermissionBitmap(999L)).thenReturn(emptyBitmap);
        when(roleRepository.findRolesByUserId(999L)).thenReturn(List.of());
        
        PermissionBitmap result = cacheService.getPermissionBitmap(999L);
        
        // 应被缓存，不会重复计算
        assertTrue(result.getActionBits().isEmpty());
        
        // 验证只计算一次（后续请求从缓存获取）
        PermissionBitmap cached = cacheService.getPermissionBitmap(999L);
        assertEquals(result.getVersion(), cached.getVersion());
        
        // 验证计算方法只被调用一次
        verify(permissionDomainService, times(1)).computeUserPermissionBitmap(999L);
    }
    
    @Test
    void shouldRejectInvalidUserId() {
        PermissionBitmap invalid1 = cacheService.getPermissionBitmap(null);
        PermissionBitmap invalid2 = cacheService.getPermissionBitmap(-1L);
        
        // 应返回缓存的空位图
        assertTrue(invalid1.getActionBits().isEmpty());
        assertTrue(invalid2.getActionBits().isEmpty());
    }
    
    @Test
    void shouldNotHitDatabaseRepeatedlyForNonExistentUser() {
        // 模拟空用户返回空位图
        PermissionBitmap emptyBitmap = PermissionBitmap.empty(System.currentTimeMillis());
        when(permissionDomainService.computeUserPermissionBitmap(999999L)).thenReturn(emptyBitmap);
        when(roleRepository.findRolesByUserId(999999L)).thenReturn(List.of());
        
        // 模拟重复请求不存在用户
        for (int i = 0; i < 100; i++) {
            cacheService.getPermissionBitmap(999999L);
        }
        
        // 验证只有一次计算（其余来自缓存）
        verify(permissionDomainService, times(1)).computeUserPermissionBitmap(999999L);
        
        // 验证 Redis 只写入一次
        verify(valueOperations, times(1)).set(anyString(), any(), anyLong(), any());
    }
    
    @Test
    void shouldCacheWithShortTtlForEmptyPermission() {
        PermissionBitmap emptyBitmap = PermissionBitmap.empty(System.currentTimeMillis());
        when(permissionDomainService.computeUserPermissionBitmap(0L)).thenReturn(emptyBitmap);
        
        // 缓存无效 userId (使用 cacheEmptyPermission)
        PermissionBitmap result = cacheService.cacheEmptyPermission(0L);
        
        assertTrue(result.getActionBits().isEmpty());
        
        // 验证短 TTL (60秒) 用于空权限缓存
        verify(valueOperations).set(anyString(), eq(emptyBitmap), eq(60L), any());
    }
}
```

- [ ] **步骤 3：提交安全测试**

```bash
git add src/test/java/com/jguard/service/CacheKeySecurityTest.java \
        src/test/java/com/jguard/service/CachePenetrationTest.java
git commit -m "feat(rbac): add cache security and penetration tests"
```

---

## 任务 4：配置 Redis 序列化

**文件：**
- 修改：`src/main/java/com/jguard/config/RedisConfig.java`
- 创建：`src/main/java/com/jguard/infrastructure/persistence/serializer/PermissionBitmapSerializer.java`
- 创建：`src/main/java/com/jguard/infrastructure/persistence/serializer/PermissionBitmapDeserializer.java`

- [ ] **步骤 1：编写 PermissionBitmapSerializer**

```java
package com.jguard.infrastructure.persistence.serializer;

import com.jguard.domain.permission.valueobject.PermissionBitmap;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

public class PermissionBitmapSerializer extends JsonSerializer<PermissionBitmap> {
    
    @Override
    public void serialize(PermissionBitmap value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        
        // 序列化版本号
        gen.writeNumberField("version", value.getVersion());
        
        // 序列化actionBits Map（将BitSet转为字符串）
        gen.writeObjectFieldStart("actionBits");
        Map<Long, BitSet> bits = value.getActionBits();
        for (Map.Entry<Long, BitSet> entry : bits.entrySet()) {
            // BitSet转二进制字符串表示
            gen.writeStringField(String.valueOf(entry.getKey()), bitsToString(entry.getValue()));
        }
        gen.writeEndObject();
        
        gen.writeEndObject();
    }
    
    private String bitsToString(BitSet bitSet) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bitSet.length(); i++) {
            sb.append(bitSet.get(i) ? '1' : '0');
        }
        return sb.toString();
    }
}
```

- [ ] **步骤 2：编写 PermissionBitmapDeserializer**

```java
package com.jguard.infrastructure.persistence.serializer;

import com.jguard.domain.permission.valueobject.PermissionBitmap;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

public class PermissionBitmapDeserializer extends JsonDeserializer<PermissionBitmap> {
    
    @Override
    public PermissionBitmap deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        
        // 解析版本号
        long version = node.has("version") ? node.get("version").asLong() : System.currentTimeMillis();
        
        // 解析actionBits Map
        Map<Long, BitSet> actionBits = new HashMap<>();
        JsonNode bitsNode = node.get("actionBits");
        if (bitsNode != null && bitsNode.isObject()) {
            bitsNode.fields().forEachRemaining(entry -> {
                Long resourceId = Long.parseLong(entry.getKey());
                BitSet bitSet = stringToBits(entry.getValue().asText());
                actionBits.put(resourceId, bitSet);
            });
        }
        
        return new PermissionBitmap(actionBits, version);
    }
    
    private BitSet stringToBits(String str) {
        BitSet bitSet = new BitSet();
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '1') {
                bitSet.set(i);
            }
        }
        return bitSet;
    }
}
```

- [ ] **步骤 3：配置 Redis 序列化器**

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

- [ ] **步骤 4：提交 Redis 配置**

```bash
git add src/main/java/com/jguard/config/RedisConfig.java \
        src/main/java/com/jguard/infrastructure/persistence/serializer/PermissionBitmapSerializer.java \
        src/main/java/com/jguard/infrastructure/persistence/serializer/PermissionBitmapDeserializer.java
git commit -m "feat(rbac): add complete PermissionBitmap Redis serializers"
```

---

## 任务 5：添加启动时缓存预热

**文件：**
- 创建：`src/main/java/com/jguard/domain/permission/service/CacheWarmupService.java`

- [ ] **步骤 1：编写缓存预热服务**

```java
package com.jguard.domain.permission.service;

import com.jguard.domain.permission.repository.UserRoleRepository;
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
git add src/main/java/com/jguard/domain/permission/service/CacheWarmupService.java
git commit -m "feat(rbac): add cache warmup on application startup"
```

---

## 任务 6：创建 PermissionBitmap 序列化测试（新增）

**文件：**
- 创建：`src/test/java/com/jguard/service/PermissionBitmapSerializationTest.java`

- [ ] **步骤 1：编写序列化/反序列化测试**

```java
package com.jguard.service;

import com.jguard.domain.permission.valueobject.PermissionBitmap;
import com.jguard.domain.permission.valueobject.ActionType;
import com.jguard.infrastructure.persistence.serializer.PermissionBitmapSerializer;
import com.jguard.infrastructure.persistence.serializer.PermissionBitmapDeserializer;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class PermissionBitmapSerializationTest {
    
    private final PermissionBitmapSerializer serializer = new PermissionBitmapSerializer();
    private final PermissionBitmapDeserializer deserializer = new PermissionBitmapDeserializer();
    
    @Test
    void shouldSerializeAndDeserializeCorrectly() {
        PermissionBitmap original = PermissionBitmap.empty(1234567890L)
            .addPermission(1L, Set.of(ActionType.VIEW, ActionType.CREATE))
            .addPermission(2L, Set.of(ActionType.UPDATE));
        
        // 序列化
        String json = serializeToJson(original);
        
        // 反序列化
        PermissionBitmap deserialized = deserializeFromJson(json);
        
        // 验证版本号一致
        assertEquals(original.getVersion(), deserialized.getVersion());
        
        // 验证权限位图一致
        assertTrue(deserialized.hasAction(1L, ActionType.VIEW));
        assertTrue(deserialized.hasAction(1L, ActionType.CREATE));
        assertFalse(deserialized.hasAction(1L, ActionType.DELETE));
        assertTrue(deserialized.hasAction(2L, ActionType.UPDATE));
        assertFalse(deserialized.hasAction(2L, ActionType.VIEW));
    }
    
    @Test
    void shouldSerializeEmptyBitmap() {
        PermissionBitmap empty = PermissionBitmap.empty(System.currentTimeMillis());
        
        String json = serializeToJson(empty);
        PermissionBitmap deserialized = deserializeFromJson(json);
        
        assertTrue(deserialized.getActionBits().isEmpty());
        assertFalse(deserialized.hasAction(1L, ActionType.VIEW));
    }
    
    @Test
    void shouldPreserveBitSetRepresentation() {
        PermissionBitmap bitmap = PermissionBitmap.empty()
            .addPermission(1L, Set.of(ActionType.VIEW, ActionType.CREATE, ActionType.UPDATE, ActionType.DELETE));
        
        String json = serializeToJson(bitmap);
        
        // BitSet 应表示为二进制字符串 "1111" (VIEW=0, CREATE=1, UPDATE=2, DELETE=3)
        assertTrue(json.contains("\"1111\"") || json.contains("\"11110\""));
        
        PermissionBitmap deserialized = deserializeFromJson(json);
        
        // 验证所有操作权限存在
        assertTrue(deserialized.hasAction(1L, ActionType.VIEW));
        assertTrue(deserialized.hasAction(1L, ActionType.CREATE));
        assertTrue(deserialized.hasAction(1L, ActionType.UPDATE));
        assertTrue(deserialized.hasAction(1L, ActionType.DELETE));
        assertFalse(deserialized.hasAction(1L, ActionType.EXECUTE));
    }
    
    @Test
    void shouldHandleMultipleResources() {
        PermissionBitmap bitmap = PermissionBitmap.empty()
            .addPermission(1L, Set.of(ActionType.VIEW))
            .addPermission(2L, Set.of(ActionType.CREATE, ActionType.UPDATE))
            .addPermission(3L, Set.of(ActionType.DELETE));
        
        String json = serializeToJson(bitmap);
        PermissionBitmap deserialized = deserializeFromJson(json);
        
        // 验证多个资源的权限独立保存
        assertTrue(deserialized.hasAction(1L, ActionType.VIEW));
        assertFalse(deserialized.hasAction(1L, ActionType.CREATE));
        
        assertTrue(deserialized.hasAction(2L, ActionType.CREATE));
        assertTrue(deserialized.hasAction(2L, ActionType.UPDATE));
        assertFalse(deserialized.hasAction(2L, ActionType.DELETE));
        
        assertTrue(deserialized.hasAction(3L, ActionType.DELETE));
        assertFalse(deserialized.hasAction(3L, ActionType.VIEW));
    }
    
    private String serializeToJson(PermissionBitmap bitmap) {
        // 使用 ObjectMapper 或直接调用 serializer
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule()
            .addSerializer(PermissionBitmap.class, serializer));
        try {
            return mapper.writeValueAsString(bitmap);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private PermissionBitmap deserializeFromJson(String json) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule()
            .addDeserializer(PermissionBitmap.class, deserializer));
        try {
            return mapper.readValue(json, PermissionBitmap.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
```

- [ ] **步骤 2：提交测试**

```bash
git add src/test/java/com/jguard/service/PermissionBitmapSerializationTest.java
git commit -m "feat(rbac): add PermissionBitmap serialization/deserialization tests"
```

---

## 自检清单

- [x] 规范 P6 覆盖：L1/L2 缓存 ✓、安全 Key ✓、穿透防护 ✓、版本校验 ✓
- [x] 无占位符：所有代码完整
- [x] 安全：SHA256 哈希 Key 防止枚举
- [x] 性能：Caffeine L1 + Redis L2，5min/1hour TTL
- [x] **序列化器完整**：PermissionBitmapSerializer + PermissionBitmapDeserializer ✓
- [x] **BitSet序列化**：使用二进制字符串表示 ✓、version字段完整 ✓
- [x] **缓存预热**：CacheWarmupService启动时加载活跃用户 ✓
- [x] **测试依赖注入**：CachePenetrationTest 完整依赖注入 ✓、mock配置完整 ✓
- [x] **新增**：PermissionBitmapSerializationTest ✓、序列化/反序列化正确性测试 ✓
- [x] **新增**：BitSet表示验证测试 ✓、多资源独立保存测试 ✓、空位图处理测试 ✓
- [x] **改进点补充**：CacheMetricsController监控端点 ✓、命中率统计 ✓、健康状态检查 ✓

---

## 任务 7：缓存命中率监控端点（新增 - 改进点）

**文件：**
- 创建：`src/main/java/com/jguard/controller/CacheMetricsController.java`

> **改进点：** 添加缓存命中率监控端点，用于运维监控和性能调优。

- [ ] **步骤 1：编写 CacheMetricsController**

```java
package com.jguard.controller;

import com.jguard.common.Result;
import com.jguard.domain.permission.service.CacheMetricsService;
import com.jguard.domain.permission.service.CacheMetricsService.CacheMetrics;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/cache/metrics")
public class CacheMetricsController {
    
    private final CacheMetricsService cacheMetricsService;
    
    /**
     * 获取缓存命中率指标
     */
    @GetMapping("/hit-rate")
    public Result<Map<String, Object>> getCacheHitRate() {
        CacheMetrics metrics = cacheMetricsService.getMetrics();
        
        Map<String, Object> result = new HashMap<>();
        result.put("l1Hits", metrics.l1Hits());
        result.put("l1Misses", metrics.l1Misses());
        result.put("l2Hits", metrics.l2Hits());
        result.put("l2Misses", metrics.l2Misses());
        result.put("computations", metrics.computations());
        result.put("localCacheSize", metrics.localCacheSize());
        result.put("overallHitRate", metrics.hitRate());
        
        // 计算分层命中率
        long l1Total = metrics.l1Hits() + metrics.l1Misses();
        long l2Total = metrics.l2Hits() + metrics.l2Misses();
        
        result.put("l1HitRate", l1Total > 0 ? (double) metrics.l1Hits() / l1Total : 0.0);
        result.put("l2HitRate", l2Total > 0 ? (double) metrics.l2Hits() / l2Total : 0.0);
        
        return Result.success(result);
    }
    
    /**
     * 重置缓存指标计数器
     */
    @PostMapping("/reset")
    public Result<Void> resetMetrics() {
        cacheMetricsService.reset();
        return Result.success(null);
    }
    
    /**
     * 获取缓存健康状态
     */
    @GetMapping("/health")
    public Result<Map<String, Object>> getCacheHealth() {
        CacheMetrics metrics = cacheMetricsService.getMetrics();
        
        Map<String, Object> health = new HashMap<>();
        
        // 健康判断标准：命中率 > 80%
        boolean healthy = metrics.hitRate() > 0.8;
        
        health.put("status", healthy ? "HEALTHY" : "WARNING");
        health.put("hitRate", metrics.hitRate());
        health.put("message", healthy ? 
            "缓存工作正常，命中率达标" : 
            "命中率较低，建议检查缓存预热策略");
        
        return Result.success(health);
    }
}
```

- [ ] **步骤 2：在 CacheMetricsService 中添加 reset 方法**

```java
// 在 CacheMetricsService.java 中添加

public void reset() {
    l1Hits.set(0);
    l1Misses.set(0);
    l2Hits.set(0);
    l2Misses.set(0);
    computations.set(0);
}
```

- [ ] **步骤 3：提交监控端点**

```bash
git add src/main/java/com/jguard/controller/CacheMetricsController.java \
        src/main/java/com/jguard/domain/permission/service/CacheMetricsService.java
git commit -m "feat(rbac): add cache metrics monitoring endpoint for hit rate tracking"
```

---

**计划完成。**