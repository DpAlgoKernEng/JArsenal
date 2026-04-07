package com.example.demo.domain.auth.valueobject;

import com.example.demo.domain.shared.exception.DomainException;
import java.util.UUID;

/**
 * Token 标识值对象
 */
public class TokenId {

    private final String value;

    public TokenId(String value) {
        if (value == null || value.isEmpty()) {
            throw new DomainException("Token ID 不能为空");
        }
        this.value = value;
    }

    public static TokenId generate() {
        return new TokenId(UUID.randomUUID().toString());
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TokenId other = (TokenId) obj;
        return value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}