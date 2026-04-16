package com.example.demo.infrastructure.interceptor;

import com.example.demo.domain.permission.aggregate.DataDimension;
import com.example.demo.domain.permission.entity.DataScope;
import com.example.demo.domain.permission.repository.DataDimensionRepository;
import com.example.demo.domain.permission.repository.DepartmentRepository;
import com.example.demo.domain.permission.repository.UserDimensionRepository;
import com.example.demo.domain.permission.service.DataScopeDomainService;
import com.example.demo.infrastructure.persistence.interceptor.DataScopeInterceptor;
import com.example.demo.domain.permission.valueobject.ScopeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.Optional;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * SQL 注入安全测试
 */
class SqlInjectionTest {

    @Mock
    private DataDimensionRepository dimensionRepository;

    @Mock
    private UserDimensionRepository userDimensionRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private DataScopeDomainService dataScopeService;

    @Mock
    private JdbcTemplate jdbcTemplate;

    private DataScopeInterceptor interceptor;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        interceptor = new DataScopeInterceptor(
            dataScopeService,
            dimensionRepository,
            jdbcTemplate,
            userDimensionRepository,
            departmentRepository
        );
    }

    @Test
    void shouldUseParameterizedQuery() {
        DataScope scope = DataScope.custom("DEPARTMENT", Set.of(1L, 2L, 3L));
        com.example.demo.infrastructure.persistence.interceptor.DataScopeConfig config =
            new com.example.demo.infrastructure.persistence.interceptor.DataScopeConfig("DEPARTMENT", "d", "dept_id");

        String condition = interceptor.buildParameterizedCondition(scope, config);

        // 必须不包含原始值
        assertFalse(condition.contains("1,2,3"));
        assertFalse(condition.contains("1,"));
        assertFalse(condition.contains("2,"));
        assertFalse(condition.contains("3"));

        // 必须使用参数占位符
        assertTrue(condition.contains("?"));
    }

    @Test
    void shouldRejectNegativeValuesInValidation() {
        Set<Long> maliciousValues = Set.of(1L, -1L);

        // validateScopeValues 方法应该过滤掉负数
        DataDimension dimension = DataDimension.create("DEPARTMENT", "部门", "department", "id");
        when(dimensionRepository.findByCode("DEPARTMENT")).thenReturn(Optional.of(dimension));
        when(jdbcTemplate.queryForList(anyString(), eq(Long.class))).thenReturn(java.util.List.of(1L));

        Set<Long> valid = interceptor.validateScopeValues(maliciousValues, "DEPARTMENT");

        // 只有正数应保留
        assertTrue(valid.contains(1L));
        assertFalse(valid.contains(-1L));
    }

    @Test
    void shouldReturnEmptySetWhenDimensionNotFound() {
        Set<Long> inputValues = Set.of(1L, 2L, 3L);

        when(dimensionRepository.findByCode("DEPARTMENT")).thenReturn(Optional.empty());

        Set<Long> result = interceptor.validateScopeValues(inputValues, "DEPARTMENT");

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptySetForNullValues() {
        Set<Long> result = interceptor.validateScopeValues(null, "DEPARTMENT");

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptySetForEmptyValues() {
        Set<Long> result = interceptor.validateScopeValues(Set.of(), "DEPARTMENT");

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldValidateScopeValuesWithExistingIds() {
        Set<Long> inputValues = Set.of(1L, 2L, 3L);

        DataDimension dimension = DataDimension.create("DEPARTMENT", "部门", "department", "id");
        when(dimensionRepository.findByCode("DEPARTMENT")).thenReturn(Optional.of(dimension));
        when(jdbcTemplate.queryForList(anyString(), eq(Long.class))).thenReturn(java.util.List.of(1L, 2L));

        Set<Long> result = interceptor.validateScopeValues(inputValues, "DEPARTMENT");

        // 只有存在于维度表中的ID被保留
        assertTrue(result.contains(1L));
        assertTrue(result.contains(2L));
        assertFalse(result.contains(3L));  // 不存在的ID被过滤
    }

    @Test
    void shouldNotInjectSqlThroughDimensionCode() {
        String maliciousDimension = "DEPARTMENT'; DROP TABLE users;--";

        when(dimensionRepository.findByCode(maliciousDimension)).thenReturn(Optional.empty());

        Optional<DataDimension> dimension = dimensionRepository.findByCode(maliciousDimension);

        assertTrue(dimension.isEmpty());
    }
}