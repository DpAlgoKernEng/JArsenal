package com.example.demo.infrastructure.persistence.mapper;

import com.example.demo.domain.permission.valueobject.ActionType;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * Permission Action Mapper Interface
 * MyBatis mapper for permission_action table persistence
 */
@Mapper
public interface PermissionActionMapper {

    @Insert("INSERT INTO permission_action(permission_id, action) VALUES(#{permissionId}, #{action})")
    int insert(@Param("permissionId") Long permissionId, @Param("action") ActionType action);

    @Insert("<script>" +
            "INSERT INTO permission_action(permission_id, action) VALUES " +
            "<foreach collection='actions' item='action' separator=','>" +
            "(#{permissionId}, #{action})" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("permissionId") Long permissionId, @Param("actions") List<ActionType> actions);

    @Select("SELECT action FROM permission_action WHERE permission_id = #{permissionId}")
    List<String> findActionsByPermissionId(@Param("permissionId") Long permissionId);

    @Delete("DELETE FROM permission_action WHERE permission_id = #{permissionId}")
    int deleteByPermissionId(@Param("permissionId") Long permissionId);

    @Delete("DELETE FROM permission_action WHERE permission_id IN " +
            "<foreach collection='permissionIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>")
    int deleteByPermissionIds(@Param("permissionIds") List<Long> permissionIds);
}