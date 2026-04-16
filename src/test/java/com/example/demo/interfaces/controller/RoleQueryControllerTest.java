package com.example.demo.interfaces.controller;

import com.example.demo.application.service.RoleApplicationService;
import com.example.demo.domain.permission.aggregate.Role;
import com.example.demo.domain.permission.valueobject.InheritMode;
import com.example.demo.domain.permission.valueobject.RoleCode;
import com.example.demo.domain.permission.valueobject.RoleStatus;
import com.example.demo.interfaces.assembler.RoleAssembler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 角色查询控制器测试
 */
@ExtendWith(MockitoExtension.class)
class RoleQueryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RoleApplicationService roleApplicationService;

    private RoleAssembler roleAssembler;
    private RoleQueryController roleQueryController;

    private Role testRole;

    @BeforeEach
    void setUp() {
        roleAssembler = new RoleAssembler();
        roleQueryController = new RoleQueryController(roleApplicationService, roleAssembler);
        mockMvc = MockMvcBuilders.standaloneSetup(roleQueryController).build();

        testRole = Role.create(new RoleCode("TEST_ROLE"), "测试角色", null, InheritMode.EXTEND);
        ReflectionTestUtils.setField(testRole, "id", 1L);
        testRole.setSort(1);
    }

    @Test
    @DisplayName("查询角色树成功")
    void getRoleTree_success() throws Exception {
        // given
        Role role1 = Role.create(new RoleCode("ROLE_1"), "角色1", null, InheritMode.EXTEND);
        ReflectionTestUtils.setField(role1, "id", 1L);

        Role role2 = Role.create(new RoleCode("ROLE_2"), "角色2", 1L, InheritMode.EXTEND);
        ReflectionTestUtils.setField(role2, "id", 2L);

        when(roleApplicationService.getRoleTree()).thenReturn(Arrays.asList(role1, role2));

        // when & then
        mockMvc.perform(get("/api/v1/roles"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[0].code").value("ROLE_1"))
            .andExpect(jsonPath("$.data[0].children").isArray());
    }

    @Test
    @DisplayName("查询角色树 - 空列表")
    void getRoleTree_empty() throws Exception {
        // given
        when(roleApplicationService.getRoleTree()).thenReturn(Collections.emptyList());

        // when & then
        mockMvc.perform(get("/api/v1/roles"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("根据ID查询角色成功")
    void getRole_success() throws Exception {
        // given
        when(roleApplicationService.getRoleById(1L)).thenReturn(testRole);

        // when & then
        mockMvc.perform(get("/api/v1/roles/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.id").value(1))
            .andExpect(jsonPath("$.data.code").value("TEST_ROLE"))
            .andExpect(jsonPath("$.data.name").value("测试角色"))
            .andExpect(jsonPath("$.data.status").value("ENABLED"));
    }

    @Test
    @DisplayName("根据ID查询角色 - 不存在")
    void getRole_notFound() throws Exception {
        // given
        when(roleApplicationService.getRoleById(999L)).thenReturn(null);

        // when & then
        mockMvc.perform(get("/api/v1/roles/999"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(404))
            .andExpect(jsonPath("$.message").value("角色不存在"));
    }

    @Test
    @DisplayName("查询用户角色列表成功")
    void getUserRoles_success() throws Exception {
        // given
        when(roleApplicationService.getRolesByUserId(1L)).thenReturn(Arrays.asList(testRole));

        // when & then
        mockMvc.perform(get("/api/v1/roles/user/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    @DisplayName("查询用户角色列表 - 无角色")
    void getUserRoles_empty() throws Exception {
        // given
        when(roleApplicationService.getRolesByUserId(1L)).thenReturn(Collections.emptyList());

        // when & then
        mockMvc.perform(get("/api/v1/roles/user/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data").isEmpty());
    }
}