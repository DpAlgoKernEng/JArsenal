package com.jguard.interfaces.controller;

import com.jguard.application.service.ResourceApplicationService;
import com.jguard.domain.permission.aggregate.Resource;
import com.jguard.domain.permission.entity.ResourceField;
import com.jguard.domain.permission.valueobject.ResourceType;
import com.jguard.domain.permission.valueobject.SensitiveLevel;
import com.jguard.interfaces.assembler.ResourceAssembler;
import com.jguard.interfaces.dto.response.ResourceResponse;
import com.jguard.interfaces.dto.response.ResourceTreeResponse;
import com.jguard.interfaces.dto.response.SensitiveFieldResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ResourceQueryController 测试
 */
@ExtendWith(MockitoExtension.class)
class ResourceQueryControllerTest {

    @Mock
    private ResourceApplicationService resourceApplicationService;

    @Mock
    private ResourceAssembler resourceAssembler;

    @InjectMocks
    private ResourceQueryController resourceQueryController;

    private Resource testResource;
    private ResourceField testField;

    @BeforeEach
    void setUp() {
        testResource = Resource.createMenu("TEST_MENU", "测试菜单", "/test", "TestIcon", "TestComponent");
        testResource.setId(1L);

        testField = new ResourceField();
        testField.setId(1L);
        testField.setResourceId(1L);
        testField.setFieldCode("testField");
        testField.setFieldName("测试字段");
        testField.setSensitiveLevel(SensitiveLevel.ENCRYPTED);
        testField.setMaskPattern("PHONE");
    }

    @Test
    @DisplayName("查询资源树 - 成功")
    void getResourceTree_success() {
        List<ResourceResponse> mockTree = new ArrayList<>();
        ResourceResponse response = new ResourceResponse();
        response.setId(1L);
        response.setCode("TEST_MENU");
        response.setName("测试菜单");
        mockTree.add(response);

        when(resourceApplicationService.getResourceTreeResponse()).thenReturn(mockTree);

        var result = resourceQueryController.getResourceTree();

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
        verify(resourceApplicationService).getResourceTreeResponse();
    }

    @Test
    @DisplayName("查询资源树V2 - 成功")
    void getResourceTreeV2_success() {
        List<ResourceTreeResponse> mockTree = new ArrayList<>();
        ResourceTreeResponse response = new ResourceTreeResponse(
            1L, "TEST_MENU", "测试菜单", "MENU", null, "/test", null, null, "TestIcon", "TestComponent", 0, true, new ArrayList<>()
        );
        mockTree.add(response);

        when(resourceApplicationService.getResourceTreeResponseV2()).thenReturn(mockTree);

        var result = resourceQueryController.getResourceTreeV2();

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
        verify(resourceApplicationService).getResourceTreeResponseV2();
    }

    @Test
    @DisplayName("根据ID查询资源 - 成功")
    void getResource_success() {
        ResourceResponse mockResponse = new ResourceResponse();
        mockResponse.setId(1L);
        mockResponse.setCode("TEST_MENU");
        mockResponse.setName("测试菜单");
        mockResponse.setType("MENU");

        when(resourceApplicationService.getResourceDetail(1L)).thenReturn(mockResponse);

        var result = resourceQueryController.getResource(1L);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("TEST_MENU", result.getData().getCode());
        verify(resourceApplicationService).getResourceDetail(1L);
    }

    @Test
    @DisplayName("根据ID查询资源 - 资源不存在")
    void getResource_notFound() {
        when(resourceApplicationService.getResourceDetail(999L)).thenReturn(null);

        var result = resourceQueryController.getResource(999L);

        assertNotNull(result);
        assertEquals(404, result.getCode());
        assertEquals("资源不存在", result.getMessage());
    }

    @Test
    @DisplayName("查询敏感字段列表 - 成功")
    void getResourceFields_success() {
        List<SensitiveFieldResponse> mockFields = new ArrayList<>();
        SensitiveFieldResponse fieldResponse = new SensitiveFieldResponse(
            1L, "testField", "测试字段", "ENCRYPTED", "PHONE"
        );
        mockFields.add(fieldResponse);

        when(resourceApplicationService.getSensitiveFieldResponses(1L)).thenReturn(mockFields);

        var result = resourceQueryController.getResourceFields(1L);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
        assertEquals("testField", result.getData().get(0).getFieldCode());
    }

    @Test
    @DisplayName("查询所有API资源 - 成功")
    void getApiResources_success() {
        Resource apiResource = Resource.createApi("TEST_API", "测试API", "/api/test/**", "GET");
        apiResource.setId(2L);
        List<Resource> apis = List.of(apiResource);

        List<ResourceResponse> mockResponses = new ArrayList<>();
        ResourceResponse response = new ResourceResponse();
        response.setId(2L);
        response.setCode("TEST_API");
        response.setType("API");
        mockResponses.add(response);

        when(resourceApplicationService.getAllApiResources()).thenReturn(apis);
        when(resourceAssembler.toResponseList(apis)).thenReturn(mockResponses);

        var result = resourceQueryController.getApiResources();

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
        verify(resourceApplicationService).getAllApiResources();
    }
}