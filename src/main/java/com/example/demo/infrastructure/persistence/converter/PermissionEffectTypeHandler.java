package com.example.demo.infrastructure.persistence.converter;

import com.example.demo.domain.permission.valueobject.PermissionEffect;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MyBatis TypeHandler for PermissionEffect enum.
 * Converts between PermissionEffect enum and database string.
 * Defaults to ALLOW if null.
 */
public class PermissionEffectTypeHandler extends BaseTypeHandler<PermissionEffect> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, PermissionEffect parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.name());
    }

    @Override
    public PermissionEffect getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String effect = rs.getString(columnName);
        return effect != null ? PermissionEffect.valueOf(effect) : PermissionEffect.ALLOW;
    }

    @Override
    public PermissionEffect getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String effect = rs.getString(columnIndex);
        return effect != null ? PermissionEffect.valueOf(effect) : PermissionEffect.ALLOW;
    }

    @Override
    public PermissionEffect getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String effect = cs.getString(columnIndex);
        return effect != null ? PermissionEffect.valueOf(effect) : PermissionEffect.ALLOW;
    }
}