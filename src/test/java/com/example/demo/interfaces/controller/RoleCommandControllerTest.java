package com.example.demo.interfaces.controller;

import com.example.demo.application.service.RoleApplicationService;
import com.example.demo.domain.permission.aggregate.Role;
import com.example.demo.domain.permission.valueobject.InheritMode;
import com.example.demo.domain.permission.valueobject.RoleCode;
import com.example.demo.domain.permission.valueobject.RoleStatus;
import com.example.demo.interfaces.assembler.RoleAssembler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 角色命令控制器测试
 */
@ExtendWith(MockitoExtension.class)
class RoleCommandControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private RoleApplicationService roleApplicationService;

    private RoleAssembler roleAssembler;
    private RoleCommandController roleCommandController;

    private Role testRole;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        roleAssembler = new RoleAssembler();
        roleCommandController = new RoleCommandController(roleApplicationService, roleAssembler);
        mockMvc = MockMvcBuilders.standaloneSetup(roleCommandController).build();

        testRole = Role.create(new RoleCode("TEST_ROLE"), "测试角色", null, InheritMode.EXTEND);
        ReflectionTestUtils.setField(testRole, "id", 1L);
        testRole.setSort(1);
    }

    @Test
    @DisplayName("创建角色成功")
    void createRole_success() throws Exception {
        // given
        String requestBody = """
            {
                "code": "NEW_ROLE",
                "name": "新角色",
                "parentId": null,
                "inheritMode": "EXTEND",
                "sort": 1
            }
            """;

        when(roleApplicationService.createRole(any())).thenReturn(1L);
        when(roleApplicationService.getRoleById(1L)).thenReturn(testRole);

        // when & then
        mockMvc.perform(post("/api/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.id").value(1))
            .andExpect(jsonPath("$.data.code").value("TEST_ROLE"));
    }

    @Test
    @DisplayName("创建角色失败 - 缺少必填字段")
    void createRole_missingFields() throws Exception {
        // given
        String requestBody = """
            {
                "name": "新角色"
            }
            """;

        // when & then
        mockMvc.perform(post("/api/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("更新角色成功")
    void updateRole_success() throws Exception {
        // given
        String requestBody = """
            {
                "name": "更新后的名称",
                "parentId": null,
                "sort": 2
            }
            """;

        doNothing().when(roleApplicationService).updateRole(any());
        when(roleApplicationService.getRoleById(1L)).thenReturn(testRole);

        // when & then
        mockMvc.perform(put("/api/roles/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("删除角色成功")
    void deleteRole_success() throws Exception {
        // given
        doNothing().when(roleApplicationService).deleteRole(1L);

        // when & then
        mockMvc.perform(delete("/api/roles/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        verify(roleApplicationService).deleteRole(1L);
    }

    @Test
    @DisplayName("分配权限成功")
    void assignPermission_success() throws Exception {
        // given
        String requestBody = """
            {
                "resourceId": 10,
                "actions": ["VIEW", "UPDATE"],
                "effect": "ALLOW"
            }
            """;

        doNothing().when(roleApplicationService).assignPermission(any());

        // when & then
        mockMvc.perform(post("/api/roles/1/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        verify(roleApplicationService).assignPermission(any());
    }

    @Test
    @DisplayName("分配权限失败 - 缺少必填字段")
    void assignPermission_missingFields() throws Exception {
        // given
        String requestBody = """
            {
                "resourceId": 10
            }
            """;

        // when & then
        mockMvc.perform(post("/api/roles/1/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("分配角色给用户成功")
    void assignRolesToUser_success() throws Exception {
        // given
        String requestBody = """
            {
                "roleIds": [1, 2, 3]
            }
            """;

        doNothing().when(roleApplicationService).assignRolesToUser(any());

        // when & then
        mockMvc.perform(post("/api/roles/user/1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        verify(roleApplicationService).assignRolesToUser(any());
    }

    @Test
    @DisplayName("分配角色给用户失败 - 空角色列表")
    void assignRolesToUser_emptyList() throws Exception {
        // given
        String requestBody = """
            {
                "roleIds": []
            }
            """;

        // when & then
        mockMvc.perform(post("/api/roles/user/1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("移除用户角色成功")
    void removeRoleFromUser_success() throws Exception {
        // given
        doNothing().when(roleApplicationService).removeRoleFromUser(1L, 1L);

        // when & then
        mockMvc.perform(delete("/api/roles/user/1/roles/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        verify(roleApplicationService).removeRoleFromUser(1L, 1L);
    }
}