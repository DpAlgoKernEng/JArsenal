package com.jguard.infrastructure.persistence.converter;

import com.jguard.domain.permission.valueobject.RoleStatus;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MyBatis TypeHandler for RoleStatus enum.
 * Converts between RoleStatus enum and database integer (1=ENABLED, 0=DISABLED).
 */
public class RoleStatusTypeHandler extends BaseTypeHandler<RoleStatus> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, RoleStatus parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter == RoleStatus.ENABLED ? 1 : 0);
    }

    @Override
    public RoleStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int status = rs.getInt(columnName);
        if (rs.wasNull()) {
            return null;
        }
        return status == 1 ? RoleStatus.ENABLED : RoleStatus.DISABLED;
    }

    @Override
    public RoleStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int status = rs.getInt(columnIndex);
        if (rs.wasNull()) {
            return null;
        }
        return status == 1 ? RoleStatus.ENABLED : RoleStatus.DISABLED;
    }

    @Override
    public RoleStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int status = cs.getInt(columnIndex);
        if (cs.wasNull()) {
            return null;
        }
        return status == 1 ? RoleStatus.ENABLED : RoleStatus.DISABLED;
    }
}