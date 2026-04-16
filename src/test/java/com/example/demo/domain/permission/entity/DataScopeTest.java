package com.example.demo.domain.permission.entity;

import com.example.demo.domain.permission.valueobject.ScopeType;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

/**
 * DataScope 值对象测试
 */
class DataScopeTest {

    @Test
    void shouldCreateAllScope() {
        DataScope scope = DataScope.all("DEPARTMENT");

        assertEquals("DEPARTMENT", scope.getDimensionCode());
        assertEquals(ScopeType.ALL, scope.getScopeType());
        assertTrue(scope.getScopeValues().isEmpty());
    }

    @Test
    void shouldCreateSelfScope() {
        DataScope scope = DataScope.self("PROJECT");

        assertEquals("PROJECT", scope.getDimensionCode());
        assertEquals(ScopeType.SELF, scope.getScopeType());
        assertTrue(scope.getScopeValues().isEmpty());
    }

    @Test
    void shouldCreateSelfDeptScope() {
        DataScope scope = DataScope.selfDept("DEPARTMENT");

        assertEquals("DEPARTMENT", scope.getDimensionCode());
        assertEquals(ScopeType.SELF_DEPT, scope.getScopeType());
        assertTrue(scope.getScopeValues().isEmpty());
    }

    @Test
    void shouldCreateDeptTreeScope() {
        Set<Long> deptIds = Set.of(1L, 2L, 3L);
        DataScope scope = DataScope.deptTree("DEPARTMENT", deptIds);

        assertEquals("DEPARTMENT", scope.getDimensionCode());
        assertEquals(ScopeType.DEPT_TREE, scope.getScopeType());
        assertEquals(3, scope.getScopeValues().size());
        assertTrue(scope.getScopeValues().contains(1L));
        assertTrue(scope.getScopeValues().contains(2L));
        assertTrue(scope.getScopeValues().contains(3L));
    }

    @Test
    void shouldCreateCustomScope() {
        Set<Long> customIds = Set.of(10L, 20L, 30L);
        DataScope scope = DataScope.custom("CUSTOMER", customIds);

        assertEquals("CUSTOMER", scope.getDimensionCode());
        assertEquals(ScopeType.CUSTOM, scope.getScopeType());
        assertEquals(customIds, scope.getScopeValues());
    }

    @Test
    void shouldHandleNullScopeValues() {
        DataScope scope = new DataScope("DEPARTMENT", ScopeType.CUSTOM, null);

        assertNotNull(scope.getScopeValues());
        assertTrue(scope.getScopeValues().isEmpty());
    }

    @Test
    void shouldHandleEmptyScopeValues() {
        DataScope scope = DataScope.custom("PROJECT", Set.of());

        assertTrue(scope.getScopeValues().isEmpty());
        assertFalse(scope.hasScopeValues());
    }

    @Test
    void shouldReturnImmutableScopeValues() {
        Set<Long> values = Set.of(1L, 2L);
        DataScope scope = DataScope.custom("DEPARTMENT", values);

        // 返回的集合应该是不可变的
        Set<Long> scopeValues = scope.getScopeValues();
        assertThrows(UnsupportedOperationException.class, () -> scopeValues.add(3L));
    }

    @Test
    void shouldCheckHasScopeValues() {
        DataScope scopeWithValues = DataScope.custom("DEPARTMENT", Set.of(1L, 2L));
        assertTrue(scopeWithValues.hasScopeValues());

        DataScope scopeWithoutValues = DataScope.all("DEPARTMENT");
        assertFalse(scopeWithoutValues.hasScopeValues());
    }

    @Test
    void shouldBeEqualByDimensionCodeAndScopeType() {
        DataScope scope1 = DataScope.self("DEPARTMENT");
        DataScope scope2 = DataScope.self("DEPARTMENT");

        assertEquals(scope1, scope2);
        assertEquals(scope1.hashCode(), scope2.hashCode());
    }

    @Test
    void shouldNotBeEqualByDifferentDimension() {
        DataScope scope1 = DataScope.self("DEPARTMENT");
        DataScope scope2 = DataScope.self("PROJECT");

        assertNotEquals(scope1, scope2);
    }

    @Test
    void shouldNotBeEqualByDifferentScopeType() {
        DataScope scope1 = DataScope.self("DEPARTMENT");
        DataScope scope2 = DataScope.selfDept("DEPARTMENT");

        assertNotEquals(scope1, scope2);
    }
}