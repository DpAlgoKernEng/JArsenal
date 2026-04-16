package com.example.demo.infrastructure.interceptor;

import com.example.demo.domain.permission.entity.DataScope;
import com.example.demo.infrastructure.persistence.interceptor.DataScopeConfig;
import com.example.demo.infrastructure.persistence.interceptor.DataScopeInterceptor;
import com.example.demo.domain.permission.valueobject.ScopeType;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

/**
 * DataScopeInterceptor 测试
 */
class DataScopeInterceptorTest {

    @Test
    void shouldBuildParameterizedConditionForSelf() {
        DataScopeInterceptor interceptor = createInterceptor();
        DataScope scope = DataScope.self("DEPARTMENT");
        DataScopeConfig config = new DataScopeConfig("DEPARTMENT", "u", "dept_id");

        String condition = interceptor.buildParameterizedCondition(scope, config);

        assertEquals("u.dept_id = ?", condition);
    }

    @Test
    void shouldBuildParameterizedConditionForSelfDept() {
        DataScopeInterceptor interceptor = createInterceptor();
        DataScope scope = DataScope.selfDept("DEPARTMENT");
        DataScopeConfig config = new DataScopeConfig("DEPARTMENT", "", "dept_id");

        String condition = interceptor.buildParameterizedCondition(scope, config);

        assertEquals("dept_id = ?", condition);
    }

    @Test
    void shouldBuildParameterizedConditionForDeptTree() {
        DataScopeInterceptor interceptor = createInterceptor();
        DataScope scope = DataScope.deptTree("DEPARTMENT", Set.of(1L, 2L, 3L));
        DataScopeConfig config = new DataScopeConfig("DEPARTMENT", "d", "dept_id");

        String condition = interceptor.buildParameterizedCondition(scope, config);

        assertEquals("d.dept_id IN (?)", condition);
    }

    @Test
    void shouldBuildParameterizedConditionForCustom() {
        DataScopeInterceptor interceptor = createInterceptor();
        DataScope scope = DataScope.custom("DEPARTMENT", Set.of(1L, 2L, 3L));
        DataScopeConfig config = new DataScopeConfig("DEPARTMENT", "d", "dept_id");

        String condition = interceptor.buildParameterizedCondition(scope, config);

        assertTrue(condition.contains(":_dataScopeValues") || condition.contains("?"));
        assertFalse(condition.contains("1,2,3")); // 不应包含原始值
        assertFalse(condition.contains("1")); // 不应包含具体数字
    }

    @Test
    void shouldReturnNullConditionForAllScope() {
        DataScopeInterceptor interceptor = createInterceptor();
        DataScope scope = DataScope.all("DEPARTMENT");
        DataScopeConfig config = new DataScopeConfig("DEPARTMENT", "u", "dept_id");

        String condition = interceptor.buildParameterizedCondition(scope, config);

        assertNull(condition);
    }

    @Test
    void shouldRewriteSqlWithExistingWhere() {
        DataScopeInterceptor interceptor = createInterceptor();
        String originalSql = "SELECT * FROM user u WHERE u.status = 1 ORDER BY u.name";
        String condition = "u.dept_id = ?";

        String newSql = interceptor.rewriteSql(originalSql, condition);

        assertTrue(newSql.contains("WHERE"));
        assertTrue(newSql.contains(condition));
        assertTrue(newSql.contains("ORDER BY"));
        assertTrue(newSql.contains("u.status = 1"));
        // WHERE 条件应该先于原条件
        assertTrue(newSql.indexOf(condition) < newSql.indexOf("u.status = 1"));
    }

    @Test
    void shouldRewriteSqlWithoutWhere() {
        DataScopeInterceptor interceptor = createInterceptor();
        String originalSql = "SELECT * FROM user u ORDER BY u.name";
        String condition = "u.dept_id = ?";

        String newSql = interceptor.rewriteSql(originalSql, condition);

        assertTrue(newSql.contains("WHERE"));
        assertTrue(newSql.contains(condition));
        assertTrue(newSql.contains("ORDER BY"));
        // WHERE 应该在 ORDER BY 之前
        assertTrue(newSql.indexOf("WHERE") < newSql.indexOf("ORDER BY"));
    }

    @Test
    void shouldRewriteSqlWithLimit() {
        DataScopeInterceptor interceptor = createInterceptor();
        String originalSql = "SELECT * FROM user u LIMIT 10";
        String condition = "u.dept_id = ?";

        String newSql = interceptor.rewriteSql(originalSql, condition);

        assertTrue(newSql.contains("WHERE"));
        assertTrue(newSql.contains(condition));
        assertTrue(newSql.contains("LIMIT"));
        assertTrue(newSql.indexOf("WHERE") < newSql.indexOf("LIMIT"));
    }

    @Test
    void shouldRewriteSqlWithGroupBy() {
        DataScopeInterceptor interceptor = createInterceptor();
        String originalSql = "SELECT u.dept_id, COUNT(*) FROM user u GROUP BY u.dept_id";
        String condition = "u.dept_id = ?";

        String newSql = interceptor.rewriteSql(originalSql, condition);

        assertTrue(newSql.contains("WHERE"));
        assertTrue(newSql.contains(condition));
        assertTrue(newSql.contains("GROUP BY"));
        assertTrue(newSql.indexOf("WHERE") < newSql.indexOf("GROUP BY"));
    }

    @Test
    void shouldNotRewriteSqlWhenConditionIsNull() {
        DataScopeInterceptor interceptor = createInterceptor();
        String originalSql = "SELECT * FROM user u";

        String newSql = interceptor.rewriteSql(originalSql, null);

        assertEquals(originalSql, newSql);
    }

    private DataScopeInterceptor createInterceptor() {
        return new DataScopeInterceptor(null, null, null, null, null);
    }
}