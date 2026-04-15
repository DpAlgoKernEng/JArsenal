package com.example.demo.domain.permission.valueobject;

import com.example.demo.domain.permission.exception.DomainException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class RolePermission {
    private final Long resourceId;
    private final Set<ActionType> actions;
    private final PermissionEffect effect;

    public RolePermission(Long resourceId, Set<ActionType> actions, PermissionEffect effect) {
        if (resourceId == null) {
            throw new DomainException("资源ID不能为空");
        }
        if (actions == null || actions.isEmpty()) {
            throw new DomainException("操作集合不能为空");
        }
        this.resourceId = resourceId;
        this.actions = Collections.unmodifiableSet(new HashSet<>(actions));
        this.effect = effect != null ? effect : PermissionEffect.ALLOW;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public Set<ActionType> getActions() {
        return actions;
    }

    public PermissionEffect getEffect() {
        return effect;
    }

    public boolean hasAction(ActionType action) {
        return actions.contains(action);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RolePermission that)) return false;
        return resourceId.equals(that.resourceId);
    }

    @Override
    public int hashCode() {
        return resourceId.hashCode();
    }
}