package com.jguard.domain.permission.valueobject;

import com.jguard.domain.shared.exception.DomainException;

public record RoleId(Long value) {

    public RoleId {
        if (value == null || value <= 0) {
            throw new DomainException("角色ID必须为正整数");
        }
    }
}