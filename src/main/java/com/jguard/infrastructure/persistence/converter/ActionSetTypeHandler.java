package com.jguard.infrastructure.persistence.converter;

import com.jguard.domain.permission.valueobject.ActionType;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.HashSet;

/**
 * MyBatis TypeHandler for Set&lt;ActionType&gt;.
 * Converts between Set of ActionType enum and database comma-separated string.
 */
@Slf4j
public class ActionSetTypeHandler extends BaseTypeHandler<Set<ActionType>> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Set<ActionType> parameter, JdbcType jdbcType) throws SQLException {
        String value = parameter.stream()
            .map(ActionType::name)
            .reduce((a, b) -> a + "," + b)
            .orElse("");
        ps.setString(i, value);
    }

    @Override
    public Set<ActionType> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return parseActionSet(value);
    }

    @Override
    public Set<ActionType> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return parseActionSet(value);
    }

    @Override
    public Set<ActionType> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return parseActionSet(value);
    }

    private Set<ActionType> parseActionSet(String value) {
        if (value == null || value.isBlank()) {
            return new HashSet<>();
        }

        Set<ActionType> result = new HashSet<>();
        String[] parts = value.split(",");
        for (String part : parts) {
            try {
                result.add(ActionType.valueOf(part.trim()));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid action type found in database, ignoring: {}", part.trim());
            }
        }
        return result;
    }
}