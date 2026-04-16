package com.example.demo.interfaces.assembler;

import com.example.demo.domain.permission.aggregate.Role;
import com.example.demo.interfaces.dto.response.RoleResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Role 转换器
 */
@Component
public class RoleAssembler {

    /**
     * 领域对象转响应 DTO
     */
    public RoleResponse toResponse(Role role) {
        if (role == null) {
            return null;
        }
        RoleResponse response = new RoleResponse();
        response.setId(role.getId());
        response.setCode(role.getCode().value());
        response.setName(role.getName());
        response.setParentId(role.getParentId());
        response.setStatus(role.getStatus().name());
        response.setInheritMode(role.getInheritMode().name());
        response.setBuiltin(role.isBuiltin());
        response.setSort(role.getSort());
        // createTime and updateTime are not available in Role aggregate yet
        return response;
    }

    /**
     * 领域对象列表转响应 DTO 列表
     */
    public List<RoleResponse> toResponseList(List<Role> roles) {
        return roles.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * 构建角色树结构
     */
    public List<RoleResponse> buildRoleTree(List<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return new ArrayList<>();
        }

        // 转换为响应对象
        List<RoleResponse> responses = toResponseList(roles);

        // 按父ID分组
        Map<Long, List<RoleResponse>> parentMap = responses.stream()
            .collect(Collectors.groupingBy(r -> r.getParentId() != null ? r.getParentId() : 0L));

        // 设置子角色
        responses.forEach(r -> r.setChildren(parentMap.getOrDefault(r.getId(), new ArrayList<>())));

        // 返回顶层角色（parentId为null或0）
        return responses.stream()
            .filter(r -> r.getParentId() == null || r.getParentId() == 0L)
            .collect(Collectors.toList());
    }
}