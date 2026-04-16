package com.example.demo.infrastructure.persistence.mapper;

import com.example.demo.domain.permission.entity.RoleDataScope;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Set;

/**
 * 角色数据范围 Mapper
 */
@Mapper
public interface RoleDataScopeMapper {

    @Insert("INSERT INTO role_data_scope(role_id, dimension_code, dimension_value, scope_expression) " +
            "VALUES(#{roleId}, #{dimensionCode}, #{dimensionValue}, #{scopeExpression})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RoleDataScope scope);

    @Select("SELECT * FROM role_data_scope WHERE id = #{id}")
    RoleDataScope findById(@Param("id") Long id);

    @Select("SELECT * FROM role_data_scope WHERE role_id = #{roleId}")
    List<RoleDataScope> findByRoleId(@Param("roleId") Long roleId);

    @Delete("DELETE FROM role_data_scope WHERE role_id = #{roleId} AND dimension_code = #{dimensionCode}")
    int deleteByRoleAndDimension(@Param("roleId") Long roleId, @Param("dimensionCode") String dimensionCode);

    @Delete("DELETE FROM role_data_scope WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Long roleId);
}