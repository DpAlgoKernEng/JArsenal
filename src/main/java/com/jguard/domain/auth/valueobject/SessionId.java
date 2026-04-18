package com.jguard.domain.auth.valueobject;

import com.jguard.domain.shared.exception.DomainException;
import java.util.UUID;

/**
 * Session 标识值对象
 */
public class SessionId {

    private final String value;

    public SessionId(String value) {
        if (value == null || value.isEmpty()) {
            throw new DomainException("Session ID 不能为空");
        }
        this.value = value;
    }

    public static SessionId generate() {
        return new SessionId(UUID.randomUUID().toString());
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SessionId other = (SessionId) obj;
        return value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}