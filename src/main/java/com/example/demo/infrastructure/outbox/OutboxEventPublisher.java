package com.example.demo.infrastructure.outbox;

import com.example.demo.domain.shared.event.DomainEvent;
import com.example.demo.domain.shared.event.DomainEventPublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

/**
 * Outbox 事件发布器
 * 在事务内将事件保存到 outbox 表，保证与业务数据的原子性
 */
@Slf4j
@Component
public class OutboxEventPublisher {

    private final OutboxMapper outboxMapper;
    private final ObjectMapper objectMapper;

    public OutboxEventPublisher(OutboxMapper outboxMapper) {
        this.outboxMapper = outboxMapper;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * 保存事件到 Outbox（在事务内调用）
     */
    public void saveToOutbox(DomainEvent event, String aggregateType, String aggregateId) {
        try {
            OutboxMessage message = new OutboxMessage();
            message.setEventId(event.eventId());
            message.setEventType(event.eventType());
            message.setAggregateType(aggregateType);
            message.setAggregateId(aggregateId);
            message.setPayload(objectMapper.writeValueAsString(event));

            outboxMapper.insert(message);
            log.debug("Event saved to outbox: {}", event.eventType());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event: {}", event.eventType(), e);
            throw new RuntimeException("Failed to serialize event", e);
        }
    }

    /**
     * 批量保存事件到 Outbox
     */
    public void saveAllToOutbox(List<DomainEvent> events, String aggregateType, String aggregateId) {
        events.forEach(event -> saveToOutbox(event, aggregateType, aggregateId));
    }
}