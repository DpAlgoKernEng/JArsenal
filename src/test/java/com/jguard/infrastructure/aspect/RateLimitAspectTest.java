package com.jguard.infrastructure.aspect;

import com.jguard.infrastructure.annotation.RateLimit;
import com.jguard.infrastructure.annotation.RateLimit.LimitType;
import com.jguard.infrastructure.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * RateLimitAspect 测试
 */
@ExtendWith(MockitoExtension.class)
class RateLimitAspectTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private DefaultRedisScript<Long> rateLimitScript;

    @Test
    @DisplayName("限流配置 - fail-open 配置验证")
    void failOpen_defaultValue_shouldBeFalse() {
        // given - 创建 aspect
        RateLimitAspect aspect = new RateLimitAspect(stringRedisTemplate);

        // when - 读取默认值
        boolean failOpen = (boolean) ReflectionTestUtils.getField(aspect, "failOpen");

        // then - 默认应为 false (fail-closed)
        assertFalse(failOpen, "默认应使用 fail-closed 模式");
    }

    @Test
    @DisplayName("限流配置 - fail-open 可配置")
    void failOpen_canBeConfigured_shouldUpdate() {
        // given
        RateLimitAspect aspect = new RateLimitAspect(stringRedisTemplate);

        // when - 设置为 fail-open
        ReflectionTestUtils.setField(aspect, "failOpen", true);

        // then
        boolean failOpen = (boolean) ReflectionTestUtils.getField(aspect, "failOpen");
        assertTrue(failOpen);
    }
}