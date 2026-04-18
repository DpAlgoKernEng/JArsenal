package com.jguard.infrastructure.persistence.converter;

import com.jguard.domain.permission.valueobject.ResourceType;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MyBatis TypeHandler for ResourceType enum.
 * Converts between ResourceType enum and database string.
 */
public class ResourceTypeTypeHandler extends BaseTypeHandler<ResourceType> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, ResourceType parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.name());
    }

    @Override
    public ResourceType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String type = rs.getString(columnName);
        return type != null ? ResourceType.valueOf(type) : null;
    }

    @Override
    public ResourceType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String type = rs.getString(columnIndex);
        return type != null ? ResourceType.valueOf(type) : null;
    }

    @Override
    public ResourceType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String type = cs.getString(columnIndex);
        return type != null ? ResourceType.valueOf(type) : null;
    }
}