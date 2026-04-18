package com.jguard.infrastructure.persistence.repository;

import com.jguard.domain.permission.aggregate.Resource;
import com.jguard.domain.permission.repository.ResourceRepository;
import com.jguard.domain.permission.valueobject.ResourceType;
import com.jguard.infrastructure.persistence.mapper.ResourceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Resource Repository Implementation
 * Implements ResourceRepository using MyBatis mapper
 */
@Repository
public class ResourceRepositoryImpl implements ResourceRepository {

    private final ResourceMapper resourceMapper;

    public ResourceRepositoryImpl(ResourceMapper resourceMapper) {
        this.resourceMapper = resourceMapper;
    }

    @Override
    public Resource save(Resource resource) {
        if (resource.getId() == null) {
            resourceMapper.insert(resource);
        } else {
            resourceMapper.update(resource);
        }
        return resource;
    }

    @Override
    public Optional<Resource> findById(Long id) {
        Resource resource = resourceMapper.findById(id);
        return Optional.ofNullable(resource);
    }

    @Override
    public Optional<Resource> findByCode(String code) {
        Resource resource = resourceMapper.findByCode(code);
        return Optional.ofNullable(resource);
    }

    @Override
    public List<Resource> findAll() {
        return resourceMapper.findAll();
    }

    @Override
    public List<Resource> findByType(ResourceType type) {
        return resourceMapper.findByType(type);
    }

    @Override
    public List<Resource> findByParentId(Long parentId) {
        return resourceMapper.findByParentId(parentId);
    }

    @Override
    public List<Resource> findAllApis() {
        return resourceMapper.findAllApis();
    }

    @Override
    public void deleteById(Long id) {
        resourceMapper.softDelete(id);
    }
}