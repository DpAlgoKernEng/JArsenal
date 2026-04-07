package com.example.demo.infrastructure.outbox;

import com.example.demo.domain.shared.event.DomainEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Outbox 发布器
 * 定时扫描 outbox 表，将待发送的事件发布到 Kafka
 */
@Slf4j
@Component
public class OutboxPublisher {

    private static final String TOPIC = "domain-events";
    private static final int BATCH_SIZE = 100;
    private static final int MAX_RETRY = 3;

    private final OutboxMapper outboxMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OutboxPublisher(OutboxMapper outboxMapper,
                           KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxMapper = outboxMapper;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * 定时发布待发送的事件（每 5 秒执行一次）
     */
    @Scheduled(fixedDelay = 5000, initialDelay = 5000)
    public void publishPendingEvents() {
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
                kafkaTemplate.send(TOPIC, message.getEventId(), wrappedPayload).get();

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