package com.example.demo.infrastructure.persistence.mapper;

import com.example.demo.domain.permission.entity.Permission;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * Permission Mapper Interface
 * MyBatis mapper for Permission entity persistence
 */
@Mapper
public interface PermissionMapper {

    @Insert("INSERT INTO permission(role_id, resource_id, effect) VALUES(#{roleId}, #{resourceId}, #{effect})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Permission permission);

    @Select("SELECT * FROM permission WHERE role_id = #{roleId}")
    @Results(id = "permissionResult", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "roleId", column = "role_id"),
            @Result(property = "resourceId", column = "resource_id"),
            @Result(property = "effect", column = "effect", typeHandler = com.example.demo.infrastructure.persistence.converter.PermissionEffectTypeHandler.class)
    })
    List<Permission> findByRoleId(@Param("roleId") Long roleId);

    @Select("SELECT * FROM permission WHERE role_id = #{roleId} AND resource_id = #{resourceId}")
    @ResultMap("permissionResult")
    Permission findByRoleAndResource(@Param("roleId") Long roleId, @Param("resourceId") Long resourceId);

    @Delete("DELETE FROM permission WHERE role_id = #{roleId} AND resource_id = #{resourceId}")
    int deleteByRoleAndResource(@Param("roleId") Long roleId, @Param("resourceId") Long resourceId);

    @Delete("DELETE FROM permission WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Long roleId);

    @Select("SELECT id FROM permission WHERE role_id = #{roleId}")
    List<Long> findIdsByRoleId(@Param("roleId") Long roleId);
}