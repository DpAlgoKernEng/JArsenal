package com.jguard.infrastructure.persistence.mapper;

import org.apache.ibatis.annotations.*;
import java.util.Set;

/**
 * 角色数据范围值 Mapper
 * 存储数据范围的具体ID值
 */
@Mapper
public interface RoleDataScopeValueMapper {

    /**
     * 批量插入范围值
     */
    @Insert("<script>" +
            "INSERT INTO role_data_scope_value(scope_id, scope_value) VALUES " +
            "<foreach collection='values' item='val' separator=','>" +
            "(#{scopeId}, #{val})" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("scopeId") Long scopeId, @Param("values") Set<Long> values);

    /**
     * 查找范围的所有值
     */
    @Select("SELECT scope_value FROM role_data_scope_value WHERE scope_id = #{scopeId}")
    Set<Long> findByScopeId(@Param("scopeId") Long scopeId);

    /**
     * 删除范围的所有值
     */
    @Delete("DELETE FROM role_data_scope_value WHERE scope_id = #{scopeId}")
    int deleteByScopeId(@Param("scopeId") Long scopeId);
}