package com.example.demo.domain.auth.event;

import com.example.demo.domain.shared.event.DomainEvent;
import com.example.demo.domain.user.valueobject.UserId;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 用户登出事件
 */
public class UserLoggedOut extends DomainEvent {

    private final UserId userId;

    @JsonCreator
    public UserLoggedOut(@JsonProperty("userId") UserId userId) {
        super();
        this.userId = userId;
    }

    @JsonProperty("userId")
    public UserId getUserId() {
        return userId;
    }
}