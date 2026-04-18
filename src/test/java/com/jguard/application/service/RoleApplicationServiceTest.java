package com.jguard.application.service;

import com.jguard.application.command.AssignPermissionCommand;
import com.jguard.application.command.AssignRoleCommand;
import com.jguard.application.command.CreateRoleCommand;
import com.jguard.application.command.UpdateRoleCommand;
import com.jguard.domain.permission.aggregate.Role;
import com.jguard.domain.permission.entity.Permission;
import com.jguard.domain.permission.repository.PermissionRepository;
import com.jguard.domain.permission.repository.RoleRepository;
import com.jguard.domain.permission.repository.UserRoleRepository;
import com.jguard.domain.permission.valueobject.*;
import com.jguard.domain.shared.exception.DomainException;
import com.jguard.infrastructure.persistence.mapper.PermissionActionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 角色应用服务测试
 */
@ExtendWith(MockitoExtension.class)
class RoleApplicationServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private PermissionActionMapper permissionActionMapper;

    private RoleApplicationService roleApplicationService;

    private Role testRole;

    @BeforeEach
    void setUp() {
        roleApplicationService = new RoleApplicationService(
            roleRepository, permissionRepository, userRoleRepository, permissionActionMapper
        );

        testRole = Role.create(new RoleCode("TEST_ROLE"), "测试角色", null, InheritMode.EXTEND);
        ReflectionTestUtils.setField(testRole, "id", 1L);
        testRole.setSort(1);
    }

    @Test
    @DisplayName("创建角色成功 - 返回角色ID")
    void createRole_success_shouldReturnRoleId() {
        // given
        CreateRoleCommand command = new CreateRoleCommand("NEW_ROLE", "新角色", null, "EXTEND", 1);

        when(roleRepository.findByCode("NEW_ROLE")).thenReturn(Optional.empty());
        doAnswer((Answer<Role>) invocation -> {
            Role role = invocation.getArgument(0);
            ReflectionTestUtils.setField(role, "id", 100L);
            return role;
        }).when(roleRepository).save(any(Role.class));

        // when
        Long roleId = roleApplicationService.createRole(command);

        // then
        assertNotNull(roleId);
        assertEquals(100L, roleId);
        verify(roleRepository).findByCode("NEW_ROLE");
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    @DisplayName("创建角色失败 - 角色编码已存在")
    void createRole_duplicateCode_shouldThrowException() {
        // given
        CreateRoleCommand command = new CreateRoleCommand("EXISTING_ROLE", "角色", null, "EXTEND", 1);

        Role existingRole = Role.create(new RoleCode("EXISTING_ROLE"), "已存在角色", null, InheritMode.EXTEND);
        ReflectionTestUtils.setField(existingRole, "id", 1L);
        when(roleRepository.findByCode("EXISTING_ROLE")).thenReturn(Optional.of(existingRole));

        // when & then
        assertThrows(DomainException.class, () -> roleApplicationService.createRole(command));
        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("创建角色失败 - 父角色不存在")
    void createRole_parentNotFound_shouldThrowException() {
        // given
        CreateRoleCommand command = new CreateRoleCommand("NEW_ROLE", "新角色", 999L, "EXTEND", 1);

        when(roleRepository.findByCode("NEW_ROLE")).thenReturn(Optional.empty());
        when(roleRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(DomainException.class, () -> roleApplicationService.createRole(command));
        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("创建角色成功 - 有父角色")
    void createRole_withParent_shouldCreateRole() {
        // given
        CreateRoleCommand command = new CreateRoleCommand("CHILD_ROLE", "子角色", 1L, "EXTEND", 2);

        Role parentRole = Role.create(new RoleCode("PARENT_ROLE"), "父角色", null, InheritMode.EXTEND);
        ReflectionTestUtils.setField(parentRole, "id", 1L);

        when(roleRepository.findByCode("CHILD_ROLE")).thenReturn(Optional.empty());
        when(roleRepository.findById(1L)).thenReturn(Optional.of(parentRole));
        doAnswer((Answer<Role>) invocation -> {
            Role role = invocation.getArgument(0);
            ReflectionTestUtils.setField(role, "id", 2L);
            return role;
        }).when(roleRepository).save(any(Role.class));

        // when
        Long roleId = roleApplicationService.createRole(command);

        // then
        assertNotNull(roleId);
        assertEquals(2L, roleId);
        verify(roleRepository).findById(1L);
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    @DisplayName("更新角色成功 - 更新名称")
    void updateRole_updateName_shouldUpdate() {
        // given
        UpdateRoleCommand command = new UpdateRoleCommand(1L, "新名称", null, null, null, null);

        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);

        // when
        roleApplicationService.updateRole(command);

        // then
        verify(roleRepository).findById(1L);
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    @DisplayName("更新角色成功 - 启用角色")
    void updateRole_enableRole_shouldUpdate() {
        // given
        testRole.disable();
        UpdateRoleCommand command = new UpdateRoleCommand(1L, null, null, null, "ENABLED", null);

        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);

        // when
        roleApplicationService.updateRole(command);

        // then
        assertEquals(RoleStatus.ENABLED, testRole.getStatus());
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    @DisplayName("更新角色成功 - 禁用角色")
    void updateRole_disableRole_shouldUpdate() {
        // given
        UpdateRoleCommand command = new UpdateRoleCommand(1L, null, null, null, "DISABLED", null);

        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);

        // when
        roleApplicationService.updateRole(command);

        // then
        assertEquals(RoleStatus.DISABLED, testRole.getStatus());
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    @DisplayName("更新角色失败 - 角色不存在")
    void updateRole_notFound_shouldThrowException() {
        // given
        UpdateRoleCommand command = new UpdateRoleCommand(999L, "新名称", null, null, null, null);

        when(roleRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(DomainException.class, () -> roleApplicationService.updateRole(command));
        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("更新角色失败 - 循环继承")
    void updateRole_circularInheritance_shouldThrowException() {
        // given
        // 设置父角色
        Role parentRole = Role.create(new RoleCode("PARENT_ROLE"), "父角色", 2L, InheritMode.EXTEND);
        ReflectionTestUtils.setField(parentRole, "id", 1L);

        // 当前角色
        Role currentRole = Role.create(new RoleCode("CURRENT_ROLE"), "当前角色", 1L, InheritMode.EXTEND);
        ReflectionTestUtils.setField(currentRole, "id", 2L);

        UpdateRoleCommand command = new UpdateRoleCommand(2L, null, 1L, null, null, null);

        when(roleRepository.findById(1L)).thenReturn(Optional.of(parentRole));
        when(roleRepository.findById(2L)).thenReturn(Optional.of(currentRole));

        // when & then
        assertThrows(DomainException.class, () -> roleApplicationService.updateRole(command));
    }

    @Test
    @DisplayName("删除角色成功")
    void deleteRole_success_shouldSoftDelete() {
        // given
        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(userRoleRepository.findUserIdsByRoleId(1L)).thenReturn(Collections.emptyList());
        when(roleRepository.findByParentId(1L)).thenReturn(Collections.emptyList());
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);

        // when
        roleApplicationService.deleteRole(1L);

        // then
        assertTrue(testRole.isDeleted());
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    @DisplayName("删除角色失败 - 角色正在被使用")
    void deleteRole_inUse_shouldThrowException() {
        // given
        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(userRoleRepository.findUserIdsByRoleId(1L)).thenReturn(Arrays.asList(1L, 2L));

        // when & then
        assertThrows(DomainException.class, () -> roleApplicationService.deleteRole(1L));
        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("删除角色失败 - 存在子角色")
    void deleteRole_hasChildren_shouldThrowException() {
        // given
        Role childRole = Role.create(new RoleCode("CHILD_ROLE"), "子角色", 1L, InheritMode.EXTEND);
        ReflectionTestUtils.setField(childRole, "id", 2L);

        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(userRoleRepository.findUserIdsByRoleId(1L)).thenReturn(Collections.emptyList());
        when(roleRepository.findByParentId(1L)).thenReturn(Arrays.asList(childRole));

        // when & then
        assertThrows(DomainException.class, () -> roleApplicationService.deleteRole(1L));
        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("查询角色成功")
    void getRoleById_success_shouldReturnRole() {
        // given
        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));

        // when
        Role result = roleApplicationService.getRoleById(1L);

        // then
        assertNotNull(result);
        assertEquals("TEST_ROLE", result.getCode().value());
    }

    @Test
    @DisplayName("查询角色失败 - 不存在")
    void getRoleById_notFound_shouldReturnNull() {
        // given
        when(roleRepository.findById(999L)).thenReturn(Optional.empty());

        // when
        Role result = roleApplicationService.getRoleById(999L);

        // then
        assertNull(result);
    }

    @Test
    @DisplayName("查询角色列表成功")
    void listAllRoles_success_shouldReturnList() {
        // given
        Role role1 = Role.create(new RoleCode("ROLE_1"), "角色1", null, InheritMode.EXTEND);
        Role role2 = Role.create(new RoleCode("ROLE_2"), "角色2", null, InheritMode.EXTEND);

        when(roleRepository.findAllNotDeleted()).thenReturn(Arrays.asList(role1, role2));

        // when
        List<Role> result = roleApplicationService.listAllRoles();

        // then
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("分配权限成功 - 新权限")
    void assignPermission_newPermission_shouldCreate() {
        // given
        AssignPermissionCommand command = new AssignPermissionCommand(1L, 10L,
            Arrays.asList("VIEW", "UPDATE"), "ALLOW");

        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(permissionRepository.findByRoleAndResource(1L, 10L)).thenReturn(null);

        Permission newPermission = Permission.create(1L, 10L, PermissionEffect.ALLOW);
        ReflectionTestUtils.setField(newPermission, "id", 100L);

        doAnswer((Answer<Permission>) invocation -> {
            Permission perm = invocation.getArgument(0);
            ReflectionTestUtils.setField(perm, "id", 100L);
            return perm;
        }).when(permissionRepository).save(any(Permission.class));

        when(permissionActionMapper.batchInsert(anyLong(), anyList())).thenReturn(2);
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);

        // when
        roleApplicationService.assignPermission(command);

        // then
        verify(permissionRepository).save(any(Permission.class));
        verify(permissionActionMapper).batchInsert(eq(100L), anyList());
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    @DisplayName("分配权限成功 - 更新已有权限")
    void assignPermission_existingPermission_shouldUpdate() {
        // given
        AssignPermissionCommand command = new AssignPermissionCommand(1L, 10L,
            Arrays.asList("VIEW", "DELETE"), "ALLOW");

        Permission existingPermission = Permission.create(1L, 10L, PermissionEffect.ALLOW);
        ReflectionTestUtils.setField(existingPermission, "id", 100L);

        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(permissionRepository.findByRoleAndResource(1L, 10L)).thenReturn(existingPermission);
        when(permissionActionMapper.deleteByPermissionId(100L)).thenReturn(1);
        when(permissionActionMapper.batchInsert(anyLong(), anyList())).thenReturn(2);
        when(permissionRepository.save(any(Permission.class))).thenReturn(existingPermission);
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);

        // when
        roleApplicationService.assignPermission(command);

        // then
        verify(permissionActionMapper).deleteByPermissionId(100L);
        verify(permissionActionMapper).batchInsert(eq(100L), anyList());
        verify(permissionRepository).save(any(Permission.class));
    }

    @Test
    @DisplayName("分配角色给用户成功")
    void assignRolesToUser_success_shouldAssign() {
        // given
        AssignRoleCommand command = new AssignRoleCommand(1L, Arrays.asList(1L, 2L));

        Role role1 = Role.create(new RoleCode("ROLE_1"), "角色1", null, InheritMode.EXTEND);
        ReflectionTestUtils.setField(role1, "id", 1L);
        Role role2 = Role.create(new RoleCode("ROLE_2"), "角色2", null, InheritMode.EXTEND);
        ReflectionTestUtils.setField(role2, "id", 2L);

        when(roleRepository.findById(1L)).thenReturn(Optional.of(role1));
        when(roleRepository.findById(2L)).thenReturn(Optional.of(role2));
        // 用户已有角色1
        when(userRoleRepository.findRoleIdsByUserId(1L)).thenReturn(Arrays.asList(1L));

        // when
        roleApplicationService.assignRolesToUser(command);

        // then
        // 角色1已存在，不会被移除和重新添加
        // 角色2是新添加的
        verify(userRoleRepository, never()).removeRole(1L, 1L); // 角色1已存在，不移除
        verify(userRoleRepository).assignRole(1L, 2L); // 添加新角色2
    }

    @Test
    @DisplayName("分配角色给用户失败 - 角色已禁用")
    void assignRolesToUser_disabledRole_shouldThrowException() {
        // given
        AssignRoleCommand command = new AssignRoleCommand(1L, Arrays.asList(1L));

        testRole.disable();
        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));

        // when & then
        assertThrows(DomainException.class, () -> roleApplicationService.assignRolesToUser(command));
    }

    @Test
    @DisplayName("移除用户角色成功")
    void removeRoleFromUser_success_shouldRemove() {
        // given
        doNothing().when(userRoleRepository).removeRole(1L, 1L);

        // when
        roleApplicationService.removeRoleFromUser(1L, 1L);

        // then
        verify(userRoleRepository).removeRole(1L, 1L);
    }

    @Test
    @DisplayName("获取用户角色ID列表成功")
    void getUserRoleIds_success_shouldReturnList() {
        // given
        when(userRoleRepository.findRoleIdsByUserId(1L)).thenReturn(Arrays.asList(1L, 2L, 3L));

        // when
        List<Long> result = roleApplicationService.getUserRoleIds(1L);

        // then
        assertEquals(3, result.size());
        assertTrue(result.contains(1L));
        assertTrue(result.contains(2L));
        assertTrue(result.contains(3L));
    }
}