package com.example.demo.domain.permission.valueobject;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 权限位图值对象，用于高效的权限计算
 * 使用BitSet实现O(n)复杂度的权限合并和判断
 */
public final class PermissionBitmap {
    private final Map<Long, BitSet> actionBits;
    private final long version;

    public PermissionBitmap(Map<Long, BitSet> actionBits, long version) {
        this.actionBits = actionBits != null ? new HashMap<>(actionBits) : new HashMap<>();
        this.version = version;
    }

    public static PermissionBitmap empty(long version) {
        return new PermissionBitmap(new HashMap<>(), version);
    }

    public static PermissionBitmap empty() {
        return empty(System.currentTimeMillis());
    }

    /**
     * 位图合并（高效O(n)操作）
     */
    public PermissionBitmap merge(PermissionBitmap other) {
        Map<Long, BitSet> merged = new HashMap<>(this.actionBits);

        other.actionBits.forEach((resource, bits) -> {
            BitSet existing = merged.get(resource);
            if (existing != null) {
                existing.or(bits);
            } else {
                merged.put(resource, (BitSet) bits.clone());
            }
        });

        return new PermissionBitmap(merged, System.currentTimeMillis());
    }

    /**
     * 检查是否有指定操作权限
     */
    public boolean hasAction(Long resourceId, ActionType action) {
        BitSet bits = actionBits.get(resourceId);
        return bits != null && bits.get(action.ordinal());
    }

    /**
     * 处理DENY冲突（DENY优先）
     */
    public PermissionBitmap applyDeny(PermissionBitmap denyBitmap) {
        Map<Long, BitSet> result = new HashMap<>();

        // Deep copy the current actionBits
        this.actionBits.forEach((resource, bits) -> {
            result.put(resource, (BitSet) bits.clone());
        });

        // Apply deny bits
        denyBitmap.actionBits.forEach((resource, denyBits) -> {
            BitSet existing = result.get(resource);
            if (existing != null) {
                existing.andNot(denyBits);
            }
        });

        return new PermissionBitmap(result, System.currentTimeMillis());
    }

    /**
     * 添加权限
     */
    public PermissionBitmap addPermission(Long resourceId, Set<ActionType> actions) {
        Map<Long, BitSet> newBits = new HashMap<>(this.actionBits);
        BitSet bits = newBits.computeIfAbsent(resourceId, k -> new BitSet());
        for (ActionType action : actions) {
            bits.set(action.ordinal());
        }
        return new PermissionBitmap(newBits, System.currentTimeMillis());
    }

    public Map<Long, BitSet> getActionBits() {
        return new HashMap<>(actionBits);
    }

    public long getVersion() {
        return version;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - version > 300000; // 5分钟
    }
}