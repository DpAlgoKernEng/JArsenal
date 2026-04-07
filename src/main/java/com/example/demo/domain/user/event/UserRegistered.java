package com.example.demo.domain.user.event;

import com.example.demo.domain.shared.event.DomainEvent;
import com.example.demo.domain.user.valueobject.UserId;
import com.example.demo.domain.user.valueobject.Username;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 用户注册成功事件
 */
public class UserRegistered extends DomainEvent {

    private final UserId userId;
    private final Username username;

    @JsonCreator
    public UserRegistered(
            @JsonProperty("userId") UserId userId,
            @JsonProperty("username") Username username) {
        super();
        this.userId = userId;
        this.username = username;
    }

    @JsonProperty("userId")
    public UserId getUserId() {
        return userId;
    }

    @JsonProperty("username")
    public Username getUsername() {
        return username;
    }
}