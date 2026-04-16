package com.example.demo.infrastructure.persistence.mapper;

import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Set;

/**
 * User Role Mapper Interface
 * MyBatis mapper for user_role association table persistence
 */
@Mapper
public interface UserRoleMapper {

    @Insert("INSERT INTO user_role(user_id, role_id) VALUES(#{userId}, #{roleId})")
    int insert(@Param("userId") Long userId, @Param("roleId") Long roleId);

    @Delete("DELETE FROM user_role WHERE user_id = #{userId} AND role_id = #{roleId}")
    int delete(@Param("userId") Long userId, @Param("roleId") Long roleId);

    @Select("SELECT role_id FROM user_role WHERE user_id = #{userId}")
    List<Long> findRoleIdsByUserId(@Param("userId") Long userId);

    @Select("SELECT user_id FROM user_role WHERE role_id = #{roleId}")
    List<Long> findUserIdsByRoleId(@Param("roleId") Long roleId);

    @Select("SELECT role_id FROM user_role WHERE user_id = #{userId}")
    Set<Long> findUserRoleIds(@Param("userId") Long userId);

    @Delete("DELETE FROM user_role WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Long roleId);

    @Delete("DELETE FROM user_role WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM user_role WHERE user_id = #{userId} AND role_id = #{roleId}")
    int existsByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);

    /**
     * 获取活跃用户ID列表（按角色数量排序，用于缓存预热）
     */
    @Select("SELECT user_id FROM user_role GROUP BY user_id ORDER BY COUNT(*) DESC LIMIT #{limit}")
    List<Long> findActiveUserIds(@Param("limit") int limit);
}