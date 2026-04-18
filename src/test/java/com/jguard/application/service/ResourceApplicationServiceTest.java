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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ResourceApplicationService 测试
 */
@ExtendWith(MockitoExtension.class)
class ResourceApplicationServiceTest {

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private ResourceFieldRepository resourceFieldRepository;

    @Mock
    private ResourceAssembler resourceAssembler;

    @InjectMocks
    private ResourceApplicationService resourceApplicationService;

    private Resource testMenuResource;
    private Resource testApiResource;
    private ResourceField testField;
    private CreateResourceCommand createMenuCommand;
    private CreateResourceCommand createApiCommand;

    @BeforeEach
    void setUp() {
        testMenuResource = Resource.createMenu("TEST_MENU", "测试菜单", "/test", "TestIcon", "TestComponent");
        testMenuResource.setId(1L);

        testApiResource = Resource.createApi("TEST_API", "测试API", "/api/test/**", "GET");
        testApiResource.setId(2L);

        testField = new ResourceField();
        testField.setId(1L);
        testField.setResourceId(1L);
        testField.setFieldCode("testField");
        testField.setSensitiveLevel(SensitiveLevel.ENCRYPTED);

        createMenuCommand = new CreateResourceCommand(
            "TEST_MENU", "测试菜单", null, "MENU", "/test", null, null, "TestIcon", "TestComponent", 1, null
        );

        createApiCommand = new CreateResourceCommand(
            "TEST_API", "测试API", null, "API", null, "/api/test/**", "GET", null, null, 1, null
        );
    }

    @Test
    @DisplayName("创建菜单资源 - 成功")
    void createMenuResource_success() {
        when(resourceRepository.findByCode("TEST_MENU")).thenReturn(Optional.empty());
        when(resourceRepository.save(any(Resource.class))).thenAnswer(invocation -> {
            Resource r = invocation.getArgument(0);
            r.setId(1L);
            return r;
        });

        Long resourceId = resourceApplicationService.createResource(createMenuCommand);

        assertNotNull(resourceId);
        assertEquals(1L, resourceId);
        verify(resourceRepository).findByCode("TEST_MENU");
        verify(resourceRepository).save(any(Resource.class));
    }

    @Test
    @DisplayName("创建API资源 - 成功")
    void createApiResource_success() {
        when(resourceRepository.findByCode("TEST_API")).thenReturn(Optional.empty());
        when(resourceRepository.save(any(Resource.class))).thenAnswer(invocation -> {
            Resource r = invocation.getArgument(0);
            r.setId(2L);
            return r;
        });

        Long resourceId = resourceApplicationService.createResource(createApiCommand);

        assertNotNull(resourceId);
        assertEquals(2L, resourceId);
    }

    @Test
    @DisplayName("创建资源 - 编码已存在")
    void createResource_codeExists() {
        when(resourceRepository.findByCode("TEST_MENU")).thenReturn(Optional.of(testMenuResource));

        assertThrows(DomainException.class, () -> {
            resourceApplicationService.createResource(createMenuCommand);
        });

        verify(resourceRepository).findByCode("TEST_MENU");
        verify(resourceRepository, never()).save(any());
    }

    @Test
    @DisplayName("创建资源 - 父资源不存在")
    void createResource_parentNotFound() {
        CreateResourceCommand command = new CreateResourceCommand(
            "TEST_OPERATION", "测试操作", 999L, "OPERATION", null, null, null, null, null, 1, null
        );

        when(resourceRepository.findByCode("TEST_OPERATION")).thenReturn(Optional.empty());
        when(resourceRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> {
            resourceApplicationService.createResource(command);
        });
    }

    @Test
    @DisplayName("更新资源 - 成功")
    void updateResource_success() {
        UpdateResourceCommand command = new UpdateResourceCommand(
            1L, "更新菜单", null, "/test/updated", null, null, "NewIcon", null, null, null, null
        );

        when(resourceRepository.findById(1L)).thenReturn(Optional.of(testMenuResource));
        when(resourceRepository.save(any(Resource.class))).thenReturn(testMenuResource);

        resourceApplicationService.updateResource(command);

        verify(resourceRepository).findById(1L);
        verify(resourceRepository).save(any(Resource.class));
    }

    @Test
    @DisplayName("更新资源 - 资源不存在")
    void updateResource_notFound() {
        UpdateResourceCommand command = new UpdateResourceCommand(999L, "更新菜单", null, null, null, null, null, null, null, null, null);

        when(resourceRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> {
            resourceApplicationService.updateResource(command);
        });
    }

    @Test
    @DisplayName("删除资源 - 成功")
    void deleteResource_success() {
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(testMenuResource));
        when(resourceRepository.findByParentId(1L)).thenReturn(new ArrayList<>());
        when(resourceRepository.save(any(Resource.class))).thenReturn(testMenuResource);

        resourceApplicationService.deleteResource(1L);

        verify(resourceRepository).findById(1L);
        verify(resourceRepository).findByParentId(1L);
        verify(resourceRepository).save(any(Resource.class));
    }

    @Test
    @DisplayName("删除资源 - 存在子资源")
    void deleteResource_hasChildren() {
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(testMenuResource));
        when(resourceRepository.findByParentId(1L)).thenReturn(List.of(testApiResource));

        assertThrows(DomainException.class, () -> {
            resourceApplicationService.deleteResource(1L);
        });

        verify(resourceRepository, never()).save(any());
    }

    @Test
    @DisplayName("添加敏感字段 - 成功")
    void addSensitiveField_success() {
        AddSensitiveFieldCommand command = new AddSensitiveFieldCommand(
            1L, "testField", "测试字段", "ENCRYPTED", "PHONE"
        );

        when(resourceRepository.findById(1L)).thenReturn(Optional.of(testMenuResource));
        when(resourceFieldRepository.findByResourceAndCode(1L, "testField")).thenReturn(Optional.empty());
        when(resourceFieldRepository.save(any(ResourceField.class))).thenAnswer(invocation -> {
            ResourceField f = invocation.getArgument(0);
            f.setId(1L);
            return f;
        });

        resourceApplicationService.addSensitiveField(command);

        verify(resourceRepository).findById(1L);
        verify(resourceFieldRepository).findByResourceAndCode(1L, "testField");
        verify(resourceFieldRepository).save(any(ResourceField.class));
    }

    @Test
    @DisplayName("添加敏感字段 - 字段编码已存在")
    void addSensitiveField_codeExists() {
        AddSensitiveFieldCommand command = new AddSensitiveFieldCommand(
            1L, "testField", "测试字段", "ENCRYPTED", "PHONE"
        );

        when(resourceRepository.findById(1L)).thenReturn(Optional.of(testMenuResource));
        when(resourceFieldRepository.findByResourceAndCode(1L, "testField")).thenReturn(Optional.of(testField));

        assertThrows(DomainException.class, () -> {
            resourceApplicationService.addSensitiveField(command);
        });
    }

    @Test
    @DisplayName("查询资源详情 - 成功")
    void getResourceDetail_success() {
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(testMenuResource));
        when(resourceFieldRepository.findByResourceId(1L)).thenReturn(List.of(testField));

        ResourceResponse mockResponse = new ResourceResponse();
        mockResponse.setId(1L);
        mockResponse.setCode("TEST_MENU");
        when(resourceAssembler.toResponse(any(Resource.class), any(List.class))).thenReturn(mockResponse);

        ResourceResponse response = resourceApplicationService.getResourceDetail(1L);

        assertNotNull(response);
        assertEquals("TEST_MENU", response.getCode());
    }

    @Test
    @DisplayName("查询资源详情 - 资源不存在")
    void getResourceDetail_notFound() {
        when(resourceRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> {
            resourceApplicationService.getResourceDetail(999L);
        });
    }

    @Test
    @DisplayName("查询所有API资源 - 成功")
    void getAllApiResources_success() {
        when(resourceRepository.findAllApis()).thenReturn(List.of(testApiResource));

        List<Resource> apis = resourceApplicationService.getAllApiResources();

        assertNotNull(apis);
        assertEquals(1, apis.size());
        assertEquals(ResourceType.API, apis.get(0).getType());
    }

    @Test
    @DisplayName("循环引用验证 - 自引用")
    void validateCircularReference_selfReference() {
        UpdateResourceCommand command = new UpdateResourceCommand(1L, null, 1L, null, null, null, null, null, null, null, null);

        when(resourceRepository.findById(1L)).thenReturn(Optional.of(testMenuResource));

        assertThrows(DomainException.class, () -> {
            resourceApplicationService.updateResource(command);
        });
    }
}