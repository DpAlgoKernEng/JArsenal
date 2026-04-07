package com.example.demo.infrastructure.persistence.mapper;

import com.example.demo.infrastructure.persistence.po.UserPO;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户 Mapper 接口
 */
@Mapper
public interface UserMapper {

    @Select("SELECT id, username, password, email, status, create_time, update_time FROM user WHERE id = #{id}")
    UserPO selectById(@Param("id") Long id);

    @Select("SELECT id, username, password, email, status, create_time, update_time FROM user WHERE username = #{username}")
    UserPO selectByUsername(@Param("username") String username);

    @Select("SELECT COUNT(1) FROM user WHERE username = #{username}")
    int countByUsername(@Param("username") String username);

    @Insert("INSERT INTO user(username, password, email, status, create_time, update_time) " +
            "VALUES(#{username}, #{password}, #{email}, #{status}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserPO user);

    @Update("UPDATE user SET username=#{username}, email=#{email}, status=#{status}, update_time=NOW() WHERE id=#{id}")
    int update(UserPO user);

    @Delete("DELETE FROM user WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Select("<script>" +
            "SELECT id, username, password, email, status, create_time, update_time FROM user " +
            "<where>" +
            "<if test='username != null and username != \"\"'> AND username LIKE CONCAT('%', #{username}, '%')</if>" +
            "<if test='status != null'> AND status = #{status}</if>" +
            "</where>" +
            "ORDER BY create_time DESC" +
            "</script>")
    List<UserPO> selectByCondition(@Param("username") String username, @Param("status") Integer status);

    @Select("<script>" +
            "SELECT COUNT(1) FROM user " +
            "<where>" +
            "<if test='username != null and username != \"\"'> AND username LIKE CONCAT('%', #{username}, '%')</if>" +
            "<if test='status != null'> AND status = #{status}</if>" +
            "</where>" +
            "</script>")
    long countByCondition(@Param("username") String username, @Param("status") Integer status);

    @Select("SELECT id, username, password, email, status, create_time, update_time FROM user ORDER BY create_time DESC")
    List<UserPO> selectAll();
}