package com.example.demo.domain.shared.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 领域事件基类
 * 所有领域事件都需要继承此类
 */
public abstract class DomainEvent {

    private final LocalDateTime occurredOn;
    private final String eventId;

    protected DomainEvent() {
        this.occurredOn = LocalDateTime.now();
        this.eventId = UUID.randomUUID().toString();
    }

    /**
     * 获取事件发生时间
     */
    public LocalDateTime occurredOn() {
        return occurredOn;
    }

    /**
     * 获取事件唯一标识
     */
    public String eventId() {
        return eventId;
    }

    /**
     * 获取事件类型名称，用于 Kafka 消息路由
     */
    public String eventType() {
        return this.getClass().getSimpleName();
    }
}