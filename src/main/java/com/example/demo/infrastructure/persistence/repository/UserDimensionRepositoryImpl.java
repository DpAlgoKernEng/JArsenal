package com.example.demo.infrastructure.persistence.repository;

import com.example.demo.domain.permission.repository.UserDimensionRepository;
import com.example.demo.infrastructure.persistence.mapper.UserDimensionMapper;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 用户维度仓储实现
 */
@Repository
public class UserDimensionRepositoryImpl implements UserDimensionRepository {

    private final UserDimensionMapper mapper;

    public UserDimensionRepositoryImpl(UserDimensionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Long getValueByDimension(Long userId, String dimensionCode) {
        return mapper.getValueByDimension(userId, dimensionCode);
    }

    @Override
    public List<Long> findValuesByDimension(Long userId, String dimensionCode) {
        return mapper.findValuesByDimension(userId, dimensionCode);
    }

    @Override
    public void assignDimension(Long userId, String dimensionCode, Long valueId) {
        // 先删除旧值
        mapper.deleteByUserAndDimension(userId, dimensionCode);
        // 再插入新值
        mapper.insert(userId, dimensionCode, valueId);
    }

    @Override
    public void removeDimension(Long userId, String dimensionCode) {
        mapper.deleteByUserAndDimension(userId, dimensionCode);
    }

    @Override
    public void removeByUserId(Long userId) {
        mapper.deleteByUserId(userId);
    }
}