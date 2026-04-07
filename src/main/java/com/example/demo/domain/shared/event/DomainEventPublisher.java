package com.example.demo.domain.shared.event;

import java.util.List;

/**
 * 领域事件发布接口
 * 定义在领域层，实现在基础设施层（Kafka/Spring Event）
 */
public interface DomainEventPublisher {

    /**
     * 发布单个领域事件
     */
    void publish(DomainEvent event);

    /**
     * 批量发布领域事件
     */
    void publishAll(List<DomainEvent> events);
}