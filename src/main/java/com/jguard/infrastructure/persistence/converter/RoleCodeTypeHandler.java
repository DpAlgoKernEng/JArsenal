package com.jguard.infrastructure.persistence.converter;

import com.jguard.domain.permission.valueobject.RoleCode;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MyBatis TypeHandler for RoleCode value object.
 * Converts between RoleCode and database VARCHAR column.
 */
public class RoleCodeTypeHandler extends BaseTypeHandler<RoleCode> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, RoleCode parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.value());
    }

    @Override
    public RoleCode getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String code = rs.getString(columnName);
        return code != null ? new RoleCode(code) : null;
    }

    @Override
    public RoleCode getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String code = rs.getString(columnIndex);
        return code != null ? new RoleCode(code) : null;
    }

    @Override
    public RoleCode getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String code = cs.getString(columnIndex);
        return code != null ? new RoleCode(code) : null;
    }
}