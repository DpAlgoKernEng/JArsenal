<template>
  <el-dialog
    v-model="visible"
    :title="title"
    width="560px"
    @closed="handleClosed"
    class="resource-form-dialog"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="资源编码" prop="code">
        <el-input
          v-model="form.code"
          placeholder="如: USER_MANAGE"
          :disabled="isEdit"
        />
        <div class="form-tip">编码格式: 大写字母开头，可包含大写字母、数字和下划线</div>
      </el-form-item>
      <el-form-item label="资源名称" prop="name">
        <el-input v-model="form.name" placeholder="如: 用户管理" />
      </el-form-item>
      <el-form-item label="资源类型" prop="type">
        <el-radio-group v-model="form.type" :disabled="isEdit">
          <el-radio value="MENU">菜单资源</el-radio>
          <el-radio value="OPERATION">操作资源</el-radio>
          <el-radio value="API">API资源</el-radio>
        </el-radio-group>
        <div class="form-tip" v-if="!isEdit">
          类型根据当前Tab自动设置
        </div>
      </el-form-item>
      <el-form-item label="父资源" prop="parentId">
        <el-tree-select
          v-model="form.parentId"
          :data="parentOptions"
          :props="selectProps"
          check-strictly
          placeholder="选择父资源（可选）"
          clearable
          :render-after-expand="false"
          class="parent-select"
        />
      </el-form-item>
      <el-form-item label="路径" prop="path">
        <el-input v-model="form.path" placeholder="如: /system/users" />
        <div class="form-tip">
          MENU: 前端路由路径<br />
          OPERATION: 操作标识路径（如: /users/list）<br />
          API: 后端API路径（如: /api/users）
        </div>
      </el-form-item>
      <el-form-item label="路径模式" prop="pathPattern">
        <el-input v-model="form.pathPattern" placeholder="路径模式（如正则）" />
        <div class="form-tip">可选：用于动态路径匹配，如: /api/users/*</div>
      </el-form-item>

      <!-- API类型显示method字段 -->
      <el-form-item label="HTTP方法" prop="method" v-if="form.type === 'API'">
        <el-select v-model="form.method" placeholder="选择HTTP方法">
          <el-option value="GET" label="GET" />
          <el-option value="POST" label="POST" />
          <el-option value="PUT" label="PUT" />
          <el-option value="DELETE" label="DELETE" />
          <el-option value="PATCH" label="PATCH" />
          <el-option value="*" label="ALL (*)" />
        </el-select>
      </el-form-item>

      <!-- MENU类型显示icon和component字段 -->
      <el-form-item label="图标" prop="icon" v-if="form.type === 'MENU'">
        <el-input v-model="form.icon" placeholder="如: User">
          <template #prefix>
            <el-icon v-if="form.icon"><component :is="form.icon" /></el-icon>
          </template>
        </el-input>
        <div class="form-tip">Element Plus图标名称，如: User, Setting, Document</div>
      </el-form-item>
      <el-form-item label="组件路径" prop="component" v-if="form.type === 'MENU'">
        <el-input v-model="form.component" placeholder="如: views/UserList" />
        <div class="form-tip">Vue组件路径（相对于src目录）</div>
      </el-form-item>

      <el-form-item label="排序" prop="sort">
        <el-input-number v-model="form.sort" :min="0" :max="999" controls-position="right" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">禁用</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button class="cancel-button" @click="visible = false">取消</el-button>
        <el-button class="submit-button" @click="handleSubmit" :loading="submitLoading">
          确定
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { resourceApi } from '../../api/resource'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  parentId: {
    type: Number,
    default: null
  },
  resourceData: {
    type: Object,
    default: null
  },
  isEdit: {
    type: Boolean,
    default: false
  },
  defaultType: {
    type: String,
    default: 'MENU'
  },
  // 全部资源列表（用于父资源选择）
  allResources: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const title = computed(() => props.isEdit ? '编辑资源' : '新增资源')

const formRef = ref()
const submitLoading = ref(false)

const form = reactive({
  code: '',
  name: '',
  parentId: null,
  type: 'MENU',
  path: '',
  pathPattern: '',
  method: '',
  icon: '',
  component: '',
  sort: 0,
  status: 1
})

// 编码格式校验：大写字母开头，可包含大写字母、数字和下划线，长度2-50
const codePattern = /^[A-Z][A-Z0-9_]{1,49}$/

const rules = {
  code: [
    { required: true, message: '请输入资源编码', trigger: 'blur' },
    { pattern: codePattern, message: '编码格式不正确：大写字母开头，可包含大写字母、数字和下划线', trigger: 'blur' },
    { min: 2, max: 50, message: '编码长度2-50字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入资源名称', trigger: 'blur' },
    { min: 2, max: 50, message: '名称长度2-50字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择资源类型', trigger: 'change' }
  ],
  path: [
    { required: true, message: '请输入路径', trigger: 'blur' }
  ],
  method: [
    { required: true, message: '请选择HTTP方法', trigger: 'change' }
  ],
  sort: [
    { required: true, message: '请输入排序值', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 父资源选择配置
const selectProps = {
  children: 'children',
  label: 'label',
  value: 'value'
}

// 父资源选项（按同类型过滤）
const parentOptions = computed(() => {
  // 过滤同类型的资源
  const sameTypeResources = filterByType(props.allResources, form.type)

  if (!props.isEdit || !props.resourceData) {
    return buildTreeOptions(sameTypeResources)
  }

  // 编辑模式：需要排除当前资源及其所有子资源
  const excludeIds = collectChildIds([props.resourceData])
  return filterTreeOptions(buildTreeOptions(sameTypeResources), excludeIds)
})

// 过滤同类型资源
const filterByType = (resources, type) => {
  const result = []
  for (const node of resources) {
    if (node.type === type) {
      const newNode = { ...node }
      if (node.children && node.children.length > 0) {
        const filteredChildren = filterByType(node.children, type)
        if (filteredChildren.length > 0) {
          newNode.children = filteredChildren
        } else {
          newNode.children = []
        }
      }
      result.push(newNode)
    }
  }
  return result
}

// 构建树选择器选项
const buildTreeOptions = (resources) => {
  return resources.map(resource => ({
    value: resource.id,
    label: resource.name,
    children: resource.children && resource.children.length > 0
      ? buildTreeOptions(resource.children)
      : undefined
  }))
}

// 递归收集子资源ID
const collectChildIds = (resources) => {
  const ids = []
  for (const resource of resources) {
    ids.push(resource.id)
    if (resource.children && resource.children.length > 0) {
      ids.push(...collectChildIds(resource.children))
    }
  }
  return ids
}

// 过滤树选项
const filterTreeOptions = (options, excludeIds) => {
  return options.filter(opt => !excludeIds.includes(opt.value))
    .map(opt => {
      if (opt.children && opt.children.length > 0) {
        return {
          ...opt,
          children: filterTreeOptions(opt.children, excludeIds)
        }
      }
      return opt
    })
}

// 监听资源数据变化（编辑模式）
watch(() => props.resourceData, (data) => {
  if (data && props.isEdit) {
    form.code = data.code || ''
    form.name = data.name || ''
    form.parentId = data.parentId || null
    form.type = data.type || 'MENU'
    form.path = data.path || ''
    form.pathPattern = data.pathPattern || ''
    form.method = data.method || ''
    form.icon = data.icon || ''
    form.component = data.component || ''
    form.sort = data.sort ?? 0
    form.status = data.status ?? 1
  }
}, { immediate: true })

// 监听 parentId 变化（新增子资源时）
watch(() => props.parentId, (id) => {
  if (!props.isEdit && id) {
    form.parentId = id
  }
}, { immediate: true })

// 监听 defaultType 变化（新增时自动设置类型）
watch(() => props.defaultType, (type) => {
  if (!props.isEdit) {
    form.type = type
  }
}, { immediate: true })

// 对话框关闭时重置
const handleClosed = () => {
  formRef.value?.resetFields()
  form.code = ''
  form.name = ''
  form.parentId = null
  form.type = props.defaultType
  form.path = ''
  form.pathPattern = ''
  form.method = ''
  form.icon = ''
  form.component = ''
  form.sort = 0
  form.status = 1
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitLoading.value = true

    const data = {
      code: form.code,
      name: form.name,
      parentId: form.parentId || null,
      type: form.type,
      path: form.path,
      pathPattern: form.pathPattern || null,
      method: form.type === 'API' ? form.method : null,
      icon: form.type === 'MENU' ? form.icon : null,
      component: form.type === 'MENU' ? form.component : null,
      sort: form.sort,
      status: form.status
    }

    if (props.isEdit) {
      await resourceApi.update(props.resourceData.id, data)
      ElMessage.success('更新成功')
    } else {
      await resourceApi.create(data)
      ElMessage.success('创建成功')
    }

    visible.value = false
    emit('success')
  } catch (error) {
    // error handled by interceptor
  } finally {
    submitLoading.value = false
  }
}

defineExpose({
  reset: handleClosed
})
</script>

<style lang="scss" scoped>
.resource-form-dialog {
  :deep(.el-dialog) {
    @include glass-card;
    border-radius: 16px;

    .el-dialog__header {
      padding: 20px 24px 16px;
      border-bottom: 1px solid var(--color-border-light);

      .el-dialog__title {
        @include gradient-text;
        font-weight: 600;
      }
    }

    .el-dialog__body {
      padding: 24px;

      .el-form-item {
        margin-bottom: 20px;
      }

      .el-input,
      .el-input-number,
      .el-select,
      .parent-select {
        --el-input-bg-color: var(--color-bg-glass);
        --el-input-border-color: var(--color-border);
        --el-input-text-color: var(--color-text-primary);
        --el-input-placeholder-color: var(--color-text-muted);
      }

      .form-tip {
        margin-top: 4px;
        color: var(--color-text-muted);
        font-size: 12px;
        line-height: 1.5;
      }

      .el-radio-group {
        .el-radio {
          --el-radio-text-color: var(--color-text-primary);
        }
      }
    }

    .el-dialog__footer {
      padding: 16px 24px 20px;
      border-top: 1px solid var(--color-border-light);
    }
  }

  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;

    .cancel-button {
      background: var(--color-bg-glass);
      border: 1px solid var(--color-border);
      color: var(--color-text-primary);
      transition: all 0.3s ease;

      &:hover {
        background: var(--color-border);
      }
    }

    .submit-button {
      @include gradient-button;
      padding: 8px 24px;
    }
  }
}
</style>