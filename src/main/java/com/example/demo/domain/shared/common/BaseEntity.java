package com.example.demo.domain.shared.common;

import com.example.demo.domain.shared.event.DomainEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * 实体基类
 * 提供领域事件注册机制
 */
public abstract class BaseEntity<ID> {

    private ID id;
    private final List<DomainEvent> events = new ArrayList<>();

    /**
     * 获取实体标识
     */
    public ID getId() {
        return id;
    }

    /**
     * 设置实体标识
     */
    protected void setId(ID id) {
        this.id = id;
    }

    /**
     * 注册领域事件
     */
    protected void registerEvent(DomainEvent event) {
        this.events.add(event);
    }

    /**
     * 清除已注册的事件
     */
    public void clearEvents() {
        this.events.clear();
    }

    /**
     * 获取所有已注册的事件
     */
    public List<DomainEvent> events() {
        return new ArrayList<>(this.events);
    }

    /**
     * 判断实体是否相等（基于标识）
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        BaseEntity<?> other = (BaseEntity<?>) obj;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}