package com.jguard.infrastructure.persistence.repository;

import com.jguard.domain.user.aggregate.User;
import com.jguard.domain.user.repository.UserRepository;
import com.jguard.domain.user.valueobject.UserId;
import com.jguard.domain.user.valueobject.Username;
import com.jguard.infrastructure.persistence.converter.UserConverter;
import com.jguard.infrastructure.persistence.mapper.UserMapper;
import com.jguard.infrastructure.persistence.po.UserPO;
import com.github.pagehelper.PageHelper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户仓库实现
 */
@Repository
public class UserRepositoryImpl implements UserRepository {

    private final UserMapper userMapper;
    private final UserConverter userConverter;

    public UserRepositoryImpl(UserMapper userMapper, UserConverter userConverter) {
        this.userMapper = userMapper;
        this.userConverter = userConverter;
    }

    @Override
    public User findById(UserId id) {
        UserPO po = userMapper.selectById(id.value());
        return userConverter.toDomain(po);
    }

    @Override
    public User findByUsername(Username username) {
        UserPO po = userMapper.selectByUsername(username.value());
        return userConverter.toDomain(po);
    }

    @Override
    public void save(User user) {
        UserPO po = userConverter.toPO(user);
        if (po.getId() == null) {
            // 新建，ID 由数据库生成
            userMapper.insert(po);
            // 将生成的 ID 设置回领域对象
            user.setIdAfterPersist(po.getId());
        } else {
            // 更新
            userMapper.update(po);
        }
    }

    @Override
    public void delete(UserId id) {
        userMapper.deleteById(id.value());
    }

    @Override
    public boolean existsByUsername(Username username) {
        return userMapper.countByUsername(username.value()) > 0;
    }

    @Override
    public List<User> findAll(String username, Integer status, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<UserPO> pos = userMapper.selectByCondition(username, status);
        return pos.stream()
            .map(userConverter::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public long count(String username, Integer status) {
        return userMapper.countByCondition(username, status);
    }

    @Override
    public List<User> findAll() {
        List<UserPO> pos = userMapper.selectAll();
        return pos.stream()
            .map(userConverter::toDomain)
            .collect(Collectors.toList());
    }
}