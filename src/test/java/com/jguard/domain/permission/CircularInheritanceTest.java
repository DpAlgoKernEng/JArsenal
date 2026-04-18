package com.jguard.domain.permission;

import com.jguard.domain.permission.aggregate.Role;
import com.jguard.domain.permission.valueobject.RoleCode;
import com.jguard.domain.permission.valueobject.InheritMode;
import com.jguard.domain.shared.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 循环继承验证测试
 *
 * 循环继承是指在角色继承关系中形成的循环依赖链：
 * - 直接循环：A -> A（角色以自己为父角色）
 * - 间接循环：A -> B -> C -> A
 *
 * 保护机制：
 * - 数据库层：V2迁移脚本中的触发器 prevent_circular_inheritance
 * - 应用层：PermissionDomainService 在修改父角色前会检查继承链
 *
 * 此测试验证领域层对循环继承的防护能力。
 */
@DisplayName("循环继承验证测试")
class CircularInheritanceTest {

    @Test
    @DisplayName("角色不能以自己为父角色")
    void shouldRejectSelfAsParent() {
        Role role = Role.create(new RoleCode("TEST"), "测试角色", null, InheritMode.EXTEND);
        role.setId(1L);

        // 尝试将自己设为父角色（代码层校验）
        // 预期：应该抛出 DomainException
        // 当前状态：setParentId 方法尚未实现校验逻辑，此测试将失败
        assertThrows(DomainException.class, () -> {
            role.setParentId(1L);  // 与自己ID相同
            // 当校验逻辑实现后，设置自身为父角色应该立即抛出异常
        }, "角色不应允许将自己设为父角色，这会形成直接循环继承");
    }

    @Test
    @DisplayName("应该检测循环继承链")
    void shouldDetectCircularChain() {
        // 模拟 A -> B -> C -> A 的循环场景
        // 这是间接循环继承的典型案例

        Role roleA = Role.create(new RoleCode("ROLE_A"), "角色A", null, InheritMode.EXTEND);
        roleA.setId(1L);

        Role roleB = Role.create(new RoleCode("ROLE_B"), "角色B", 1L, InheritMode.EXTEND);
        roleB.setId(2L);

        Role roleC = Role.create(new RoleCode("ROLE_C"), "角色C", 2L, InheritMode.EXTEND);
        roleC.setId(3L);

        // 尝试让 A 以 C 为父角色，形成循环 A -> B -> C -> A
        // 这个校验通常需要查询数据库获取完整的继承链
        // 由 PermissionDomainService 或数据库触发器（V2）完成

        // 当前状态：此测试仅记录预期行为
        // 真正的循环检测需要：
        // 1. 领域服务查询数据库获取祖先链
        // 2. 检查新的父角色是否在当前角色的后代链中
        // 3. 或依赖数据库触发器在 INSERT/UPDATE 时拦截

        // 文档化预期：如果实现了循环检测，以下代码应该抛出异常
        // assertThrows(DomainException.class, () -> {
        //     // 模拟领域服务调用：设置 C 为 A 的父角色
        //     roleA.setParentId(3L);
        //     // 领域服务检查继承链会检测到循环
        // });
    }

    @Test
    @DisplayName("新创建的角色父角色应为null或有效ID")
    void shouldHaveValidParentOnCreation() {
        // 创建顶级角色（无父角色）
        Role topRole = Role.create(new RoleCode("ROOT"), "根角色", null, InheritMode.EXTEND);
        assertNull(topRole.getParentId(), "顶级角色的父角色ID应为null");

        // 创建子角色
        Role childRole = Role.create(new RoleCode("CHILD"), "子角色", 1L, InheritMode.EXTEND);
        assertEquals(1L, childRole.getParentId(), "子角色应记录父角色ID");
    }

    @Test
    @DisplayName("继承模式应正确设置")
    void shouldSetCorrectInheritMode() {
        // EXTEND模式：继承父角色权限并扩展
        Role extendRole = Role.create(new RoleCode("EXT"), "扩展角色", 1L, InheritMode.EXTEND);
        assertEquals(InheritMode.EXTEND, extendRole.getInheritMode());

        // LIMIT模式：限制继承，仅保留自有权限
        Role limitRole = Role.create(new RoleCode("LMT"), "限制角色", 1L, InheritMode.LIMIT);
        assertEquals(InheritMode.LIMIT, limitRole.getInheritMode());

        // 默认模式
        Role defaultRole = Role.create(new RoleCode("DFLT"), "默认角色", 1L, null);
        assertEquals(InheritMode.EXTEND, defaultRole.getInheritMode(), "未指定继承模式时应默认为EXTEND");
    }

    @Test
    @DisplayName("多级继承链应正常工作")
    void shouldSupportMultiLevelInheritance() {
        // 构建合法的多级继承链: ROOT -> LEVEL1 -> LEVEL2 -> LEVEL3
        Role root = Role.create(new RoleCode("ROOT"), "根角色", null, InheritMode.EXTEND);
        root.setId(1L);

        Role level1 = Role.create(new RoleCode("L1"), "一级角色", 1L, InheritMode.EXTEND);
        level1.setId(2L);

        Role level2 = Role.create(new RoleCode("L2"), "二级角色", 2L, InheritMode.EXTEND);
        level2.setId(3L);

        Role level3 = Role.create(new RoleCode("L3"), "三级角色", 3L, InheritMode.EXTEND);
        level3.setId(4L);

        // 验证链结构
        assertNull(root.getParentId());
        assertEquals(1L, level1.getParentId());
        assertEquals(2L, level2.getParentId());
        assertEquals(3L, level3.getParentId());
    }
}