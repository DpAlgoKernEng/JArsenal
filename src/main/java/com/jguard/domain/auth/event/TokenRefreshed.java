package com.jguard.domain.auth.event;

import com.jguard.domain.shared.event.DomainEvent;
import com.jguard.domain.user.valueobject.UserId;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Token 刷新事件
 */
public class TokenRefreshed extends DomainEvent {

    private final UserId userId;

    @JsonCreator
    public TokenRefreshed(@JsonProperty("userId") UserId userId) {
        super();
        this.userId = userId;
    }

    @JsonProperty("userId")
    public UserId getUserId() {
        return userId;
    }
}