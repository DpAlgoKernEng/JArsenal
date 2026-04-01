package com.example.demo.service.impl;

import com.example.demo.dto.PageResult;
import com.example.demo.dto.UserQueryRequest;
import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.UserService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResult<User> listUsers(UserQueryRequest request) {
        // 1. 调用 PageHelper.startPage() 设置分页参数
        //    必须在查询语句紧挨着的前面调用，只对紧接着的第一个查询生效
        PageHelper.startPage(request.getPageNum(), request.getPageSize());

        // 2. 执行查询
        List<User> users = userMapper.selectByCondition(
            request.getUsername(),
            request.getStatus()
        );

        // 3. 使用 PageInfo 包装结果，获取分页信息
        PageInfo<User> pageInfo = new PageInfo<>(users);

        // 4. 返回自定义分页结果
        return PageResult.of(
            pageInfo.getList(),
            pageInfo.getTotal(),
            pageInfo.getPages(),
            pageInfo.getPageNum(),
            pageInfo.getPageSize()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    @Transactional
    public User createUser(User user) {
        userMapper.insert(user);
        return user;
    }

    @Override
    @Transactional
    public User updateUser(User user) {
        userMapper.update(user);
        return userMapper.selectById(user.getId());
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        userMapper.deleteById(id);
    }
}