package com.jguard.application.service;

import com.jguard.application.command.AddSensitiveFieldCommand;
import com.jguard.application.command.CreateResourceCommand;
import com.jguard.application.command.UpdateResourceCommand;
import com.jguard.domain.permission.aggregate.Resource;
import com.jguard.domain.permission.entity.ResourceField;
import com.jguard.domain.permission.repository.ResourceFieldRepository;
import com.jguard.domain.permission.repository.ResourceRepository;
import com.jguard.domain.permission.valueobject.ResourceType;
import com.jguard.domain.permission.valueobject.SensitiveLevel;
import com.jguard.domain.shared.exception.DomainException;
import com.jguard.interfaces.assembler.ResourceAssembler;
import com.jguard.interfaces.dto.response.ResourceResponse;
import com.jguard.interfaces.dto.response.ResourceTreeResponse;
import com.jguard.interfaces.dto.response.SensitiveFieldResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 资源应用服务
 * 协调领域对象完成资源管理业务用例
 */
@Service
@Transactional
public class ResourceApplicationService {

    private final ResourceRepository resourceRepository;
    private final ResourceFieldRepository resourceFieldRepository;
    private final ResourceAssembler resourceAssembler;

    public ResourceApplicationService(ResourceRepository resourceRepository,
                                       ResourceFieldRepository resourceFieldRepository,
                                       ResourceAssembler resourceAssembler) {
        this.resourceRepository = resourceRepository;
        this.resourceFieldRepository = resourceFieldRepository;
        this.resourceAssembler = resourceAssembler;
    }

    /**
     * 创建资源
     */
    public Long createResource(CreateResourceCommand command) {
        // 检查编码是否已存在
        if (resourceRepository.findByCode(command.getCode()).isPresent()) {
            throw new DomainException("资源编码已存在: " + command.getCode());
        }

        // 检查父资源是否存在（如果指定了父资源）
        if (command.getParentId() != null && command.getParentId() > 0) {
            Optional<Resource> parentResource = resourceRepository.findById(command.getParentId());
            if (parentResource.isEmpty()) {
                throw new DomainException("父资源不存在: " + command.getParentId());
            }
        }

        // 根据类型创建资源
        ResourceType type = ResourceType.valueOf(command.getType());
        Resource resource = switch (type) {
            case MENU -> Resource.createMenu(
                command.getCode(),
                command.getName(),
                command.getPath(),
                command.getIcon(),
                command.getComponent()
            );
            case OPERATION -> Resource.createOperation(
                command.getCode(),
                command.getName(),
                command.getParentId()
            );
            case API -> Resource.createApi(
                command.getCode(),
                command.getName(),
                command.getPathPattern(),
                command.getMethod()
            );
        };

        // 设置排序和父资源
        resource.setSort(command.getSort() != null ? command.getSort() : 0);
        if (command.getParentId() != null && type != ResourceType.MENU) {
            resource.setParentId(command.getParentId());
        }

        // 设置数据维度编码
        if (command.getDataDimensionCode() != null) {
            resource.setDataDimensionCode(command.getDataDimensionCode());
        }

        // 持久化
        resourceRepository.save(resource);

        return resource.getId();
    }

    /**
     * 更新资源
     */
    public void updateResource(UpdateResourceCommand command) {
        Resource resource = resourceRepository.findById(command.getResourceId())
            .orElseThrow(() -> new DomainException("资源不存在: " + command.getResourceId()));

        // 更新基本信息
        if (command.getName() != null) {
            resource.setName(command.getName());
        }

        if (command.getParentId() != null) {
            // 检查是否会造成循环引用
            validateNoCircularReference(resource.getId(), command.getParentId());
            resource.setParentId(command.getParentId());
        }

        if (command.getPath() != null) {
            resource.setPath(command.getPath());
        }

        if (command.getIcon() != null) {
            resource.setIcon(command.getIcon());
        }

        if (command.getSort() != null) {
            resource.setSort(command.getSort());
        }

        // API类型资源可以更新pathPattern和method
        if (resource.getType() == ResourceType.API) {
            // Note: Resource aggregate doesn't have setters for pathPattern/method yet
            // This would need to be added to the aggregate if needed
        }

        // 更新状态
        if (command.getStatus() != null) {
            boolean enabled = "ENABLED".equals(command.getStatus());
            resource.setStatus(enabled);
        }

        // 更新数据维度编码
        if (command.getDataDimensionCode() != null) {
            resource.setDataDimensionCode(command.getDataDimensionCode());
        }

        resourceRepository.save(resource);
    }

    /**
     * 验证不会造成循环引用
     */
    private void validateNoCircularReference(Long resourceId, Long newParentId) {
        if (newParentId == null || newParentId == 0L) {
            return;
        }

        // 不能将自己设为自己的父资源
        if (resourceId.equals(newParentId)) {
            throw new DomainException("不能将资源设为自己的父资源");
        }

        // 检查新父资源的祖先链中是否包含当前资源
        Resource parentResource = resourceRepository.findById(newParentId)
            .orElseThrow(() -> new DomainException("父资源不存在: " + newParentId));

        Long currentParentId = parentResource.getParentId();
        while (currentParentId != null && currentParentId > 0L) {
            if (currentParentId.equals(resourceId)) {
                throw new DomainException("会造成循环引用: 资源 " + resourceId + " 不能作为资源 " + newParentId + " 的子资源");
            }
            Resource ancestor = resourceRepository.findById(currentParentId).orElse(null);
            if (ancestor == null) {
                break;
            }
            currentParentId = ancestor.getParentId();
        }
    }

    /**
     * 软删除资源
     */
    public void deleteResource(Long resourceId) {
        Resource resource = resourceRepository.findById(resourceId)
            .orElseThrow(() -> new DomainException("资源不存在: " + resourceId));

        // 检查是否有子资源
        List<Resource> children = resourceRepository.findByParentId(resourceId);
        if (!children.isEmpty()) {
            throw new DomainException("资源存在子资源，无法删除。子资源数: " + children.size());
        }

        // 检查资源是否被权限引用（需要查询permission表）
        // This would require PermissionRepository check if needed

        resource.softDelete();
        resourceRepository.save(resource);
    }

    /**
     * 根据ID查询资源
     */
    @Transactional(readOnly = true)
    public Resource getResourceById(Long resourceId) {
        return resourceRepository.findById(resourceId).orElse(null);
    }

    /**
     * 查询所有资源列表
     */
    @Transactional(readOnly = true)
    public List<Resource> listAllResources() {
        return resourceRepository.findAll();
    }

    /**
     * 查询资源树结构
     */
    @Transactional(readOnly = true)
    public List<Resource> getResourceTree() {
        return resourceRepository.findAll();
    }

    /**
     * 添加敏感字段
     */
    public void addSensitiveField(AddSensitiveFieldCommand command) {
        Resource resource = resourceRepository.findById(command.getResourceId())
            .orElseThrow(() -> new DomainException("资源不存在: " + command.getResourceId()));

        // 检查字段编码是否已存在
        Optional<ResourceField> existingField = resourceFieldRepository.findByResourceAndCode(
            command.getResourceId(), command.getFieldCode());
        if (existingField.isPresent()) {
            throw new DomainException("字段编码已存在: " + command.getFieldCode());
        }

        // 创建敏感字段
        ResourceField field = new ResourceField();
        field.setResourceId(command.getResourceId());
        field.setFieldCode(command.getFieldCode());
        field.setFieldName(command.getFieldName());
        field.setSensitiveLevel(SensitiveLevel.valueOf(command.getSensitiveLevel()));
        field.setMaskPattern(command.getMaskPattern());

        resourceFieldRepository.save(field);
    }

    /**
     * 查询资源的敏感字段列表
     */
    @Transactional(readOnly = true)
    public List<ResourceField> getResourceFields(Long resourceId) {
        return resourceFieldRepository.findByResourceId(resourceId);
    }

    /**
     * 获取资源详情（包含敏感字段）
     */
    @Transactional(readOnly = true)
    public ResourceResponse getResourceDetail(Long resourceId) {
        Resource resource = resourceRepository.findById(resourceId)
            .orElseThrow(() -> new DomainException("资源不存在: " + resourceId));

        List<ResourceField> fields = resourceFieldRepository.findByResourceId(resourceId);
        return resourceAssembler.toResponse(resource, fields);
    }

    /**
     * 获取资源列表响应
     */
    @Transactional(readOnly = true)
    public List<ResourceResponse> listResourcesResponse() {
        List<Resource> resources = resourceRepository.findAll();
        return resourceAssembler.toResponseList(resources);
    }

    /**
     * 获取资源树响应
     */
    @Transactional(readOnly = true)
    public List<ResourceResponse> getResourceTreeResponse() {
        List<Resource> resources = resourceRepository.findAll();
        return resourceAssembler.buildResourceTree(resources);
    }

    /**
     * 获取ResourceTreeResponse树结构
     */
    @Transactional(readOnly = true)
    public List<ResourceTreeResponse> getResourceTreeResponseV2() {
        List<Resource> resources = resourceRepository.findAll();
        return resourceAssembler.buildTreeResponse(resources);
    }

    /**
     * 获取敏感字段响应列表
     */
    @Transactional(readOnly = true)
    public List<SensitiveFieldResponse> getSensitiveFieldResponses(Long resourceId) {
        List<ResourceField> fields = resourceFieldRepository.findByResourceId(resourceId);
        return fields.stream()
            .map(f -> new SensitiveFieldResponse(
                f.getId(),
                f.getFieldCode(),
                f.getFieldName(),
                f.getSensitiveLevel().name(),
                f.getMaskPattern()
            ))
            .collect(Collectors.toList());
    }

    /**
     * 根据类型查询资源
     */
    @Transactional(readOnly = true)
    public List<Resource> getResourcesByType(ResourceType type) {
        return resourceRepository.findByType(type);
    }

    /**
     * 查询所有API资源
     */
    @Transactional(readOnly = true)
    public List<Resource> getAllApiResources() {
        return resourceRepository.findAllApis();
    }
}