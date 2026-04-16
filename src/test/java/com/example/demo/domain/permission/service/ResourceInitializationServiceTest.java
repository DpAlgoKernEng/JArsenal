package com.example.demo.domain.permission.service;

import com.example.demo.infrastructure.persistence.mapper.DataDimensionMapper;
import com.example.demo.infrastructure.persistence.mapper.ResourceMapper;
import com.example.demo.infrastructure.persistence.mapper.RoleMapper;
import com.example.demo.domain.permission.aggregate.DataDimension;
import com.example.demo.domain.permission.aggregate.Resource;
import com.example.demo.domain.permission.aggregate.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ResourceInitializationService 测试
 */
@ExtendWith(MockitoExtension.class)
class ResourceInitializationServiceTest {

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private ResourceMapper resourceMapper;

    @Mock
    private DataDimensionMapper dataDimensionMapper;

    private ResourceInitializationService service;

    @BeforeEach
    void setUp() {
        service = new ResourceInitializationService(roleMapper, resourceMapper, dataDimensionMapper);
    }

    @Test
    @DisplayName("获取预置角色编码列表")
    void getPresetRoleCodes() {
        var codes = service.getPresetRoleCodes();

        assertNotNull(codes);
        assertEquals(4, codes.size());
        assertTrue(codes.contains("SUPER_ADMIN"));
        assertTrue(codes.contains("ADMIN"));
        assertTrue(codes.contains("DEPT_MANAGER"));
        assertTrue(codes.contains("USER"));
    }

    @Test
    @DisplayName("获取预置数据维度编码列表")
    void getPresetDimensionCodes() {
        var codes = service.getPresetDimensionCodes();

        assertNotNull(codes);
        assertEquals(3, codes.size());
        assertTrue(codes.contains("DEPARTMENT"));
        assertTrue(codes.contains("PROJECT"));
        assertTrue(codes.contains("CUSTOMER"));
    }

    @Test
    @DisplayName("获取预置菜单资源编码列表")
    void getPresetMenuCodes() {
        var codes = service.getPresetMenuCodes();

        assertNotNull(codes);
        assertEquals(5, codes.size());
        assertTrue(codes.contains("SYSTEM"));
        assertTrue(codes.contains("USER_MANAGE"));
        assertTrue(codes.contains("ROLE_MANAGE"));
        assertTrue(codes.contains("RESOURCE_MANAGE"));
        assertTrue(codes.contains("PERMISSION"));
    }

    @Test
    @DisplayName("验证预置角色 - 所有角色存在")
    void verifyPresetRoles_allPresent() {
        // 模拟所有预置角色存在且为内置角色
        for (String code : service.getPresetRoleCodes()) {
            Role role = mock(Role.class);
            when(role.isBuiltin()).thenReturn(true);
            when(role.getName()).thenReturn(code + "名称");
            when(roleMapper.findByCode(code)).thenReturn(role);
        }

        service.verifyPresetData();

        // 验证每个角色都被查询
        for (String code : service.getPresetRoleCodes()) {
            verify(roleMapper).findByCode(code);
        }
    }

    @Test
    @DisplayName("验证预置角色 - 角色缺失")
    void verifyPresetRoles_missingRole() {
        // 模拟部分角色缺失
        when(roleMapper.findByCode("SUPER_ADMIN")).thenReturn(null);
        Role adminRole = mock(Role.class);
        when(adminRole.isBuiltin()).thenReturn(true);
        when(roleMapper.findByCode("ADMIN")).thenReturn(adminRole);

        service.verifyPresetData();

        verify(roleMapper).findByCode("SUPER_ADMIN");
        verify(roleMapper).findByCode("ADMIN");
    }

    @Test
    @DisplayName("验证预置数据维度 - 所有维度存在")
    void verifyPresetDimensions_allPresent() {
        // 模拟所有预置维度存在
        for (String code : service.getPresetDimensionCodes()) {
            DataDimension dimension = new DataDimension();
            dimension.setCode(code);
            dimension.setName(code + "名称");
            when(dataDimensionMapper.findByCode(code)).thenReturn(Optional.of(dimension));
        }

        // 模拟角色
        for (String code : service.getPresetRoleCodes()) {
            Role role = mock(Role.class);
            when(role.isBuiltin()).thenReturn(true);
            when(roleMapper.findByCode(code)).thenReturn(role);
        }

        // 模拟菜单
        for (String code : service.getPresetMenuCodes()) {
            Resource resource = mock(Resource.class);
            when(resource.getName()).thenReturn(code + "名称");
            when(resourceMapper.findByCode(code)).thenReturn(resource);
        }

        service.verifyPresetData();

        for (String code : service.getPresetDimensionCodes()) {
            verify(dataDimensionMapper).findByCode(code);
        }
    }

    @Test
    @DisplayName("验证预置菜单资源 - 所有菜单存在")
    void verifyPresetMenus_allPresent() {
        // 模拟所有预置菜单存在
        for (String code : service.getPresetMenuCodes()) {
            Resource resource = mock(Resource.class);
            when(resource.getName()).thenReturn(code + "名称");
            when(resourceMapper.findByCode(code)).thenReturn(resource);
        }

        // 模拟角色
        for (String code : service.getPresetRoleCodes()) {
            Role role = mock(Role.class);
            when(role.isBuiltin()).thenReturn(true);
            when(roleMapper.findByCode(code)).thenReturn(role);
        }

        // 模拟维度
        for (String code : service.getPresetDimensionCodes()) {
            DataDimension dimension = new DataDimension();
            dimension.setCode(code);
            when(dataDimensionMapper.findByCode(code)).thenReturn(Optional.of(dimension));
        }

        service.verifyPresetData();

        for (String code : service.getPresetMenuCodes()) {
            verify(resourceMapper).findByCode(code);
        }
    }
}