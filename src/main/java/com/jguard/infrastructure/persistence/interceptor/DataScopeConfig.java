package com.jguard.infrastructure.persistence.interceptor;

/**
 * 数据权限配置
 * 从 @DataScope 注解解析的配置信息
 */
public class DataScopeConfig {

    private final String dimension;
    private final String tableAlias;
    private final String column;

    public DataScopeConfig(String dimension, String tableAlias, String column) {
        this.dimension = dimension;
        this.tableAlias = tableAlias;
        this.column = column;
    }

    public String getDimension() {
        return dimension;
    }

    public String getTableAlias() {
        return tableAlias;
    }

    public String getColumn() {
        return column;
    }
}