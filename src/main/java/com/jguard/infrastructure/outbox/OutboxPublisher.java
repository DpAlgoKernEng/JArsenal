package com.jguard.infrastructure.outbox;

import com.jguard.domain.shared.event.DomainEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Outbox 发布器
 * 定时扫描 outbox 表，将待发送的事件发布到 Kafka
 * 使用 Redisson 分布式锁确保多实例部署时不会重复发布
 */
@Slf4j
@Component
public class OutboxPublisher {

    private static final int BATCH_SIZE = 100;
    private static final int MAX_RETRY = 3;

    private final OutboxMapper outboxMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final RedissonClient redissonClient;
    private final String outboxLockKey;

    /**
     * Kafka Topic 名称（从配置注入）
     */
    @Value("${kafka.topic.domain-events:domain-events}")
    private String topic;

    /**
     * 是否启用分布式锁（多实例部署时应启用）
     */
    @Value("${outbox.lock.enabled:true}")
    private boolean lockEnabled;

    public OutboxPublisher(OutboxMapper outboxMapper,
                           KafkaTemplate<String, String> kafkaTemplate,
                           RedissonClient redissonClient,
                           String outboxLockKey) {
        this.outboxMapper = outboxMapper;
        this.kafkaTemplate = kafkaTemplate;
        this.redissonClient = redissonClient;
        this.outboxLockKey = outboxLockKey;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * 定时发布待发送的事件（每 5 秒执行一次）
     * 使用分布式锁确保多实例部署时只有一个实例执行
     */
    @Scheduled(fixedDelay = 5000, initialDelay = 5000)
    public void publishPendingEvents() {
        if (!lockEnabled) {
            // 锁未启用，直接执行（单实例部署场景）
            publishEventsWithoutLock();
            return;
        }

        // 使用分布式锁
        RLock lock = redissonClient.getLock(outboxLockKey);
        boolean locked = false;
        try {
            // 尝试获取锁，不等待，持有30秒后自动释放
            locked = lock.tryLock(0, 30, TimeUnit.SECONDS);
            if (locked) {
                log.debug("Acquired distributed lock for outbox publishing");
                publishEventsWithoutLock();
            } else {
                log.debug("Another instance is publishing outbox events, skipping");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while acquiring lock for outbox publishing");
        } catch (Exception e) {
            log.error("Failed to acquire lock for outbox publishing", e);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("Released distributed lock for outbox publishing");
            }
        }
    }

    /**
     * 执行实际的事件发布逻辑（不加锁）
     */
    private void publishEventsWithoutLock() {
        List<OutboxMessage> messages = outboxMapper.selectPending(BATCH_SIZE);

        if (messages.isEmpty()) {
            return;
        }

        log.debug("Found {} pending events to publish", messages.size());

        for (OutboxMessage message : messages) {
            try {
                // 构建包含 eventType 的包装消息
                String wrappedPayload = String.format(
                    "{\"eventType\":\"%s\",\"payload\":%s}",
                    message.getEventType(),
                    message.getPayload()
                );

                // 发送到 Kafka
                kafkaTemplate.send(topic, message.getEventId(), wrappedPayload).get();

                // 标记为已发送
                outboxMapper.markAsSent(message.getId());
                log.debug("Event published: {}", message.getEventType());
            } catch (Exception e) {
                log.error("Failed to publish event: {}", message.getEventType(), e);

                // 标记为失败
                if (message.getRetryCount() >= MAX_RETRY - 1) {
                    outboxMapper.markAsFailed(message.getId(), e.getMessage());
                    log.warn("Event exceeded max retry count: {}", message.getEventId());
                } else {
                    outboxMapper.markAsFailed(message.getId(), e.getMessage());
                }
            }
        }
    }

    /**
     * 清理已发送的旧事件（每天执行一次）
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupOldEvents() {
        int deleted = outboxMapper.deleteSentOlderThan(7);
        if (deleted > 0) {
            log.info("Cleaned up {} old sent events", deleted);
        }
    }
}