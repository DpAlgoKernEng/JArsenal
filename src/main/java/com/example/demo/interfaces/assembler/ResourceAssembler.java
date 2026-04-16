package com.example.demo.interfaces.assembler;

import com.example.demo.domain.permission.aggregate.Resource;
import com.example.demo.domain.permission.entity.ResourceField;
import com.example.demo.interfaces.dto.response.ResourceResponse;
import com.example.demo.interfaces.dto.response.ResourceTreeResponse;
import com.example.demo.interfaces.dto.response.SensitiveFieldResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Resource 转换器
 */
@Component
public class ResourceAssembler {

    /**
     * 领域对象转响应 DTO
     */
    public ResourceResponse toResponse(Resource resource, List<ResourceField> fields) {
        if (resource == null) {
            return null;
        }
        ResourceResponse response = new ResourceResponse();
        response.setId(resource.getId());
        response.setCode(resource.getCode());
        response.setName(resource.getName());
        response.setParentId(resource.getParentId());
        response.setType(resource.getType().name());
        response.setPath(resource.getPath());
        response.setPathPattern(resource.getPathPattern());
        response.setMethod(resource.getMethod());
        response.setIcon(resource.getIcon());
        response.setComponent(resource.getComponent());
        response.setSort(resource.getSort());
        response.setStatus(resource.isStatus());

        if (fields != null && !fields.isEmpty()) {
            List<SensitiveFieldResponse> sensitiveFields = fields.stream()
                .map(f -> new SensitiveFieldResponse(
                    f.getId(),
                    f.getFieldCode(),
                    f.getFieldName(),
                    f.getSensitiveLevel().name(),
                    f.getMaskPattern()
                ))
                .collect(Collectors.toList());
            response.setSensitiveFields(sensitiveFields);
        } else {
            response.setSensitiveFields(new ArrayList<>());
        }

        return response;
    }

    /**
     * 领域对象转响应 DTO（无敏感字段）
     */
    public ResourceResponse toResponse(Resource resource) {
        return toResponse(resource, null);
    }

    /**
     * 领域对象列表转响应 DTO 列表
     */
    public List<ResourceResponse> toResponseList(List<Resource> resources) {
        return resources.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * 构建资源树结构
     */
    public List<ResourceResponse> buildResourceTree(List<Resource> resources) {
        if (resources == null || resources.isEmpty()) {
            return new ArrayList<>();
        }

        // 转换为响应对象
        List<ResourceResponse> responses = toResponseList(resources);

        // 按父ID分组
        Map<Long, List<ResourceResponse>> parentMap = responses.stream()
            .collect(Collectors.groupingBy(r -> r.getParentId() != null ? r.getParentId() : 0L));

        // 设置子资源
        responses.forEach(r -> r.setChildren(parentMap.getOrDefault(r.getId(), new ArrayList<>())));

        // 返回顶层资源（parentId为null或0）
        return responses.stream()
            .filter(r -> r.getParentId() == null || r.getParentId() == 0L)
            .sorted((a, b) -> Integer.compare(a.getSort(), b.getSort()))
            .collect(Collectors.toList());
    }

    /**
     * 转换为树响应对象
     */
    public ResourceTreeResponse toTreeResponse(Resource resource) {
        if (resource == null) {
            return null;
        }
        return new ResourceTreeResponse(
            resource.getId(),
            resource.getCode(),
            resource.getName(),
            resource.getType().name(),
            resource.getParentId(),
            resource.getPath(),
            resource.getPathPattern(),
            resource.getMethod(),
            resource.getIcon(),
            resource.getComponent(),
            resource.getSort(),
            resource.isStatus(),
            new ArrayList<>
()
        );
    }

    /**
     * 构建ResourceTreeResponse树结构
     */
    public List<ResourceTreeResponse> buildTreeResponse(List<Resource> resources) {
        if (resources == null || resources.isEmpty()) {
            return new ArrayList<>();
        }

        List<ResourceTreeResponse> responses = resources.stream()
            .map(this::toTreeResponse)
            .collect(Collectors.toList());

        Map<Long, List<ResourceTreeResponse>> parentMap = responses.stream()
            .collect(Collectors.groupingBy(r -> r.getParentId() != null ? r.getParentId() : 0L));

        responses.forEach(r -> r.setChildren(parentMap.getOrDefault(r.getId(), new ArrayList<>())));

        return responses.stream()
            .filter(r -> r.getParentId() == null || r.getParentId() == 0L)
            .sorted((a, b) -> Integer.compare(a.getSort(), b.getSort()))
            .collect(Collectors.toList());
    }
}