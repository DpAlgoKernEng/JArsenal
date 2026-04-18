package com.jguard.domain.permission.repository;

import com.jguard.domain.permission.aggregate.Resource;
import com.jguard.domain.permission.valueobject.ResourceType;
import java.util.List;
import java.util.Optional;

/**
 * 资源仓储接口
 * 定义资源持久化操作契约
 */
public interface ResourceRepository {

    /**
     * 保存资源
     */
    Resource save(Resource resource);

    /**
     * 根据ID查找资源
     */
    Optional<Resource> findById(Long id);

    /**
     * 根据编码查找资源
     */
    Optional<Resource> findByCode(String code);

    /**
     * 查找所有资源
     */
    List<Resource> findAll();

    /**
     * 根据类型查找资源
     */
    List<Resource> findByType(ResourceType type);

    /**
     * 根据父资源ID查找子资源
     */
    List<Resource> findByParentId(Long parentId);

    /**
     * 查找所有API类型资源
     */
    List<Resource> findAllApis();

    /**
     * 删除资源
     */
    void deleteById(Long id);
}