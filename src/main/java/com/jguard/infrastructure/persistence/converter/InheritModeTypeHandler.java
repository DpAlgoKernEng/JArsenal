package com.jguard.infrastructure.persistence.converter;

import com.jguard.domain.permission.valueobject.InheritMode;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MyBatis TypeHandler for InheritMode enum.
 * Converts between InheritMode enum and database string.
 * Defaults to EXTEND if null.
 */
public class InheritModeTypeHandler extends BaseTypeHandler<InheritMode> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, InheritMode parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.name());
    }

    @Override
    public InheritMode getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String mode = rs.getString(columnName);
        return mode != null ? InheritMode.valueOf(mode) : InheritMode.EXTEND;
    }

    @Override
    public InheritMode getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String mode = rs.getString(columnIndex);
        return mode != null ? InheritMode.valueOf(mode) : InheritMode.EXTEND;
    }

    @Override
    public InheritMode getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String mode = cs.getString(columnIndex);
        return mode != null ? InheritMode.valueOf(mode) : InheritMode.EXTEND;
    }
}