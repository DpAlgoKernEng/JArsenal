package com.jguard.domain.user.repository;

import com.jguard.domain.user.aggregate.User;
import com.jguard.domain.user.valueobject.UserId;
import com.jguard.domain.user.valueobject.Username;
import java.util.List;

/**
 * 用户仓库接口
 * 定义在领域层，实现在基础设施层（依赖倒置）
 */
public interface UserRepository {

    /**
     * 根据ID查找用户
     */
    User findById(UserId id);

    /**
     * 根据用户名查找用户
     */
    User findByUsername(Username username);

    /**
     * 保存用户
     */
    void save(User user);

    /**
     * 删除用户
     */
    void delete(UserId id);

    /**
     * 检查用户名是否已存在
     */
    boolean existsByUsername(Username username);

    /**
     * 分页查询用户列表
     */
    List<User> findAll(String username, Integer status, int pageNum, int pageSize);

    /**
     * 统计符合条件的用户总数
     */
    long count(String username, Integer status);

    /**
     * 查找所有用户
     */
    List<User> findAll();
}