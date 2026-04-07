package com.example.demo.domain.user.event;

import com.example.demo.domain.shared.event.DomainEvent;
import com.example.demo.domain.user.valueobject.UserId;
import com.example.demo.domain.user.valueobject.UserStatus;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 用户状态变更事件
 */
public class UserStatusChanged extends DomainEvent {

    private final UserId userId;
    private final UserStatus newStatus;

    @JsonCreator
    public UserStatusChanged(
            @JsonProperty("userId") UserId userId,
            @JsonProperty("newStatus") UserStatus newStatus) {
        super();
        this.userId = userId;
        this.newStatus = newStatus;
    }

    @JsonProperty("userId")
    public UserId getUserId() {
        return userId;
    }

    @JsonProperty("newStatus")
    public UserStatus getNewStatus() {
        return newStatus;
    }
}