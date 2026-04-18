package com.jguard.infrastructure.persistence.mapper;

import com.jguard.domain.permission.aggregate.Role;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * Role Mapper Interface
 * MyBatis mapper for Role aggregate persistence
 */
@Mapper
public interface RoleMapper {

    @Insert("INSERT INTO role(code, name, parent_id, status, inherit_mode, is_builtin, is_deleted, version, sort) " +
            "VALUES(#{code.value}, #{name}, #{parentId}, #{status}, #{inheritMode}, #{isBuiltin}, #{isDeleted}, #{version}, #{sort})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Role role);

    @Update("UPDATE role SET name=#{name}, parent_id=#{parentId}, status=#{status}, inherit_mode=#{inheritMode}, " +
            "sort=#{sort}, version=#{version} WHERE id=#{id}")
    int update(Role role);

    @Select("SELECT * FROM role WHERE id = #{id} AND is_deleted = 0")
    @Results(id = "roleResult", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "code", column = "code", typeHandler = com.jguard.infrastructure.persistence.converter.RoleCodeTypeHandler.class),
            @Result(property = "name", column = "name"),
            @Result(property = "parentId", column = "parent_id"),
            @Result(property = "status", column = "status", typeHandler = com.jguard.infrastructure.persistence.converter.RoleStatusTypeHandler.class),
            @Result(property = "inheritMode", column = "inherit_mode", typeHandler = com.jguard.infrastructure.persistence.converter.InheritModeTypeHandler.class),
            @Result(property = "isBuiltin", column = "is_builtin"),
            @Result(property = "isDeleted", column = "is_deleted"),
            @Result(property = "version", column = "version"),
            @Result(property = "sort", column = "sort")
    })
    Role findById(@Param("id") Long id);

    @Select("SELECT * FROM role WHERE code = #{code} AND is_deleted = 0")
    @ResultMap("roleResult")
    Role findByCode(@Param("code") String code);

    @Select("SELECT * FROM role WHERE is_deleted = 0 ORDER BY sort")
    @ResultMap("roleResult")
    List<Role> findAllNotDeleted();

    @Select("SELECT * FROM role WHERE parent_id = #{parentId} AND is_deleted = 0")
    @ResultMap("roleResult")
    List<Role> findByParentId(@Param("parentId") Long parentId);

    @Select("SELECT r.* FROM role r INNER JOIN user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.is_deleted = 0")
    @ResultMap("roleResult")
    List<Role> findRolesByUserId(@Param("userId") Long userId);

    @Update("UPDATE role SET is_deleted = 1, version = version + 1 WHERE id = #{id}")
    int softDelete(@Param("id") Long id);

    @Update("UPDATE role SET version = version + 1 WHERE id = #{id}")
    int incrementVersion(@Param("id") Long id);

    // XML-based complex queries
    List<Role> findRoleTree();

    Role findRoleWithPermissions(@Param("roleId") Long roleId);

    List<Role> findRolesWithPermissionsByUserId(@Param("userId") Long userId);

    @Select("SELECT r.code FROM role r INNER JOIN user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.is_deleted = 0")
    List<String> findRoleCodesByUserId(@Param("userId") Long userId);

    List<Long> findAncestorRoleIds(@Param("roleId") Long roleId);
}