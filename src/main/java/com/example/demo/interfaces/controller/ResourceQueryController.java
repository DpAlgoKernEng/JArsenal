package com.example.demo.interfaces.controller;

import com.example.demo.infrastructure.annotation.RateLimit;
import com.example.demo.application.service.ResourceApplicationService;
import com.example.demo.infrastructure.common.Result;
import com.example.demo.domain.permission.aggregate.Resource;
import com.example.demo.domain.permission.entity.ResourceField;
import com.example.demo.interfaces.assembler.ResourceAssembler;
import com.example.demo.interfaces.dto.response.ResourceResponse;
import com.example.demo.interfaces.dto.response.ResourceTreeResponse;
import com.example.demo.interfaces.dto.response.SensitiveFieldResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 资源查询控制器 (CQRS - 读操作)
 */
@Tag(name = "资源管理", description = "资源查询接口")
@RestController
@RequestMapping("/api/v1/resources")
@RequiredArgsConstructor
public class ResourceQueryController {

    private final ResourceApplicationService resourceApplicationService;
    private final ResourceAssembler resourceAssembler;

    /**
     * 查询资源列表（树结构）
     */
    @Operation(summary = "查询资源树", description = "返回所有资源的树形结构")
    @RateLimit(key = "resourceTree", time = 60, count = 100)
    @GetMapping
    public Result<List<ResourceResponse>> getResourceTree() {
        List<ResourceResponse> tree = resourceApplicationService.getResourceTreeResponse();
        return Result.success(tree);
    }

    /**
     * 查询资源树结构（V2版本）
     */
    @Operation(summary = "查询资源树V2", description = "返回所有资源的树形结构（使用ResourceTreeResponse）")
    @RateLimit(key = "resourceTreeV2", time = 60, count = 100)
    @GetMapping("/tree")
    public Result<List<ResourceTreeResponse>> getResourceTreeV2() {
        List<ResourceTreeResponse> tree = resourceApplicationService.getResourceTreeResponseV2();
        return Result.success(tree);
    }

    /**
     * 根据ID查询资源详情
     */
    @Operation(summary = "根据ID查询资源", description = "获取单个资源详细信息")
    @RateLimit(key = "resourceDetail", time = 60, count = 100)
    @GetMapping("/{id}")
    public Result<ResourceResponse> getResource(
            @Parameter(description = "资源ID") @PathVariable Long id) {
        ResourceResponse resource = resourceApplicationService.getResourceDetail(id);
        if (resource == null) {
            return Result.error(404, "资源不存在");
        }
        return Result.success(resource);
    }

    /**
     * 查询资源的敏感字段列表
     */
    @Operation(summary = "查询敏感字段", description = "获取资源关联的敏感字段配置")
    @RateLimit(key = "resourceFields", time = 60, count = 100)
    @GetMapping("/{id}/fields")
    public Result<List<SensitiveFieldResponse>> getResourceFields(
            @Parameter(description = "资源ID") @PathVariable Long id) {
        List<SensitiveFieldResponse> fields = resourceApplicationService.getSensitiveFieldResponses(id);
        return Result.success(fields);
    }

    /**
     * 查询所有API资源
     */
    @Operation(summary = "查询API资源", description = "获取所有API类型的资源")
    @RateLimit(key = "apiResources", time = 60, count = 100)
    @GetMapping("/apis")
    public Result<List<ResourceResponse>> getApiResources() {
        List<Resource> apis = resourceApplicationService.getAllApiResources();
        List<ResourceResponse> responses = resourceAssembler.toResponseList(apis);
        return Result.success(responses);
    }
}