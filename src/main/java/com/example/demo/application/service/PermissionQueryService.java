package com.example.demo.application.service;

import com.example.demo.application.dto.ActionPermissionDTO;
import com.example.demo.application.dto.FieldPermissionDTO;
import com.example.demo.application.dto.MenuDTO;
import com.example.demo.application.dto.UserPermissionsDTO;
import com.example.demo.domain.permission.aggregate.Resource;
import com.example.demo.domain.permission.entity.FieldPermission;
import com.example.demo.domain.permission.entity.ResourceField;
import com.example.demo.domain.permission.repository.FieldPermissionRepository;
import com.example.demo.domain.permission.repository.ResourceFieldRepository;
import com.example.demo.domain.permission.repository.ResourceRepository;
import com.example.demo.domain.permission.repository.RoleRepository;
import com.example.demo.domain.permission.service.PermissionCacheService;
import com.example.demo.domain.permission.valueobject.ActionType;
import com.example.demo.domain.permission.valueobject.PermissionBitmap;
import com.example.demo.domain.permission.valueobject.ResourceType;
import com.example.demo.infrastructure.security.UserContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 权限查询服务
 * 用于前端权限系统
 */
@Service
@Transactional(readOnly = true)
public class PermissionQueryService {

    private final PermissionCacheService permissionCacheService;
    private final ResourceRepository resourceRepository;
    private final FieldPermissionRepository fieldPermissionRepository;
    private final ResourceFieldRepository resourceFieldRepository;
    private final RoleRepository roleRepository;

    public PermissionQueryService(PermissionCacheService permissionCacheService,
                                   ResourceRepository resourceRepository,
                                   FieldPermissionRepository fieldPermissionRepository,
                                   ResourceFieldRepository resourceFieldRepository,
                                   RoleRepository roleRepository) {
        this.permissionCacheService = permissionCacheService;
        this.resourceRepository = resourceRepository;
        this.fieldPermissionRepository = fieldPermissionRepository;
        this.resourceFieldRepository = resourceFieldRepository;
        this.roleRepository = roleRepository;
    }

    /**
     * 获取当前用户完整权限信息
     */
    public UserPermissionsDTO getUserPermissions() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return new UserPermissionsDTO(List.of(), List.of(), List.of(), 0L);
        }

        PermissionBitmap bitmap = permissionCacheService.getPermissionBitmap(userId);

        List<MenuDTO> menus = getUserMenus(bitmap);
        List<ActionPermissionDTO> actions = getUserActions(bitmap);
        List<FieldPermissionDTO> fields = getUserFields(userId, bitmap);

        return new UserPermissionsDTO(menus, actions, fields, bitmap.getVersion());
    }

    /**
     * 获取用户菜单（树结构）
     */
    public List<MenuDTO> getUserMenus() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return List.of();
        }

        PermissionBitmap bitmap = permissionCacheService.getPermissionBitmap(userId);
        return getUserMenus(bitmap);
    }

    /**
     * 获取用户操作权限
     */
    public List<ActionPermissionDTO> getUserActions() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return List.of();
        }

        PermissionBitmap bitmap = permissionCacheService.getPermissionBitmap(userId);
        return getUserActions(bitmap);
    }

    /**
     * 获取用户字段权限
     */
    public List<FieldPermissionDTO> getUserFields() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return List.of();
        }

        PermissionBitmap bitmap = permissionCacheService.getPermissionBitmap(userId);
        return getUserFields(userId, bitmap);
    }

    /**
     * 获取权限版本号
     */
    public long getPermissionVersion() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return 0L;
        }

        PermissionBitmap bitmap = permissionCacheService.getPermissionBitmap(userId);
        return bitmap.getVersion();
    }

    /**
     * 获取指定用户完整权限信息（管理员查看用户权限）
     */
    public UserPermissionsDTO getUserPermissionsByUserId(Long userId) {
        if (userId == null) {
            return new UserPermissionsDTO(List.of(), List.of(), List.of(), 0L);
        }

        PermissionBitmap bitmap = permissionCacheService.getPermissionBitmap(userId);

        List<MenuDTO> menus = getUserMenus(bitmap);
        List<ActionPermissionDTO> actions = getUserActions(bitmap);
        List<FieldPermissionDTO> fields = getUserFields(userId, bitmap);

        return new UserPermissionsDTO(menus, actions, fields, bitmap.getVersion());
    }

    /**
     * 获取指定用户的角色列表
     */
    public List<String> getUserRoles(Long userId) {
        return roleRepository.findRoleCodesByUserId(userId);
    }

    /**
     * 构建菜单树结构
     */
    private List<MenuDTO> getUserMenus(PermissionBitmap bitmap) {
        // 获取所有菜单类型资源
        List<Resource> menuResources = resourceRepository.findByType(ResourceType.MENU);

        // 过滤出用户有VIEW权限的菜单
        List<Resource> accessibleMenus = menuResources.stream()
            .filter(menu -> bitmap.hasAction(menu.getId(), ActionType.VIEW))
            .filter(Resource::isStatus)
            .sorted(Comparator.comparingInt(Resource::getSort))
            .collect(Collectors.toList());

        // 构建树结构
        return buildMenuTree(accessibleMenus, null);
    }

    /**
     * 递归构建菜单树
     */
    private List<MenuDTO> buildMenuTree(List<Resource> resources, Long parentId) {
        return resources.stream()
            .filter(r -> Objects.equals(r.getParentId(), parentId))
            .map(r -> new MenuDTO(
                r.getId(),
                r.getCode(),
                r.getName(),
                r.getPath(),
                r.getIcon(),
                r.getComponent(),
                r.getSort(),
                r.getParentId(),
                buildMenuTree(resources, r.getId())
            ))
            .collect(Collectors.toList());
    }

    /**
     * 获取用户操作权限列表
     */
    private List<ActionPermissionDTO> getUserActions(PermissionBitmap bitmap) {
        // 获取所有操作类型资源
        List<Resource> operationResources = resourceRepository.findByType(ResourceType.OPERATION);

        // 按父资源分组（父资源通常是菜单）
        Map<Long, List<Resource>> operationsByParent = operationResources.stream()
            .filter(Resource::isStatus)
            .collect(Collectors.groupingBy(Resource::getParentId));

        // 查找所有父资源（菜单）的code映射
        Map<Long, String> resourceCodeMap = resourceRepository.findAll().stream()
            .filter(r -> r.getCode() != null)
            .collect(Collectors.toMap(Resource::getId, Resource::getCode));

        // 构建操作权限DTO
        List<ActionPermissionDTO> result = new ArrayList<>();

        for (Map.Entry<Long, BitSet> entry : bitmap.getActionBits().entrySet()) {
            Long resourceId = entry.getKey();
            BitSet bits = entry.getValue();

            // 检查这个资源是否是操作类型
            Optional<Resource> resourceOpt = operationResources.stream()
                .filter(r -> r.getId().equals(resourceId))
                .findFirst();

            if (resourceOpt.isPresent()) {
                Resource resource = resourceOpt.get();
                List<String> actions = new ArrayList<>();

                for (ActionType action : ActionType.values()) {
                    if (bits.get(action.ordinal())) {
                        actions.add(action.name());
                    }
                }

                if (!actions.isEmpty()) {
                    // 使用父资源的code作为resourceCode
                    String resourceCode = resource.getParentId() != null
                        ? resourceCodeMap.getOrDefault(resource.getParentId(), resource.getCode())
                        : resource.getCode();
                    result.add(new ActionPermissionDTO(resourceCode, actions));
                }
            }
        }

        return result;
    }

    /**
     * 获取用户字段权限
     */
    private List<FieldPermissionDTO> getUserFields(Long userId, PermissionBitmap bitmap) {
        List<FieldPermissionDTO> result = new ArrayList<>();

        // 获取用户有权限的资源（通过位图）
        Set<Long> accessibleResourceIds = bitmap.getActionBits().keySet();

        // 查找这些资源的敏感字段
        Map<Long, String> resourceCodeMap = resourceRepository.findAll().stream()
            .filter(r -> r.getCode() != null)
            .collect(Collectors.toMap(Resource::getId, Resource::getCode));

        for (Long resourceId : accessibleResourceIds) {
            String resourceCode = resourceCodeMap.get(resourceId);
            if (resourceCode == null) continue;

            List<ResourceField> fields = resourceFieldRepository.findByResourceId(resourceId);

            // 获取该用户在该资源上的字段权限（按用户过滤）
            List<FieldPermission> userPermissions = fieldPermissionRepository.findByUserIdAndResourceId(userId, resourceId);

            for (ResourceField field : fields) {
                // 查找用户对该字段的权限
                Optional<FieldPermission> userPerm = userPermissions.stream()
                    .filter(p -> p.getFieldId().equals(field.getId()))
                    .findFirst();

                // 如果没有显式权限，根据敏感级别决定默认权限
                boolean canView = userPerm.map(FieldPermission::canView).orElse(false);
                boolean canEdit = userPerm.map(FieldPermission::canEdit).orElse(false);

                result.add(new FieldPermissionDTO(resourceCode, field.getFieldCode(), canView, canEdit));
            }
        }

        return result;
    }
}