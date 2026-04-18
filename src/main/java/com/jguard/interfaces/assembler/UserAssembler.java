package com.jguard.interfaces.assembler;

import com.jguard.domain.user.aggregate.User;
import com.jguard.interfaces.dto.response.UserResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * User 转换器
 */
@Component
public class UserAssembler {

    /**
     * 领域对象转响应 DTO
     */
    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }
        UserResponse response = new UserResponse();
        response.setId(user.getId().value());
        response.setUsername(user.getUsername().value());
        response.setEmail(user.getEmail().value());
        response.setStatus(user.getStatus().code());
        response.setCreateTime(user.getCreateTime());
        response.setUpdateTime(user.getUpdateTime());
        return response;
    }

    /**
     * 领域对象列表转响应 DTO 列表
     */
    public List<UserResponse> toResponseList(List<User> users) {
        return users.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
}