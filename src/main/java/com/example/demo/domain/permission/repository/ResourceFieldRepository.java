package com.example.demo.domain.permission.repository;

import com.example.demo.domain.permission.entity.ResourceField;
import java.util.List;
import java.util.Optional;

/**
 * 资源字段仓储接口
 * 定义资源字段持久化操作契约
 */
public interface ResourceFieldRepository {

    /**
     * 保存资源字段
     */
    ResourceField save(ResourceField field);

    /**
     * 根据ID查找资源字段
     */
    Optional<ResourceField> findById(Long id);

    /**
     * 根据资源ID查找所有字段
     */
    List<ResourceField> findByResourceId(Long resourceId);

    /**
     * 根据资源ID和字段编码查找字段
     */
    Optional<ResourceField> findByResourceAndCode(Long resourceId, String fieldCode);

    /**
     * 删除资源字段
     */
    void deleteById(Long id);

    /**
     * 根据资源ID删除所有字段
     */
    void deleteByResourceId(Long resourceId);
}