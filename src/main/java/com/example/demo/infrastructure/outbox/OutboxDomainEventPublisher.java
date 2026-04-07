package com.example.demo.infrastructure.outbox;

import com.example.demo.domain.shared.event.DomainEvent;
import com.example.demo.domain.shared.event.DomainEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 基于 Outbox 的领域事件发布器
 * 事件先保存到数据库，再由定时任务发布到 Kafka
 */
@Slf4j
@Component
public class OutboxDomainEventPublisher implements DomainEventPublisher {

    private final OutboxEventPublisher outboxEventPublisher;
    private final ThreadLocal<String> currentAggregateType = new ThreadLocal<>();
    private final ThreadLocal<String> currentAggregateId = new ThreadLocal<>();

    public OutboxDomainEventPublisher(OutboxEventPublisher outboxEventPublisher) {
        this.outboxEventPublisher = outboxEventPublisher;
    }

    /**
     * 设置当前聚合信息（在调用 publish 前设置）
     */
    public void setAggregateContext(String aggregateType, String aggregateId) {
        currentAggregateType.set(aggregateType);
        currentAggregateId.set(aggregateId);
    }

    /**
     * 清除聚合上下文
     */
    public void clearAggregateContext() {
        currentAggregateType.remove();
        currentAggregateId.remove();
    }

    @Override
    public void publish(DomainEvent event) {
        String aggregateType = currentAggregateType.get();
        String aggregateId = currentAggregateId.get();

        if (aggregateType == null || aggregateId == null) {
            log.warn("Aggregate context not set, using defaults");
            aggregateType = "Unknown";
            aggregateId = "Unknown";
        }

        outboxEventPublisher.saveToOutbox(event, aggregateType, aggregateId);
    }

    @Override
    public void publishAll(List<DomainEvent> events) {
        String aggregateType = currentAggregateType.get();
        String aggregateId = currentAggregateId.get();

        if (aggregateType == null || aggregateId == null) {
            log.warn("Aggregate context not set, using defaults");
            aggregateType = "Unknown";
            aggregateId = "Unknown";
        }

        outboxEventPublisher.saveAllToOutbox(events, aggregateType, aggregateId);
    }
}