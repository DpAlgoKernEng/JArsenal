package com.jguard.infrastructure.persistence.converter;

import com.jguard.domain.permission.valueobject.SensitiveLevel;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MyBatis TypeHandler for SensitiveLevel enum.
 * Converts between SensitiveLevel enum and database string.
 */
public class SensitiveLevelTypeHandler extends BaseTypeHandler<SensitiveLevel> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, SensitiveLevel parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.name());
    }

    @Override
    public SensitiveLevel getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        if (rs.wasNull() || value == null) {
            return SensitiveLevel.NORMAL; // Default value
        }
        return SensitiveLevel.valueOf(value);
    }

    @Override
    public SensitiveLevel getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        if (rs.wasNull() || value == null) {
            return SensitiveLevel.NORMAL;
        }
        return SensitiveLevel.valueOf(value);
    }

    @Override
    public SensitiveLevel getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        if (cs.wasNull() || value == null) {
            return SensitiveLevel.NORMAL;
        }
        return SensitiveLevel.valueOf(value);
    }
}