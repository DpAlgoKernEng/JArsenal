package com.jguard.infrastructure.persistence.repository;

import com.jguard.domain.permission.entity.ResourceField;
import com.jguard.domain.permission.repository.ResourceFieldRepository;
import com.jguard.infrastructure.persistence.mapper.ResourceFieldMapper;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Resource Field Repository Implementation
 */
@Repository
public class ResourceFieldRepositoryImpl implements ResourceFieldRepository {

    private final ResourceFieldMapper resourceFieldMapper;

    public ResourceFieldRepositoryImpl(ResourceFieldMapper resourceFieldMapper) {
        this.resourceFieldMapper = resourceFieldMapper;
    }

    @Override
    public ResourceField save(ResourceField field) {
        if (field.getId() == null) {
            resourceFieldMapper.insert(field);
        } else {
            resourceFieldMapper.update(field);
        }
        return field;
    }

    @Override
    public Optional<ResourceField> findById(Long id) {
        ResourceField field = resourceFieldMapper.findById(id);
        return Optional.ofNullable(field);
    }

    @Override
    public List<ResourceField> findByResourceId(Long resourceId) {
        return resourceFieldMapper.findByResourceId(resourceId);
    }

    @Override
    public Optional<ResourceField> findByResourceAndCode(Long resourceId, String fieldCode) {
        ResourceField field = resourceFieldMapper.findByResourceAndCode(resourceId, fieldCode);
        return Optional.ofNullable(field);
    }

    @Override
    public void deleteById(Long id) {
        resourceFieldMapper.deleteById(id);
    }

    @Override
    public void deleteByResourceId(Long resourceId) {
        resourceFieldMapper.deleteByResourceId(resourceId);
    }
}