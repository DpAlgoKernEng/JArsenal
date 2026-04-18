package com.jguard.domain.permission.repository;

import java.util.List;
import java.util.Set;

/**
 * 用户维度仓储接口
 * 存储用户与维度值的关联关系（如用户所属部门）
 */
public interface UserDimensionRepository {

    /**
     * 获取用户指定维度的值
     */
    Long getValueByDimension(Long userId, String dimensionCode);

    /**
     * 获取用户指定维度的所有值（多值场景）
     */
    List<Long> findValuesByDimension(Long userId, String dimensionCode);

    /**
     * 分配用户维度值
     */
    void assignDimension(Long userId, String dimensionCode, Long valueId);

    /**
     * 移除用户维度值
     */
    void removeDimension(Long userId, String dimensionCode);

    /**
     * 移除用户所有维度值
     */
    void removeByUserId(Long userId);
}