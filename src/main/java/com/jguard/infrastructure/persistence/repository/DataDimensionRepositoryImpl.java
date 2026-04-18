package com.jguard.infrastructure.persistence.repository;

import com.jguard.domain.permission.aggregate.DataDimension;
import com.jguard.domain.permission.repository.DataDimensionRepository;
import com.jguard.infrastructure.persistence.mapper.DataDimensionMapper;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * 数据维度仓储实现
 */
@Repository
public class DataDimensionRepositoryImpl implements DataDimensionRepository {

    private final DataDimensionMapper mapper;

    public DataDimensionRepositoryImpl(DataDimensionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<DataDimension> findByCode(String code) {
        return mapper.findByCode(code);
    }

    @Override
    public List<DataDimension> findAllEnabled() {
        return mapper.findAllEnabled();
    }

    @Override
    public DataDimension save(DataDimension dimension) {
        if (dimension.getId() == null) {
            mapper.insert(dimension);
        } else {
            mapper.update(dimension);
        }
        return dimension;
    }

    @Override
    public Optional<DataDimension> findById(Long id) {
        return mapper.findById(id);
    }
}