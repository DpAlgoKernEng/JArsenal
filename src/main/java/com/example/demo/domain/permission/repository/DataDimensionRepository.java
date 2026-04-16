package com.example.demo.domain.permission.repository;

import com.example.demo.domain.permission.aggregate.DataDimension;
import java.util.List;
import java.util.Optional;

/**
 * 数据维度仓储接口
 */
public interface DataDimensionRepository {

    /**
     * 根据编码查找维度
     */
    Optional<DataDimension> findByCode(String code);

    /**
     * 查找所有启用的维度
     */
    List<DataDimension> findAllEnabled();

    /**
     * 保存维度
     */
    DataDimension save(DataDimension dimension);

    /**
     * 根据ID查找维度
     */
    Optional<DataDimension> findById(Long id);
}