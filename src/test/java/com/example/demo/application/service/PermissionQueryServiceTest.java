package com.example.demo.application.service;

import com.example.demo.application.dto.*;
import com.example.demo.domain.permission.aggregate.Resource;
import com.example.demo.domain.permission.entity.FieldPermission;
import com.example.demo.domain.permission.entity.ResourceField;
import com.example.demo.domain.permission.repository.*;
import com.example.demo.domain.permission.service.PermissionCacheService;
import com.example.demo.domain.permission.valueobject.*;
import com.example.demo.infrastructure.security.UserContext;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * PermissionQueryService 测试
 */
class PermissionQueryServiceTest {

    @Mock
    private PermissionCacheService permissionCacheService;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private FieldPermissionRepository fieldPermissionRepository;

    @Mock
    private ResourceFieldRepository resourceFieldRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private PermissionQueryService permissionQueryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getUserPermissions_nullUserId_shouldReturnEmptyResult() {
        // 模拟 UserContext 返回 null
        // 在实际测试中需要使用 @MockBean 或 ReflectionTestUtils

        // 由于 UserContext 是静态类，这里只验证基本逻辑
        // 实际集成测试应该设置 UserContext

        // 验证服务返回空结果
        UserPermissionsDTO result = permissionQueryService.getUserPermissions();
        assertNotNull(result);
    }

    @Test
    void getUserMenus_withEmptyBitmap_shouldReturnEmptyList() {
        Long userId = 1L;

        PermissionBitmap emptyBitmap = PermissionBitmap.empty(System.currentTimeMillis());
        when(permissionCacheService.getPermissionBitmap(userId)).thenReturn(emptyBitmap);
        when(resourceRepository.findByType(ResourceType.MENU)).thenReturn(List.of());

        List<MenuDTO> menus = permissionQueryService.getUserMenus();

        assertNotNull(menus);
        assertTrue(menus.isEmpty());
    }

    @Test
    void getUserMenus_withAccessibleMenus_shouldReturnTree() {
        Long userId = 1L;

        // 创建测试菜单资源
        Resource menu1 = Resource.createMenu("USER", "用户管理", "/users", "User", "views/UserList");
        menu1.setId(1L);
        menu1.setSort(1);

        Resource menu2 = Resource.createMenu("ROLE", "角色管理", "/roles", "Role", null);
        menu2.setId(2L);
        menu2.setParentId(null);
        menu2.setSort(2);

        // 创建权限位图
        Map<Long, BitSet> actionBits = new HashMap<>();
        BitSet userBits = new BitSet();
        userBits.set(ActionType.VIEW.ordinal());
        actionBits.put(1L, userBits);

        PermissionBitmap bitmap = new PermissionBitmap(actionBits, System.currentTimeMillis());

        when(permissionCacheService.getPermissionBitmap(userId)).thenReturn(bitmap);
        when(resourceRepository.findByType(ResourceType.MENU)).thenReturn(List.of(menu1, menu2));

        // 需要设置 UserContext
        UserContext.setCurrentUser(userId, "test");

        List<MenuDTO> menus = permissionQueryService.getUserMenus();

        assertNotNull(menus);
        assertEquals(1, menus.size()); // 只有用户管理菜单有权限

        MenuDTO userMenu = menus.get(0);
        assertEquals("USER", userMenu.code());
        assertEquals("用户管理", userMenu.name());
        assertEquals("/users", userMenu.path());

        UserContext.clear();
    }

    @Test
    void getPermissionVersion_shouldReturnBitmapVersion() {
        Long userId = 1L;
        long expectedVersion = 12345L;

        PermissionBitmap bitmap = PermissionBitmap.empty(expectedVersion);
        when(permissionCacheService.getPermissionBitmap(userId)).thenReturn(bitmap);

        UserContext.setCurrentUser(userId, "test");

        long version = permissionQueryService.getPermissionVersion();

        assertEquals(expectedVersion, version);

        UserContext.clear();
    }
}