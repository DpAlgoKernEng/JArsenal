package com.example.demo.infrastructure.persistence.mapper;

import com.example.demo.domain.permission.entity.FieldPermission;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 字段权限 Mapper
 */
@Mapper
public interface FieldPermissionMapper {

    @Insert("INSERT INTO field_permission(role_id, field_id, can_view, can_edit) " +
            "VALUES(#{roleId}, #{fieldId}, #{canView}, #{canEdit})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(FieldPermission perm);

    @Select("SELECT fp.id, fp.role_id, fp.field_id, fp.can_view, fp.can_edit " +
            "FROM field_permission fp " +
            "INNER JOIN user_role ur ON fp.role_id = ur.role_id " +
            "INNER JOIN resource_field rf ON fp.field_id = rf.id " +
            "WHERE ur.user_id = #{userId} AND rf.resource_id = #{resourceId}")
    @Results(id = "fieldPermissionResult", value = {
        @Result(property = "id", column = "id"),
        @Result(property = "roleId", column = "role_id"),
        @Result(property = "fieldId", column = "field_id"),
        @Result(property = "canView", column = "can_view"),
        @Result(property = "canEdit", column = "can_edit")
    })
    List<FieldPermission> findByUserIdAndResourceId(@Param("userId") Long userId, @Param("resourceId") Long resourceId);

    @Select("SELECT id, role_id, field_id, can_view, can_edit FROM field_permission WHERE role_id = #{roleId}")
    @ResultMap("fieldPermissionResult")
    List<FieldPermission> findByRoleId(@Param("roleId") Long roleId);

    @Select("SELECT id, role_id, field_id, can_view, can_edit FROM field_permission WHERE field_id = #{fieldId}")
    @ResultMap("fieldPermissionResult")
    List<FieldPermission> findByFieldId(@Param("fieldId") Long fieldId);

    @Update("UPDATE field_permission SET can_view=#{canView}, can_edit=#{canEdit} WHERE id=#{id}")
    int update(FieldPermission perm);

    @Delete("DELETE FROM field_permission WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Long roleId);

    @Delete("DELETE FROM field_permission WHERE field_id = #{fieldId}")
    int deleteByFieldId(@Param("fieldId") Long fieldId);
}