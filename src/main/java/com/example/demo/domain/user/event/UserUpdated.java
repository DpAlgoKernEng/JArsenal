package com.example.demo.domain.user.event;

import com.example.demo.domain.shared.event.DomainEvent;
import com.example.demo.domain.user.valueobject.UserId;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 用户信息更新事件
 */
public class UserUpdated extends DomainEvent {

    private final UserId userId;

    @JsonCreator
    public UserUpdated(@JsonProperty("userId") UserId userId) {
        super();
        this.userId = userId;
    }

    @JsonProperty("userId")
    public UserId getUserId() {
        return userId;
    }
}