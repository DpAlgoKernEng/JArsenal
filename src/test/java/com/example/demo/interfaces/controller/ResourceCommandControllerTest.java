package com.example.demo.interfaces.controller;

import com.example.demo.application.command.AddSensitiveFieldCommand;
import com.example.demo.application.command.CreateResourceCommand;
import com.example.demo.application.command.UpdateResourceCommand;
import com.example.demo.application.service.ResourceApplicationService;
import com.example.demo.interfaces.dto.request.ResourceCreateRequest;
import com.example.demo.interfaces.dto.request.ResourceUpdateRequest;
import com.example.demo.interfaces.dto.request.SensitiveFieldRequest;
import com.example.demo.interfaces.dto.response.ResourceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ResourceCommandController 测试
 */
@ExtendWith(MockitoExtension.class)
class ResourceCommandControllerTest {

    @Mock
    private ResourceApplicationService resourceApplicationService;

    @InjectMocks
    private ResourceCommandController resourceCommandController;

    private ResourceCreateRequest createRequest;
    private ResourceUpdateRequest updateRequest;
    private SensitiveFieldRequest fieldRequest;

    @BeforeEach
    void setUp() {
        createRequest = new ResourceCreateRequest();
        createRequest.setCode("TEST_MENU");
        createRequest.setName("测试菜单");
        createRequest.setType("MENU");
        createRequest.setPath("/test");
        createRequest.setIcon("TestIcon");
        createRequest.setComponent("TestComponent");
        createRequest.setSort(1);

        updateRequest = new ResourceUpdateRequest();
        updateRequest.setName("更新菜单");
        updateRequest.setPath("/test/updated");
        updateRequest.setSort(2);

        fieldRequest = new SensitiveFieldRequest();
        fieldRequest.setFieldCode("testField");
        fieldRequest.setFieldName("测试字段");
        fieldRequest.setSensitiveLevel("ENCRYPTED");
        fieldRequest.setMaskPattern("PHONE");
    }

    @Test
    @DisplayName("创建资源 - 成功")
    void createResource_success() {
        when(resourceApplicationService.createResource(any(CreateResourceCommand.class))).thenReturn(1L);

        ResourceResponse mockResponse = new ResourceResponse();
        mockResponse.setId(1L);
        mockResponse.setCode("TEST_MENU");
        mockResponse.setName("测试菜单");
        when(resourceApplicationService.getResourceDetail(1L)).thenReturn(mockResponse);

        var result = resourceCommandController.createResource(createRequest);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("TEST_MENU", result.getData().getCode());
        verify(resourceApplicationService).createResource(any(CreateResourceCommand.class));
        verify(resourceApplicationService).getResourceDetail(1L);
    }

    @Test
    @DisplayName("创建API资源 - 成功")
    void createApiResource_success() {
        ResourceCreateRequest apiRequest = new ResourceCreateRequest();
        apiRequest.setCode("TEST_API");
        apiRequest.setName("测试API");
        apiRequest.setType("API");
        apiRequest.setPathPattern("/api/test/**");
        apiRequest.setMethod("GET");

        when(resourceApplicationService.createResource(any(CreateResourceCommand.class))).thenReturn(2L);

        ResourceResponse mockResponse = new ResourceResponse();
        mockResponse.setId(2L);
        mockResponse.setCode("TEST_API");
        mockResponse.setType("API");
        when(resourceApplicationService.getResourceDetail(2L)).thenReturn(mockResponse);

        var result = resourceCommandController.createResource(apiRequest);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("API", result.getData().getType());
    }

    @Test
    @DisplayName("更新资源 - 成功")
    void updateResource_success() {
        ResourceResponse mockResponse = new ResourceResponse();
        mockResponse.setId(1L);
        mockResponse.setName("更新菜单");
        when(resourceApplicationService.getResourceDetail(1L)).thenReturn(mockResponse);

        var result = resourceCommandController.updateResource(1L, updateRequest);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("更新菜单", result.getData().getName());
        verify(resourceApplicationService).updateResource(any(UpdateResourceCommand.class));
    }

    @Test
    @DisplayName("删除资源 - 成功")
    void deleteResource_success() {
        doNothing().when(resourceApplicationService).deleteResource(1L);

        var result = resourceCommandController.deleteResource(1L);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        verify(resourceApplicationService).deleteResource(1L);
    }

    @Test
    @DisplayName("添加敏感字段 - 成功")
    void addSensitiveField_success() {
        doNothing().when(resourceApplicationService).addSensitiveField(any(AddSensitiveFieldCommand.class));

        var result = resourceCommandController.addSensitiveField(1L, fieldRequest);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        verify(resourceApplicationService).addSensitiveField(any(AddSensitiveFieldCommand.class));
    }

    @Test
    @DisplayName("启用资源 - 成功")
    void enableResource_success() {
        doNothing().when(resourceApplicationService).updateResource(any(UpdateResourceCommand.class));

        var result = resourceCommandController.enableResource(1L);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        verify(resourceApplicationService).updateResource(any(UpdateResourceCommand.class));
    }

    @Test
    @DisplayName("禁用资源 - 成功")
    void disableResource_success() {
        doNothing().when(resourceApplicationService).updateResource(any(UpdateResourceCommand.class));

        var result = resourceCommandController.disableResource(1L);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        verify(resourceApplicationService).updateResource(any(UpdateResourceCommand.class));
    }
}