package com.example.demo.domain.permission.aggregate;

import com.example.demo.domain.permission.entity.FieldPermission;
import com.example.demo.domain.permission.entity.RoleDataScope;
import com.example.demo.domain.permission.valueobject.*;
import com.example.demo.domain.shared.common.BaseEntity;
import com.example.demo.domain.shared.exception.DomainException;
import java.util.*;

/**
 * 角色聚合根
 * DDD聚合根，管理角色及其权限配置
 */
public class Role extends BaseEntity<Long> {

    private RoleCode code;
    private String name;
    private Long parentId;
    private RoleStatus status;
    private InheritMode inheritMode;
    private boolean isBuiltin;
    private boolean isDeleted;
    private int version;
    private int sort;
    private Set<RolePermission> permissions;
    private Set<RoleDataScope> dataScopes;
    private List<FieldPermission> fieldPerms;

    /**
     * 工厂方法：创建角色
     */
    public static Role create(RoleCode code, String name, Long parentId, InheritMode mode) {
        Role role = new Role();
        role.code = code;
        role.name = name;
        role.parentId = parentId;
        role.inheritMode = mode != null ? mode : InheritMode.EXTEND;
        role.status = RoleStatus.ENABLED;
        role.isDeleted = false;
        role.isBuiltin = false;
        role.version = 0;
        role.permissions = new HashSet<>();
        role.dataScopes = new HashSet<>();
        role.fieldPerms = new ArrayList<>();
        return role;
    }

    /**
     * 分配权限
     */
    public void assignPermission(Long resourceId, Set<ActionType> actions, PermissionEffect effect) {
        RolePermission perm = new RolePermission(resourceId, actions, effect);
        permissions.add(perm);
    }

    /**
     * 移除权限
     */
    public void removePermission(Long resourceId) {
        permissions.removeIf(p -> p.getResourceId().equals(resourceId));
    }

    /**
     * 启用角色
     */
    public void enable() {
        this.status = RoleStatus.ENABLED;
    }

    /**
     * 禁用角色
     */
    public void disable() {
        if (isBuiltin) {
            throw new DomainException("内置角色不能被禁用");
        }
        this.status = RoleStatus.DISABLED;
    }

    /**
     * 软删除
     */
    public void softDelete() {
        if (isBuiltin) {
            throw new DomainException("内置角色不能被删除");
        }
        this.isDeleted = true;
    }

    /**
     * 递增版本号
     */
    public void incrementVersion() {
        this.version++;
    }

    /**
     * 获取自有权限（不含继承的）
     */
    public Set<RolePermission> getOwnPermissions() {
        return Collections.unmodifiableSet(permissions);
    }

    /**
     * 获取自有数据范围
     */
    public Set<RoleDataScope> getOwnDataScopes() {
        return Collections.unmodifiableSet(dataScopes);
    }

    /**
     * 分配数据范围
     */
    public void assignDataScope(RoleDataScope dataScope) {
        dataScopes.add(dataScope);
    }

    /**
     * 移除数据范围
     */
    public void removeDataScope(String dimensionCode) {
        dataScopes.removeIf(ds -> ds.getDimensionCode().equals(dimensionCode));
    }

    /**
     * 检查是否有DENY权限
     */
    public boolean hasDenyPermissions() {
        return permissions.stream().anyMatch(p -> p.getEffect() == PermissionEffect.DENY);
    }

    /**
     * 获取所有DENY权限
     */
    public List<RolePermission> getDenyPermissions() {
        return permissions.stream()
            .filter(p -> p.getEffect() == PermissionEffect.DENY)
            .toList();
    }

    /**
     * 获取DENY权限位图
     */
    public PermissionBitmap getDenyBitmap() {
        PermissionBitmap bitmap = PermissionBitmap.empty(System.currentTimeMillis());
        for (RolePermission p : getDenyPermissions()) {
            bitmap = bitmap.addPermission(p.getResourceId(), p.getActions());
        }
        return bitmap;
    }

    // Getter/Setter

    @Override
    public Long getId() {
        return super.getId();
    }

    public void setId(Long id) {
        super.setId(id);
    }

    public RoleCode getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public RoleStatus getStatus() {
        return status;
    }

    public void setStatus(RoleStatus status) {
        this.status = status;
    }

    public InheritMode getInheritMode() {
        return inheritMode;
    }

    public void setInheritMode(InheritMode inheritMode) {
        this.inheritMode = inheritMode;
    }

    public boolean isBuiltin() {
        return isBuiltin;
    }

    public void setBuiltin(boolean builtin) {
        this.isBuiltin = builtin;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public List<FieldPermission> getFieldPerms() {
        return Collections.unmodifiableList(fieldPerms);
    }

    public void setFieldPerms(List<FieldPermission> fieldPerms) {
        this.fieldPerms = fieldPerms;
    }

    public void setPermissions(Set<RolePermission> permissions) {
        this.permissions = permissions;
    }

    public void setDataScopes(Set<RoleDataScope> dataScopes) {
        this.dataScopes = dataScopes;
    }
}