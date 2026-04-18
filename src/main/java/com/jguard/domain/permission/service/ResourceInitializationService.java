package com.jguard.domain.permission.service;

import com.jguard.infrastructure.persistence.mapper.DataDimensionMapper;
import com.jguard.infrastructure.persistence.mapper.ResourceMapper;
import com.jguard.infrastructure.persistence.mapper.RoleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 资源初始化校验服务
 * 应用启动时验证所有预置数据是否存在
 */
@Service
public class ResourceInitializationService {

    private static final Logger log = LoggerFactory.getLogger(ResourceInitializationService.class);

    private final RoleMapper roleMapper;
    private final ResourceMapper resourceMapper;
    private final DataDimensionMapper dataDimensionMapper;

    // 预置角色编码
    private static final List<String> PRESET_ROLES = Arrays.asList(
            "SUPER_ADMIN", "ADMIN", "DEPT_MANAGER", "USER"
    );

    // 预置数据维度编码
    private static final List<String> PRESET_DIMENSIONS = Arrays.asList(
            "DEPARTMENT", "PROJECT", "CUSTOMER"
    );

    // 预置菜单资源编码
    private static final List<String> PRESET_MENUS = Arrays.asList(
            "SYSTEM", "USER_MANAGE", "ROLE_MANAGE", "RESOURCE_MANAGE", "PERMISSION"
    );

    public ResourceInitializationService(RoleMapper roleMapper,
                                          ResourceMapper resourceMapper,
                                          DataDimensionMapper dataDimensionMapper) {
        this.roleMapper = roleMapper;
        this.resourceMapper = resourceMapper;
        this.dataDimensionMapper = dataDimensionMapper;
    }

    /**
     * 应用启动后验证预置数据
     */
    @EventListener(ApplicationReadyEvent.class)
    public void verifyPresetData() {
        log.info("开始验证RBAC预置数据...");

        boolean allValid = true;

        // 1. 验证预置角色
        allValid &= verifyPresetRoles();

        // 2. 验证预置数据维度
        allValid &= verifyPresetDimensions();

        // 3. 验证预置菜单资源
        allValid &= verifyPresetMenus();

        if (allValid) {
            log.info("RBAC预置数据验证完成，所有预置数据正常");
        } else {
            log.warn("RBAC预置数据验证完成，部分预置数据缺失，请检查数据库迁移脚本是否执行");
        }
    }

    /**
     * 验证预置角色
     */
    private boolean verifyPresetRoles() {
        log.debug("验证预置角色...");
        boolean allPresent = true;

        for (String roleCode : PRESET_ROLES) {
            var role = roleMapper.findByCode(roleCode);
            if (role == null) {
                log.warn("预置角色缺失: {}", roleCode);
                allPresent = false;
            } else if (!role.isBuiltin()) {
                log.warn("预置角色 {} 未标记为内置角色", roleCode);
                allPresent = false;
            } else {
                log.debug("预置角色验证成功: {} ({})", roleCode, role.getName());
            }
        }

        return allPresent;
    }

    /**
     * 验证预置数据维度
     */
    private boolean verifyPresetDimensions() {
        log.debug("验证预置数据维度...");
        boolean allPresent = true;

        for (String dimensionCode : PRESET_DIMENSIONS) {
            var dimension = dataDimensionMapper.findByCode(dimensionCode);
            if (dimension.isEmpty()) {
                log.warn("预置数据维度缺失: {}", dimensionCode);
                allPresent = false;
            } else {
                log.debug("预置数据维度验证成功: {} ({})", dimensionCode, dimension.get().getName());
            }
        }

        return allPresent;
    }

    /**
     * 验证预置菜单资源
     */
    private boolean verifyPresetMenus() {
        log.debug("验证预置菜单资源...");
        boolean allPresent = true;

        for (String menuCode : PRESET_MENUS) {
            var resource = resourceMapper.findByCode(menuCode);
            if (resource == null) {
                log.warn("预置菜单资源缺失: {}", menuCode);
                allPresent = false;
            } else {
                log.debug("预置菜单资源验证成功: {} ({})", menuCode, resource.getName());
            }
        }

        return allPresent;
    }

    /**
     * 获取预置角色编码列表
     */
    public List<String> getPresetRoleCodes() {
        return PRESET_ROLES;
    }

    /**
     * 获取预置数据维度编码列表
     */
    public List<String> getPresetDimensionCodes() {
        return PRESET_DIMENSIONS;
    }

    /**
     * 获取预置菜单资源编码列表
     */
    public List<String> getPresetMenuCodes() {
        return PRESET_MENUS;
    }
}