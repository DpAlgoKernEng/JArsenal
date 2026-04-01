package com.example.demo.service;

import com.example.demo.dto.PageResult;
import com.example.demo.dto.UserQueryRequest;
import com.example.demo.entity.User;

public interface UserService {

    /**
     * 分页查询用户列表
     */
    PageResult<User> listUsers(UserQueryRequest request);

    /**
     * 根据ID查询用户
     */
    User getUserById(Long id);

    /**
     * 创建用户
     */
    User createUser(User user);

    /**
     * 更新用户
     */
    User updateUser(User user);

    /**
     * 删除用户
     */
    void deleteUser(Long id);
}