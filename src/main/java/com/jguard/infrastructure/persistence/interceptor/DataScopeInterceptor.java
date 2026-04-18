package com.jguard.infrastructure.persistence.interceptor;

import com.jguard.domain.permission.aggregate.DataDimension;
import com.jguard.domain.permission.entity.UserDataScope;
import com.jguard.domain.permission.repository.DataDimensionRepository;
import com.jguard.domain.permission.repository.DepartmentRepository;
import com.jguard.domain.permission.repository.UserDimensionRepository;
import com.jguard.domain.permission.service.DataScopeDomainService;
import com.jguard.domain.permission.valueobject.ScopeType;
import com.jguard.infrastructure.persistence.annotation.DataScope;
import com.jguard.infrastructure.security.UserContext;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据权限拦截器
 * MyBatis 插件，自动为查询添加数据权限过滤条件
 * 使用参数化查询防止 SQL 注入
 */
@Intercepts({
    @Signature(type = Executor.class, method = "query",
               args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
@Component
public class DataScopeInterceptor implements Interceptor {

    private static final Logger log = LoggerFactory.getLogger(DataScopeInterceptor.class);

    private final DataScopeDomainService dataScopeService;
    private final DataDimensionRepository dimensionRepository;
    private final JdbcTemplate jdbcTemplate;
    private final UserDimensionRepository userDimensionRepository;
    private final DepartmentRepository departmentRepository;

    public DataScopeInterceptor(DataScopeDomainService dataScopeService,
                                DataDimensionRepository dimensionRepository,
                                JdbcTemplate jdbcTemplate,
                                UserDimensionRepository userDimensionRepository,
                                DepartmentRepository departmentRepository) {
        this.dataScopeService = dataScopeService;
        this.dimensionRepository = dimensionRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.userDimensionRepository = userDimensionRepository;
        this.departmentRepository = departmentRepository;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
        Object parameter = invocation.getArgs()[1];

        DataScopeConfig config = getDataScopeConfig(ms);
        if (config == null) {
            return invocation.proceed();
        }

        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            log.debug("No user context, skip data scope filtering");
            return invocation.proceed();
        }

        UserDataScope userScope = dataScopeService.getUserDataScope(userId);
        com.jguard.domain.permission.entity.DataScope scope = userScope.getScope(config.getDimension());

        if (scope == null || scope.getScopeType() == ScopeType.ALL) {
            log.debug("User {} has ALL scope for dimension {}", userId, config.getDimension());
            return invocation.proceed();
        }

        BoundSql boundSql = ms.getBoundSql(parameter);
        String originalSql = boundSql.getSql();

        String condition = buildParameterizedCondition(scope, config);
        Map<String, Object> newParams = buildScopeParameters(scope, userId, config);

        String newSql = rewriteSql(originalSql, condition);
        mergeParameters(boundSql, newParams);

        log.debug("Data scope SQL rewrite: dimension={}, scopeType={}, condition={}",
                  config.getDimension(), scope.getScopeType(), condition);

        // 通过反射重置 SQL
        resetBoundSql(boundSql, newSql);

        return invocation.proceed();
    }

    /**
     * 构建参数化的 WHERE 条件
     */
    public String buildParameterizedCondition(com.jguard.domain.permission.entity.DataScope scope, DataScopeConfig config) {
        String column = buildColumnRef(config);

        return switch (scope.getScopeType()) {
            case SELF -> column + " = ?";
            case SELF_DEPT -> column + " = ?";
            case DEPT_TREE -> column + " IN (?)";
            case CUSTOM -> column + " IN (?)";
            default -> null;
        };
    }

    /**
     * 构建参数值
     */
    private Map<String, Object> buildScopeParameters(com.jguard.domain.permission.entity.DataScope scope, Long userId, DataScopeConfig config) {
        Map<String, Object> params = new HashMap<>();

        switch (scope.getScopeType()) {
            case SELF -> params.put("_dataScopeUserId", userId);
            case SELF_DEPT -> params.put("_dataScopeDeptId", getUserDeptId(userId));
            case DEPT_TREE -> params.put("_dataScopeDeptIds", getSubDeptIds(userId));
            case CUSTOM -> params.put("_dataScopeValues", validateScopeValues(scope.getScopeValues(), config.getDimension()));
        }

        return params;
    }

    /**
     * 重写 SQL，添加数据权限条件
     */
    public String rewriteSql(String originalSql, String condition) {
        if (condition == null) {
            return originalSql;
        }

        String upperSql = originalSql.toUpperCase();

        if (upperSql.contains("WHERE")) {
            // 已有 WHERE，追加 AND 条件
            int wherePos = upperSql.indexOf("WHERE");
            return originalSql.substring(0, wherePos + 5) +
                   " " + condition + " AND " +
                   originalSql.substring(wherePos + 5);
        } else {
            // 没有 WHERE，插入 WHERE 条件
            int orderByPos = upperSql.indexOf("ORDER BY");
            int groupByPos = upperSql.indexOf("GROUP BY");
            int limitPos = upperSql.indexOf("LIMIT");

            int insertPos = originalSql.length();
            if (orderByPos > 0) insertPos = Math.min(insertPos, orderByPos);
            if (groupByPos > 0) insertPos = Math.min(insertPos, groupByPos);
            if (limitPos > 0) insertPos = Math.min(insertPos, limitPos);

            return originalSql.substring(0, insertPos) +
                   " WHERE " + condition + " " +
                   originalSql.substring(insertPos);
        }
    }

    /**
     * 白名单校验范围值
     * 验证值存在于维度源表中且未被软删除
     */
    public Set<Long> validateScopeValues(Set<Long> values, String dimensionCode) {
        if (values == null || values.isEmpty()) {
            return Collections.emptySet();
        }

        // 过滤非法值（null、负数、零）
        Set<Long> validValues = values.stream()
            .filter(v -> v != null && v > 0)
            .collect(Collectors.toSet());

        if (validValues.isEmpty()) {
            return Collections.emptySet();
        }

        // 获取维度定义
        Optional<DataDimension> dimensionOpt = dimensionRepository.findByCode(dimensionCode);
        if (dimensionOpt.isEmpty()) {
            log.warn("Data dimension {} not found, skip validation", dimensionCode);
            return Collections.emptySet();
        }

        DataDimension dimension = dimensionOpt.get();

        // 白名单校验：检查值是否存在于维度源表
        String checkSql = String.format(
            "SELECT %s FROM %s WHERE %s IN (?) AND is_deleted = 0",
            dimension.getSourceColumn(), dimension.getSourceTable(), dimension.getSourceColumn()
        );

        try {
            // 使用 JdbcTemplate 执行白名单校验
            // 构建参数化查询
            String inClause = validValues.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

            String fullCheckSql = String.format(
                "SELECT %s FROM %s WHERE %s IN (%s) AND is_deleted = 0",
                dimension.getSourceColumn(), dimension.getSourceTable(),
                dimension.getSourceColumn(), inClause
            );

            List<Long> existingIds = jdbcTemplate.queryForList(fullCheckSql, Long.class);

            return new HashSet<>(existingIds);
        } catch (Exception e) {
            log.error("Failed to validate scope values for dimension {}", dimensionCode, e);
            return Collections.emptySet();
        }
    }

    /**
     * 获取用户部门ID
     */
    private Long getUserDeptId(Long userId) {
        return userDimensionRepository.getValueByDimension(userId, "DEPARTMENT");
    }

    /**
     * 获取用户子部门ID列表（递归）
     */
    private Set<Long> getSubDeptIds(Long userId) {
        Long deptId = getUserDeptId(userId);
        if (deptId == null) {
            return Collections.emptySet();
        }

        // 获取部门本身和所有子部门
        Set<Long> allDeptIds = new HashSet<>();
        allDeptIds.add(deptId);
        allDeptIds.addAll(departmentRepository.findAllSubDeptIds(deptId));

        return allDeptIds;
    }

    /**
     * 构建列引用（带表别名）
     */
    private String buildColumnRef(DataScopeConfig config) {
        if (config.getTableAlias() != null && !config.getTableAlias().isEmpty()) {
            return config.getTableAlias() + "." + config.getColumn();
        }
        return config.getColumn();
    }

    /**
     * 合并参数到 BoundSql
     */
    private void mergeParameters(BoundSql boundSql, Map<String, Object> newParams) {
        // MyBatis 参数处理：将额外参数添加到附加参数映射
        for (Map.Entry<String, Object> entry : newParams.entrySet()) {
            boundSql.setAdditionalParameter(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 重置 BoundSql 的 SQL
     */
    private void resetBoundSql(BoundSql boundSql, String newSql) {
        try {
            // 通过反射设置 sql 字段
            java.lang.reflect.Field sqlField = boundSql.getClass().getDeclaredField("sql");
            sqlField.setAccessible(true);
            sqlField.set(boundSql, newSql);
        } catch (Exception e) {
            log.error("Failed to reset bound SQL", e);
        }
    }

    /**
     * 从 Mapper 方法注解获取数据权限配置
     */
    private DataScopeConfig getDataScopeConfig(MappedStatement ms) {
        try {
            String msId = ms.getId();
            int lastDot = msId.lastIndexOf('.');
            if (lastDot < 0) {
                return null;
            }

            String mapperClassName = msId.substring(0, lastDot);
            String methodName = msId.substring(lastDot + 1);

            Class<?> mapperClass = Class.forName(mapperClassName);
            for (Method method : mapperClass.getDeclaredMethods()) {
                if (method.getName().equals(methodName)) {
                    DataScope annotation = method.getAnnotation(DataScope.class);
                    if (annotation != null) {
                        return new DataScopeConfig(
                            annotation.dimension(),
                            annotation.tableAlias(),
                            annotation.column()
                        );
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            // Mapper 类未找到，忽略
            log.debug("Mapper class not found for mapped statement: {}", ms.getId());
        }
        return null;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 无额外配置
    }
}