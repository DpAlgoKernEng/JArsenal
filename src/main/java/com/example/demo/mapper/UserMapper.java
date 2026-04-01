package com.example.demo.mapper;

import com.example.demo.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {

    /**
     * 分页查询用户列表
     */
    List<User> selectAll();

    /**
     * 条件查询用户列表
     */
    List<User> selectByCondition(@Param("username") String username,
                                  @Param("status") Integer status);

    /**
     * 根据ID查询
     */
    User selectById(@Param("id") Long id);

    /**
     * 根据用户名查询
     */
    User selectByUsername(@Param("username") String username);

    /**
     * 插入用户
     */
    int insert(User user);

    /**
     * 更新用户
     */
    int update(User user);

    /**
     * 删除用户
     */
    int deleteById(@Param("id") Long id);
}