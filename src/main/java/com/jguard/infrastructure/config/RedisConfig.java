package com.jguard.infrastructure.config;

import com.jguard.domain.permission.valueobject.PermissionBitmap;
import com.jguard.infrastructure.persistence.serializer.PermissionBitmapSerializer;
import com.jguard.infrastructure.persistence.serializer.PermissionBitmapDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置
 */
@Configuration
public class RedisConfig {

    /**
     * 用于业务数据的 RedisTemplate (JSON 序列化)
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    /**
     * 用于限流等场景的 StringRedisTemplate (纯字符串序列化)
     * Lua 脚本参数传递需要纯字符串，避免 JSON 序列化导致 tonumber() 失败
     * 也用于 PermissionBitmap 的手动序列化存储
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(factory);
        return template;
    }

    /**
     * Primary ObjectMapper configured for PermissionBitmap serialization
     * and Java 8 date/time types (LocalDateTime, LocalDate, etc.)
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // 注册 JSR310 模块支持 LocalDateTime 等 Java 8 日期时间类型
        mapper.registerModule(new JavaTimeModule());
        // 禁用将日期写为时间戳，使用 ISO-8601 格式
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 注册 PermissionBitmap 自定义序列化器
        mapper.registerModule(new SimpleModule()
            .addSerializer(PermissionBitmap.class, new PermissionBitmapSerializer())
            .addDeserializer(PermissionBitmap.class, new PermissionBitmapDeserializer()));
        return mapper;
    }
}