package com.example.demo.domain.permission.service;

import com.example.demo.domain.permission.aggregate.Resource;
import com.example.demo.domain.permission.aggregate.Role;
import com.example.demo.domain.permission.entity.FieldAccessor;
import com.example.demo.domain.permission.entity.FieldPermission;
import com.example.demo.domain.permission.entity.ResourceField;
import com.example.demo.domain.permission.repository.FieldPermissionRepository;
import com.example.demo.domain.permission.repository.ResourceFieldRepository;
import com.example.demo.domain.permission.repository.ResourceRepository;
import com.example.demo.domain.permission.repository.RoleRepository;
import com.example.demo.domain.permission.valueobject.RoleStatus;
import com.example.demo.domain.permission.valueobject.SensitiveLevel;
import com.example.demo.domain.shared.exception.DomainException;
import com.example.demo.infrastructure.security.UserContext;
import org.springframework.stereotype.Service;
import java.util.*;

/**
 * 字段权限领域服务
 * 负责字段级权限校验和敏感数据脱敏
 */
@Service
public class FieldPermissionDomainService {

    private final ResourceRepository resourceRepository;
    private final ResourceFieldRepository resourceFieldRepository;
    private final FieldPermissionRepository fieldPermissionRepository;
    private final RoleRepository roleRepository;

    public FieldPermissionDomainService(
            ResourceRepository resourceRepository,
            ResourceFieldRepository resourceFieldRepository,
            FieldPermissionRepository fieldPermissionRepository,
            RoleRepository roleRepository) {
        this.resourceRepository = resourceRepository;
        this.resourceFieldRepository = resourceFieldRepository;
        this.fieldPermissionRepository = fieldPermissionRepository;
        this.roleRepository = roleRepository;
    }

    /**
     * 获取用户对资源的字段权限
     */
    public Map<Long, FieldPermission> getFieldPermissions(Long userId, String resourceCode) {
        Resource resource = resourceRepository.findByCode(resourceCode).orElse(null);
        if (resource == null) {
            return Collections.emptyMap();
        }

        return computeUserFieldPermissions(userId, resource.getId());
    }

    /**
     * 检查用户是否可查看特定字段
     */
    public boolean canViewField(Long userId, String resourceCode, String fieldCode) {
        Resource resource = resourceRepository.findByCode(resourceCode).orElse(null);
        if (resource == null) {
            return true; // 资源不存在，默认允许
        }

        ResourceField field = resourceFieldRepository.findByResourceAndCode(resource.getId(), fieldCode)
                .orElse(null);
        if (field == null) {
            return true; // 非敏感字段，默认允许
        }

        // HIDDEN级别字段默认不可查看
        if (field.getSensitiveLevel() == SensitiveLevel.HIDDEN) {
            Map<Long, FieldPermission> perms = computeUserFieldPermissions(userId, resource.getId());
            FieldPermission perm = perms.get(field.getId());
            return perm != null && perm.canView();
        }

        // ENCRYPTED级别默认可查看（脱敏后）
        return true;
    }

    /**
     * 检查用户是否可编辑特定字段
     */
    public boolean canEditField(Long userId, String resourceCode, String fieldCode) {
        Resource resource = resourceRepository.findByCode(resourceCode).orElse(null);
        if (resource == null) {
            return true; // 资源不存在，默认允许
        }

        ResourceField field = resourceFieldRepository.findByResourceAndCode(resource.getId(), fieldCode)
                .orElse(null);
        if (field == null) {
            return true; // 非敏感字段，默认允许
        }

        // 敏感字段需要检查权限
        Map<Long, FieldPermission> perms = computeUserFieldPermissions(userId, resource.getId());
        FieldPermission perm = perms.get(field.getId());
        return perm != null && perm.canEdit();
    }

    /**
     * 处理响应数据的字段权限（脱敏处理）
     */
    public <T> T processFieldPermissions(T response, String resourceCode) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null || response == null) {
            return response;
        }

        Resource resource = resourceRepository.findByCode(resourceCode).orElse(null);
        if (resource == null) {
            return response;
        }

        List<ResourceField> sensitiveFields = resourceFieldRepository.findByResourceId(resource.getId());
        if (sensitiveFields.isEmpty()) {
            return response;
        }

        Map<Long, FieldPermission> userPerms = computeUserFieldPermissions(userId, resource.getId());

        for (ResourceField field : sensitiveFields) {
            FieldPermission perm = userPerms.get(field.getId());

            // 无权限或不可查看时进行脱敏
            if (perm == null || !perm.canView()) {
                try {
                    FieldAccessor accessor = FieldAccessor.get(response.getClass(), field.getFieldCode());
                    Object originalValue = accessor.getValue(response);
                    Object maskedValue = field.maskValue(originalValue);
                    accessor.setValue(response, maskedValue);
                } catch (RuntimeException e) {
                    // 字段不存在，忽略
                }
            }
        }

        return response;
    }

    /**
     * 校验编辑权限
     */
    public void validateEditPermission(Object request, String resourceCode) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new DomainException("UNAUTHORIZED", "用户未登录");
        }

        Resource resource = resourceRepository.findByCode(resourceCode).orElse(null);
        if (resource == null) {
            return;
        }

        List<ResourceField> sensitiveFields = resourceFieldRepository.findByResourceId(resource.getId());
        if (sensitiveFields.isEmpty()) {
            return;
        }

        Map<Long, FieldPermission> userPerms = computeUserFieldPermissions(userId, resource.getId());

        for (ResourceField field : sensitiveFields) {
            FieldPermission perm = userPerms.get(field.getId());

            try {
                FieldAccessor accessor = FieldAccessor.get(request.getClass(), field.getFieldCode());
                Object newValue = accessor.getValue(request);

                // 如果字段有新值且无编辑权限，则拒绝
                if (newValue != null && (perm == null || !perm.canEdit())) {
                    throw new DomainException("FIELD_EDIT_DENIED",
                            "无权限编辑字段: " + field.getFieldName());
                }
            } catch (RuntimeException e) {
                // 字段不存在，忽略
            }
        }
    }

    /**
     * 计算角色的字段权限（含继承链）
     * 子角色继承父角色字段权限，own权限可覆盖继承
     */
    public Map<Long, FieldPermission> computeFieldPermissions(Role role) {
        Map<Long, FieldPermission> result = new HashMap<>();

        // 递归继承父角色字段权限
        if (role.getParentId() != null) {
            Role parent = roleRepository.findById(role.getParentId()).orElse(null);
            if (parent != null && parent.getStatus() == RoleStatus.ENABLED) {
                Map<Long, FieldPermission> parentPerms = computeFieldPermissions(parent);
                result.putAll(parentPerms);
            }
        }

        // 角色 own 字段权限覆盖继承
        List<FieldPermission> roleFieldPerms = fieldPermissionRepository.findByRoleId(role.getId());
        for (FieldPermission fp : roleFieldPerms) {
            result.put(fp.getFieldId(), fp); // 覆盖同fieldId的继承权限
        }

        return result;
    }

    /**
     * 计算用户的字段权限（多角色合并）
     * 多角色权限取宽松策略：任意角色有权限则合并后有权限
     */
    public Map<Long, FieldPermission> computeUserFieldPermissions(Long userId, Long resourceId) {
        List<Role> roles = roleRepository.findRolesByUserId(userId);
        Map<Long, FieldPermission> merged = new HashMap<>();

        for (Role role : roles) {
            if (role.getStatus() != RoleStatus.ENABLED) {
                continue;
            }

            Map<Long, FieldPermission> rolePerms = computeFieldPermissions(role);

            // 合并策略：宽松合并（任意有权限则合并后有权限）
            for (Map.Entry<Long, FieldPermission> entry : rolePerms.entrySet()) {
                Long fieldId = entry.getKey();
                FieldPermission existing = merged.get(fieldId);
                FieldPermission newPerm = entry.getValue();

                if (existing == null) {
                    merged.put(fieldId, newPerm);
                } else {
                    // 合并：取宽松权限
                    boolean mergedCanView = existing.canView() || newPerm.canView();
                    boolean mergedCanEdit = existing.canEdit() || newPerm.canEdit();
                    merged.put(fieldId, FieldPermission.create(role.getId(), fieldId, mergedCanView, mergedCanEdit));
                }
            }
        }

        return merged;
    }
}