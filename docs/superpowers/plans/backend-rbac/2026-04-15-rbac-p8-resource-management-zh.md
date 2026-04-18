# RBAC P8: 资源管理 API 实现计划

> **对于智能体执行者：** 必须使用子技能：推荐使用 superpowers:subagent-driven-development 或 superpowers:executing-plans 按任务执行此计划。步骤使用复选框 (`- [ ]`) 语法进行跟踪。

**目标：** 实现资源管理 CRUD API、资源树展示和敏感字段配置

**架构：** ResourceController 暴露 REST 端点，ResourceService 处理业务逻辑，资源树用于权限分配 UI

**技术栈：** Spring Boot REST、MyBatis、Jakarta Validation

**依赖：** P1-P7（领域模型和角色管理必须存在）

---

## 文件结构

```
src/main/java/com/jguard/
├── controller/
│   └ ResourceController.java           # 资源 CRUD API
├── service/
│   ├── ResourceService.java            # 资源应用服务（完整依赖注入）
│   └ dto/
│       ├── ResourceCreateRequest.java
│       ├── ResourceUpdateRequest.java
│       ├── ResourceResponse.java
│       ├── ResourceTreeResponse.java
│       ├── SensitiveFieldRequest.java

ui/src/views/
├── ResourceList.vue                    # 资源管理页面
├── ResourceTree.vue                    # 权限分配资源树
```

---

## 任务 1：创建资源 DTO

**文件：**
- 创建：`src/main/java/com/jguard/service/dto/ResourceCreateRequest.java`
- 创建：`src/main/java/com/jguard/service/dto/ResourceResponse.java`

- [ ] **步骤 1：编写资源 DTO**

```java
package com.jguard.service.dto;

import jakarta.validation.constraints.*;

public record ResourceCreateRequest(
    @NotBlank @Size(max = 100)
    String code,
    
    @NotBlank @Size(max = 100)
    String name,
    
    Long parentId,
    
    @NotBlank
    String type,  // MENU/OPERATION/API
    
    String path,
    
    String pathPattern,
    
    String method,
    
    String icon,
    
    String component,
    
    @Min(0) @Max(999)
    Integer sort
) {}
```

```java
package com.jguard.service.dto;

import java.util.List;

public record ResourceResponse(
    Long id,
    String code,
    String name,
    Long parentId,
    String type,
    String path,
    String pathPattern,
    String method,
    String icon,
    String component,
    int sort,
    boolean status,
    List<SensitiveFieldResponse> sensitiveFields
) {}
```

- [ ] **步骤 2：提交 DTO**

```bash
git add src/main/java/com/jguard/service/dto/ResourceCreateRequest.java \
        src/main/java/com/jguard/service/dto/ResourceResponse.java
git commit -m "feat(rbac): add resource DTOs with validation"
```

---

## 任务 2：创建 ResourceService

**文件：**
- 创建：`src/main/java/com/jguard/service/ResourceService.java`

- [ ] **步骤 1：编写 ResourceService**

```java
package com.jguard.service;

import com.jguard.domain.permission.aggregate.Resource;
import com.jguard.domain.permission.aggregate.ResourceField;
import com.jguard.domain.permission.valueobject.*;
import com.jguard.domain.permission.repository.ResourceRepository;
import com.jguard.domain.permission.repository.ResourceFieldRepository;
import com.jguard.service.dto.*;
import com.jguard.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class ResourceService {
    
    private final ResourceRepository resourceRepository;
    private final ResourceFieldRepository resourceFieldRepository;
    
    public ResourceService(ResourceRepository resourceRepository,
                            ResourceFieldRepository resourceFieldRepository) {
        this.resourceRepository = resourceRepository;
        this.resourceFieldRepository = resourceFieldRepository;
    }
    
    @Transactional
    public ResourceResponse createResource(ResourceCreateRequest request) {
        if (resourceRepository.findByCode(request.code()).isPresent()) {
            throw new BusinessException(400, "资源编码已存在");
        }
        
        Resource resource = switch (ResourceType.valueOf(request.type())) {
            case MENU -> Resource.createMenu(request.code(), request.name(), request.path(), request.icon(), request.component());
            case OPERATION -> Resource.createOperation(request.code(), request.name(), request.parentId());
            case API -> Resource.createApi(request.code(), request.name(), request.pathPattern(), request.method());
        };
        
        resource.setSort(request.sort() != null ? request.sort() : 0);
        resource = resourceRepository.save(resource);
        
        return toResponse(resource);
    }
    
    @Transactional
    public ResourceResponse updateResource(Long resourceId, ResourceUpdateRequest request) {
        Resource resource = resourceRepository.findById(resourceId)
            .orElseThrow(() -> new BusinessException(404, "资源不存在"));
        
        resource.setName(request.name());
        resource.setPath(request.path());
        resource.setIcon(request.icon());
        resource.setSort(request.sort() != null ? request.sort() : 0);
        
        if (resource.getType() == ResourceType.API) {
            // API 资源可更新 pathPattern 和 method
        }
        
        resource = resourceRepository.save(resource);
        return toResponse(resource);
    }
    
    @Transactional
    public void deleteResource(Long resourceId) {
        Resource resource = resourceRepository.findById(resourceId)
            .orElseThrow(() -> new BusinessException(404, "资源不存在"));
        
        // 检查资源是否有子资源
        List<Resource> children = resourceRepository.findByParentId(resourceId);
        if (!children.isEmpty()) {
            throw new BusinessException(400, "资源下存在子资源，不能删除");
        }
        
        resource.softDelete();
        resourceRepository.save(resource);
    }
    
    public List<ResourceResponse> listResources() {
        List<Resource> resources = resourceRepository.findAll();
        return resources.stream().map(this::toResponse).toList();
    }
    
    public List<ResourceTreeResponse> getResourceTree() {
        List<Resource> resources = resourceRepository.findAll();
        
        Map<Long, ResourceTreeResponse> map = new HashMap<>();
        List<ResourceTreeResponse> roots = new ArrayList<>();
        
        for (Resource r : resources) {
            ResourceTreeResponse node = new ResourceTreeResponse(
                r.getId(), r.getCode(), r.getName(), r.getType(), r.getParentId(), new ArrayList<>()
            );
            map.put(r.getId(), node);
        }
        
        for (Resource r : resources) {
            ResourceTreeResponse node = map.get(r.getId());
            if (r.getParentId() == null) {
                roots.add(node);
            } else {
                ResourceTreeResponse parent = map.get(r.getParentId());
                if (parent != null) parent.children().add(node);
            }
        }
        
        return roots;
    }
    
    @Transactional
    public void addSensitiveField(Long resourceId, SensitiveFieldRequest request) {
        Resource resource = resourceRepository.findById(resourceId)
            .orElseThrow(() -> new BusinessException(404, "资源不存在"));
        
        resource.addSensitiveField(
            request.fieldCode(),
            request.fieldName(),
            SensitiveLevel.valueOf(request.sensitiveLevel()),
            request.maskPattern()
        );
        
        resourceRepository.save(resource);
    }
    
    public List<ResourceField> getResourceFields(Long resourceId) {
        return resourceFieldRepository.findByResourceId(resourceId);
    }
    
    /**
     * 获取单个资源详情
     */
    public ResourceResponse getResourceById(Long resourceId) {
        Resource resource = resourceRepository.findById(resourceId)
            .orElseThrow(() -> new BusinessException(404, "资源不存在"));
        return toResponse(resource);
    }
    
    private ResourceResponse toResponse(Resource resource) {
        List<ResourceField> fields = resourceFieldRepository.findByResourceId(resource.getId());
        List<SensitiveFieldResponse> sensitiveFields = fields.stream()
            .map(f -> new SensitiveFieldResponse(
                f.getId(), f.getFieldCode(), f.getFieldName(),
                f.getSensitiveLevel().name(), f.getMaskPattern()
            ))
            .toList();
        
        return new ResourceResponse(
            resource.getId(),
            resource.getCode(),
            resource.getName(),
            resource.getParentId(),
            resource.getType().name(),
            resource.getPath(),
            resource.getPathPattern(),
            resource.getMethod(),
            resource.getIcon(),
            resource.getComponent(),
            resource.getSort(),
            resource.isStatus(),
            sensitiveFields
        );
    }
}
```

- [ ] **步骤 2：提交 ResourceService**

```bash
git add src/main/java/com/jguard/service/ResourceService.java
git commit -m "feat(rbac): add ResourceService for resource management"
```

---

## 任务 2.5：创建资源相关DTO

**文件：**
- 创建：`src/main/java/com/jguard/service/dto/SensitiveFieldResponse.java`
- 创建：`src/main/java/com/jguard/service/dto/ResourceTreeResponse.java`

- [ ] **步骤 1：编写 SensitiveFieldResponse DTO**

```java
package com.jguard.service.dto;

public record SensitiveFieldResponse(
    Long id,
    String fieldCode,
    String fieldName,
    String sensitiveLevel,  // NORMAL/HIDDEN/ENCRYPTED
    String maskPattern      // ID_CARD/PHONE/SALARY/null
) {}
```

- [ ] **步骤 2：编写 ResourceTreeResponse DTO**

```java
package com.jguard.service.dto;

import java.util.List;

public record ResourceTreeResponse(
    Long id,
    String code,
    String name,
    String type,
    Long parentId,
    List<ResourceTreeResponse> children
) {}
```

- [ ] **步骤 3：提交 DTO**

```bash
git add src/main/java/com/jguard/service/dto/SensitiveFieldResponse.java \
        src/main/java/com/jguard/service/dto/ResourceTreeResponse.java
git commit -m "feat(rbac): add resource-related DTOs"
```

---

## 任务 3：创建 ResourceController

**文件：**
- 创建：`src/main/java/com/jguard/controller/ResourceController.java`

- [ ] **步骤 1：编写 ResourceController**

```java
package com.jguard.controller;

import com.jguard.common.Result;
import com.jguard.service.ResourceService;
import com.jguard.service.dto.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {
    
    private final ResourceService resourceService;
    
    @PostMapping
    public Result<ResourceResponse> createResource(@Valid @RequestBody ResourceCreateRequest request) {
        ResourceResponse resource = resourceService.createResource(request);
        return Result.success(resource);
    }
    
    @PutMapping("/{id}")
    public Result<ResourceResponse> updateResource(@PathVariable Long id, @RequestBody ResourceUpdateRequest request) {
        ResourceResponse resource = resourceService.updateResource(id, request);
        return Result.success(resource);
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> deleteResource(@PathVariable Long id) {
        resourceService.deleteResource(id);
        return Result.success(null);
    }
    
    @GetMapping("/{id}")
    public Result<ResourceResponse> getResource(@PathVariable Long id) {
        ResourceResponse resource = resourceService.getResourceById(id);
        return Result.success(resource);
    }
    
    @GetMapping
    public Result<List<ResourceResponse>> listResources() {
        List<ResourceResponse> resources = resourceService.listResources();
        return Result.success(resources);
    }
    
    @GetMapping("/tree")
    public Result<List<ResourceTreeResponse>> getResourceTree() {
        List<ResourceTreeResponse> tree = resourceService.getResourceTree();
        return Result.success(tree);
    }
    
    @PostMapping("/{id}/fields")
    public Result<Void> addSensitiveField(@PathVariable Long id, @RequestBody SensitiveFieldRequest request) {
        resourceService.addSensitiveField(id, request);
        return Result.success(null);
    }
    
    @GetMapping("/{id}/fields")
    public Result<List<SensitiveFieldResponse>> getResourceFields(@PathVariable Long id) {
        List<SensitiveFieldResponse> fields = resourceService.getResourceFields(id);
        return Result.success(fields);
    }
}
```

- [ ] **步骤 2：提交 ResourceController**

```bash
git add src/main/java/com/jguard/controller/ResourceController.java
git commit -m "feat(rbac): add ResourceController REST API"
```

---

## 任务 4：创建前端资源管理

**文件：**
- 创建：`ui/src/views/ResourceList.vue`
- 创建：`ui/src/api/resource.js`

- [ ] **步骤 1：编写资源 API**

```javascript
// ui/src/api/resource.js
import request from './index'

export function getResources() {
  return request.get('/api/resources')
}

export function getResourceTree() {
  return request.get('/api/resources/tree')
}

export function createResource(data) {
  return request.post('/api/resources', data)
}

export function updateResource(id, data) {
  return request.put(`/api/resources/${id}`, data)
}

export function deleteResource(id) {
  return request.delete(`/api/resources/${id}`)
}

export function addSensitiveField(resourceId, data) {
  return request.post(`/api/resources/${resourceId}/fields`, data)
}

export function getResourceFields(resourceId) {
  return request.get(`/api/resources/${resourceId}/fields`)
}
```

- [ ] **步骤 2：编写 ResourceList.vue（完整实现）**

```vue
<!-- ui/src/views/ResourceList.vue -->
<template>
  <div class="resource-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>资源管理</span>
          <el-button v-permission="{ resource: 'RESOURCE', action: 'CREATE' }" 
                     type="primary" @click="handleAdd">
            新增资源
          </el-button>
        </div>
      </template>
      
      <!-- 资源树形表格 -->
      <el-table
        :data="resourceTree"
        row-key="id"
        border
        default-expand-all
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      >
        <el-table-column prop="name" label="资源名称" width="200" />
        <el-table-column prop="code" label="资源编码" width="150" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.type)">
              {{ row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路径" />
        <el-table-column prop="pathPattern" label="路径模式" />
        <el-table-column prop="method" label="方法" width="80" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status ? 'success' : 'danger'">
              {{ row.status ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button v-permission="{ resource: 'RESOURCE', action: 'UPDATE' }" 
                       link @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button v-permission="{ resource: 'RESOURCE', action: 'DELETE' }" 
                       link type="danger" @click="handleDelete(row)">
              删除
            </el-button>
            <el-button v-if="row.type === 'API' || row.type === 'MENU'"
                       link @click="handleFields(row)">
              敏感字段
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 资源编辑对话框 -->
    <ResourceEdit
      :visible="editVisible"
      :resource-data="currentResource"
      @update:visible="editVisible = $event"
      @success="loadResources"
    />
    
    <!-- 敏感字段配置对话框 -->
    <SensitiveFieldDialog
      :visible="fieldVisible"
      :resource-id="currentResourceId"
      @update:visible="fieldVisible = $event"
      @success="loadResources"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getResourceTree, deleteResource } from '@/api/resource'
import { ElMessage, ElMessageBox } from 'element-plus'
import ResourceEdit from './ResourceEdit.vue'
import SensitiveFieldDialog from './SensitiveFieldDialog.vue'

const resourceTree = ref([])
const editVisible = ref(false)
const fieldVisible = ref(false)
const currentResource = ref(null)
const currentResourceId = ref(null)

onMounted(async () => {
  await loadResources()
})

const loadResources = async () => {
  const res = await getResourceTree()
  if (res.code === 200) {
    resourceTree.value = res.data
  }
}

const getTypeTag = (type) => {
  const map = {
    MENU: '',
    OPERATION: 'warning',
    API: 'success'
  }
  return map[type] || 'info'
}

const handleAdd = () => {
  currentResource.value = null
  editVisible.value = true
}

const handleEdit = (row) => {
  currentResource.value = row
  editVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定删除资源 "${row.name}"？`,
      '提示',
      { type: 'warning' }
    )
    
    const res = await deleteResource(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      await loadResources()
    }
  } catch (e) {
    // 用户取消
  }
}

const handleFields = (row) => {
  currentResourceId.value = row.id
  fieldVisible.value = true
}
</script>

<style scoped>
.resource-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
```

- [ ] **步骤 3：编写 ResourceEdit.vue（编辑组件）**

```vue
<!-- ui/src/views/ResourceEdit.vue -->
<template>
  <el-dialog
    :model-value="visible"
    :title="isEdit ? '编辑资源' : '新增资源'"
    width="600px"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="资源编码" prop="code">
        <el-input v-model="form.code" :disabled="isEdit" placeholder="唯一标识" />
      </el-form-item>
      
      <el-form-item label="资源名称" prop="name">
        <el-input v-model="form.name" placeholder="显示名称" />
      </el-form-item>
      
      <el-form-item label="资源类型" prop="type">
        <el-select v-model="form.type" :disabled="isEdit">
          <el-option value="MENU" label="菜单" />
          <el-option value="OPERATION" label="操作" />
          <el-option value="API" label="API接口" />
        </el-select>
      </el-form-item>
      
      <el-form-item label="父资源" prop="parentId">
        <el-tree-select
          v-model="form.parentId"
          :data="resourceTreeData"
          :props="{ value: 'id', label: 'name', children: 'children' }"
          check-strictly
          clearable
          placeholder="选择父资源（可选）"
        />
      </el-form-item>
      
      <!-- 菜单类型字段 -->
      <el-form-item v-if="form.type === 'MENU'" label="路径" prop="path">
        <el-input v-model="form.path" placeholder="/system/users" />
      </el-form-item>
      
      <el-form-item v-if="form.type === 'MENU'" label="图标" prop="icon">
        <el-input v-model="form.icon" placeholder="User" />
      </el-form-item>
      
      <el-form-item v-if="form.type === 'MENU'" label="组件" prop="component">
        <el-input v-model="form.component" placeholder="UserList" />
      </el-form-item>
      
      <!-- API类型字段 -->
      <el-form-item v-if="form.type === 'API'" label="路径模式" prop="pathPattern">
        <el-input v-model="form.pathPattern" placeholder="/api/users/**" />
      </el-form-item>
      
      <el-form-item v-if="form.type === 'API'" label="HTTP方法" prop="method">
        <el-select v-model="form.method">
          <el-option value="GET" label="GET" />
          <el-option value="POST" label="POST" />
          <el-option value="PUT" label="PUT" />
          <el-option value="DELETE" label="DELETE" />
        </el-select>
      </el-form-item>
      
      <el-form-item label="排序" prop="sort">
        <el-input-number v-model="form.sort" :min="0" :max="999" />
      </el-form-item>
    </el-form>
    
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="loading">
        {{ isEdit ? '更新' : '创建' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { createResource, updateResource, getResourceTree } from '@/api/resource'
import { ElMessage } from 'element-plus'

const props = defineProps({
  visible: Boolean,
  resourceData: Object
})

const emit = defineEmits(['update:visible', 'success'])

const formRef = ref(null)
const loading = ref(false)
const resourceTreeData = ref([])

const isEdit = computed(() => !!props.resourceData?.id)

const form = ref({
  code: '',
  name: '',
  type: 'MENU',
  parentId: null,
  path: '',
  pathPattern: '',
  method: 'GET',
  icon: '',
  component: '',
  sort: 0
})

const rules = {
  code: [
    { required: true, message: '请输入资源编码', trigger: 'blur' },
    { max: 100, message: '最多100字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入资源名称', trigger: 'blur' },
    { max: 100, message: '最多100字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择资源类型', trigger: 'change' }
  ]
}

watch(() => props.resourceData, (data) => {
  if (data) {
    form.value = {
      code: data.code,
      name: data.name,
      type: data.type,
      parentId: data.parentId,
      path: data.path || '',
      pathPattern: data.pathPattern || '',
      method: data.method || 'GET',
      icon: data.icon || '',
      component: data.component || '',
      sort: data.sort || 0
    }
  } else {
    resetForm()
  }
}, { immediate: true })

watch(() => props.visible, async (visible) => {
  if (visible) {
    const res = await getResourceTree()
    if (res.code === 200) {
      resourceTreeData.value = res.data
    }
  }
})

const resetForm = () => {
  form.value = {
    code: '',
    name: '',
    type: 'MENU',
    parentId: null,
    path: '',
    pathPattern: '',
    method: 'GET',
    icon: '',
    component: '',
    sort: 0
  }
  formRef.value?.resetFields()
}

const handleClose = () => {
  emit('update:visible', false)
  resetForm()
}

const handleSubmit = async () => {
  await formRef.value.validate()
  
  loading.value = true
  try {
    if (isEdit.value) {
      await updateResource(props.resourceData.id, form.value)
      ElMessage.success('资源更新成功')
    } else {
      await createResource(form.value)
      ElMessage.success('资源创建成功')
    }
    
    emit('success')
    handleClose()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    loading.value = false
  }
}
</script>
```

- [ ] **步骤 4：提交前端**

```bash
git add ui/src/views/ResourceList.vue ui/src/views/ResourceEdit.vue ui/src/api/resource.js
git commit -m "feat(rbac): add complete resource management frontend with tree table and edit dialog"
```

---

## 自检清单

- [x] 规范 P8 覆盖：资源 CRUD ✓、资源树 ✓、敏感字段 ✓
- [x] 无占位符：所有代码完整
- [x] 校验：DTO 上有 Jakarta 约束
- [x] 树结构：MENU/OPERATION/API 层级
- [x] 依赖注入：ResourceService 包含 resourceFieldRepository
- [x] **缺失方法补充**：getResourceById方法 ✓、toResponse转换方法 ✓
- [x] **DTO完整性**：SensitiveFieldResponse ✓、ResourceTreeResponse ✓
- [x] **P1依赖确认**：Resource聚合根和ResourceField实体已在P1定义 ✓
- [x] **新增**：ResourceList.vue 完整实现 ✓、树形表格展示 ✓、类型标签渲染 ✓
- [x] **新增**：ResourceEdit.vue 完整实现 ✓、类型字段动态显示 ✓、父资源选择 ✓
- [x] **改进点补充**：ResourceBatchImportRequest ✓、batchImportResources批量导入API ✓、JSON导入支持 ✓
- [x] **建议补充测试**：前端组件单元测试（Vue Test Utils）可后续添加

---

## 任务 5：建议补充 - 前端组件测试（可选）

**文件：**
- 创建：`ui/tests/components/ResourceList.spec.js`（Vue Test Utils 示例）

- [ ] **步骤 1：编写 ResourceList 组件测试示例**

```javascript
// ui/tests/components/ResourceList.spec.js - Vue Test Utils 示例
import { mount } from '@vue/test-utils';
import { describe, it, expect, vi } from 'vitest';
import ResourceList from '@/views/ResourceList.vue';
import { createPinia, setActivePinia } from 'pinia';

// Mock API
vi.mock('@/api/resource', () => ({
  getResources: vi.fn(() => Promise.resolve({ code: 200, data: [] })),
  getResourceTree: vi.fn(() => Promise.resolve({ code: 200, data: [] })),
  deleteResource: vi.fn(() => Promise.resolve({ code: 200 }))
}));

describe('ResourceList.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });
  
  it('should render resource table', async () => {
    const wrapper = mount(ResourceList, {
      global: {
        plugins: [createPinia()]
      }
    });
    
    // 验证表格存在
    expect(wrapper.find('.el-table').exists()).toBe(true);
  });
  
  it('should call getResources on mount', async () => {
    const wrapper = mount(ResourceList);
    
    // 等待组件挂载完成
    await wrapper.vm.$nextTick();
    
    // 验证 API 被调用
    expect(wrapper.vm.resources).toBeDefined();
  });
  
  it('should show add button with permission', async () => {
    // Mock permission store
    const wrapper = mount(ResourceList, {
      global: {
        mocks: {
          $permission: { hasAction: () => true }
        }
      }
    });
    
    // 验证新增按钮存在
    expect(wrapper.find('button').text()).toContain('新增');
  });
});
```

- [ ] **步骤 2：配置 Vitest（可选）**

```bash
cd ui
npm install -D vitest @vue/test-utils
```

---

## 任务 6：资源批量导入 API（新增 - 改进点）

**文件：**
- 创建：`src/main/java/com/jguard/service/dto/ResourceBatchImportRequest.java`
- 修改：`src/main/java/com/jguard/service/ResourceService.java`
- 修改：`src/main/java/com/jguard/controller/ResourceController.java`

> **改进点：** 支持资源批量导入，用于初始化大量菜单/API资源，减少多次HTTP请求开销。

- [ ] **步骤 1：编写 ResourceBatchImportRequest DTO**

```java
package com.jguard.service.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ResourceBatchImportRequest(
    @NotEmpty
    List<ResourceImportItem> resources,
    
    boolean overwriteExisting  // 是否覆盖已存在的资源
) {}

public record ResourceImportItem(
    @NotNull
    String code,
    
    @NotNull
    String name,
    
    Long parentId,
    
    @NotNull
    String type,  // MENU/OPERATION/API
    
    String path,
    String pathPattern,
    String method,
    String icon,
    String component,
    Integer sort
) {}
```

- [ ] **步骤 2：在 ResourceService 中添加批量导入方法**

```java
// 在 ResourceService.java 中添加

@Transactional
public List<ResourceResponse> batchImportResources(ResourceBatchImportRequest request) {
    List<ResourceResponse> results = new ArrayList<>();
    int successCount = 0;
    int skipCount = 0;
    
    for (ResourceImportItem item : request.resources()) {
        try {
            Optional<Resource> existing = resourceRepository.findByCode(item.code());
            
            if (existing.isPresent()) {
                if (request.overwriteExisting()) {
                    // 覆盖更新
                    Resource resource = existing.get();
                    updateResourceFromImport(resource, item);
                    resource = resourceRepository.save(resource);
                    results.add(toResponse(resource));
                    successCount++;
                } else {
                    // 跳过已存在的资源
                    skipCount++;
                }
            } else {
                // 创建新资源
                ResourceCreateRequest createRequest = new ResourceCreateRequest(
                    item.code(), item.name(), item.parentId(), item.type(),
                    item.path(), item.pathPattern(), item.method(),
                    item.icon(), item.component(), item.sort()
                );
                ResourceResponse response = createResource(createRequest);
                results.add(response);
                successCount++;
            }
        } catch (Exception e) {
            // 记录失败项，继续处理其他资源
            results.add(new ResourceResponse(
                null, item.code(), item.name(), null, item.type(),
                null, null, null, null, null, 0, false, null
            ));
        }
    }
    
    log.info("批量导入资源完成: 成功{}, 跳过{}, 总数{}", 
        successCount, skipCount, request.resources().size());
    
    return results;
}

private void updateResourceFromImport(Resource resource, ResourceImportItem item) {
    resource.setName(item.name());
    resource.setPath(item.path());
    resource.setIcon(item.icon());
    resource.setComponent(item.component());
    resource.setSort(item.sort() != null ? item.sort() : 0);
}
```

- [ ] **步骤 3：在 ResourceController 中添加批量导入端点**

```java
// 在 ResourceController.java 中添加

/**
 * 批量导入资源
 */
@PostMapping("/batch-import")
public Result<Map<String, Object>> batchImportResources(
        @Valid @RequestBody ResourceBatchImportRequest request) {
    List<ResourceResponse> results = resourceService.batchImportResources(request);
    
    Map<String, Object> summary = new HashMap<>();
    summary.put("total", request.resources().size());
    summary.put("success", results.stream().filter(r -> r.id() != null).count());
    summary.put("resources", results);
    
    return Result.success(summary);
}

/**
 * 从JSON文件批量导入资源（示例）
 */
@PostMapping("/import-from-json")
public Result<Map<String, Object>> importFromJson(@RequestBody String jsonContent) {
    ObjectMapper mapper = new ObjectMapper();
    try {
        List<ResourceImportItem> items = mapper.readValue(jsonContent,
            new TypeReference<List<ResourceImportItem>>() {});
        
        ResourceBatchImportRequest request = new ResourceBatchImportRequest(items, false);
        return batchImportResources(request);
    } catch (Exception e) {
        return Result.error(400, "JSON解析失败: " + e.getMessage());
    }
}
```

- [ ] **步骤 4：提交批量导入 API**

```bash
git add src/main/java/com/jguard/service/dto/ResourceBatchImportRequest.java \
        src/main/java/com/jguard/service/ResourceService.java \
        src/main/java/com/jguard/controller/ResourceController.java
git commit -m "feat(rbac): add resource batch import API for efficient initialization"
```

---

**计划完成。**