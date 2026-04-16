<template>
  <el-dialog
    v-model="visible"
    :title="title"
    width="480px"
    @closed="handleClosed"
    class="role-form-dialog"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="角色编码" prop="code">
        <el-input
          v-model="form.code"
          placeholder="如: ADMIN_USER"
          :disabled="isEdit"
        />
        <div class="form-tip">编码格式: 大写字母开头，可包含大写字母、数字和下划线</div>
      </el-form-item>
      <el-form-item label="角色名称" prop="name">
        <el-input v-model="form.name" placeholder="如: 管理员用户" />
      </el-form-item>
      <el-form-item label="父角色" prop="parentId">
        <el-tree-select
          v-model="form.parentId"
          :data="parentOptions"
          :props="selectProps"
          check-strictly
          placeholder="选择父角色（可选）"
          clearable
          :render-after-expand="false"
          class="parent-select"
        />
      </el-form-item>
      <el-form-item label="继承模式" prop="inheritMode">
        <el-radio-group v-model="form.inheritMode">
          <el-radio :value="0">合并继承</el-radio>
          <el-radio :value="1">覆盖继承</el-radio>
        </el-radio-group>
        <div class="form-tip">
          合并继承: 权限与父角色合并<br />
          覆盖继承: 仅保留自身权限
        </div>
      </el-form-item>
      <el-form-item label="排序" prop="sort">
        <el-input-number v-model="form.sort" :min="0" :max="999" controls-position="right" />
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
import { useRoleStore } from '../../stores/role'
import { roleApi } from '../../api/role'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  parentId: {
    type: Number,
    default: null
  },
  roleData: {
    type: Object,
    default: null
  },
  isEdit: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const roleStore = useRoleStore()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const title = computed(() => props.isEdit ? '编辑角色' : '新增角色')

const formRef = ref()
const submitLoading = ref(false)

const form = reactive({
  code: '',
  name: '',
  parentId: null,
  inheritMode: 0,
  sort: 0
})

// 编码格式校验：大写字母开头，可包含大写字母、数字和下划线，长度2-50
const codePattern = /^[A-Z][A-Z0-9_]{1,49}$/

const rules = {
  code: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { pattern: codePattern, message: '编码格式不正确：大写字母开头，可包含大写字母、数字和下划线', trigger: 'blur' },
    { min: 2, max: 50, message: '编码长度2-50字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 50, message: '名称长度2-50字符', trigger: 'blur' }
  ],
  inheritMode: [
    { required: true, message: '请选择继承模式', trigger: 'change' }
  ],
  sort: [
    { required: true, message: '请输入排序值', trigger: 'blur' }
  ]
}

// 父角色选择配置
const selectProps = {
  children: 'children',
  label: 'label',
  value: 'value'
}

// 父角色选项（过滤掉当前角色及其子角色）
const parentOptions = computed(() => {
  if (!props.isEdit || !props.roleData) {
    return roleStore.buildTreeOptions()
  }

  // 编辑模式：需要排除当前角色及其所有子角色
  const excludeIds = collectChildIds([props.roleData])
  return filterTreeOptions(roleStore.buildTreeOptions(), excludeIds)
})

// 递归收集子角色ID
const collectChildIds = (roles) => {
  const ids = []
  for (const role of roles) {
    ids.push(role.id)
    if (role.children && role.children.length > 0) {
      ids.push(...collectChildIds(role.children))
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

// 监听角色数据变化
watch(() => props.roleData, (data) => {
  if (data && props.isEdit) {
    form.code = data.code || ''
    form.name = data.name || ''
    form.parentId = data.parentId || null
    form.inheritMode = data.inheritMode ?? 0
    form.sort = data.sort ?? 0
  }
}, { immediate: true })

// 监听 parentId 变化（新增子角色时）
watch(() => props.parentId, (id) => {
  if (!props.isEdit && id) {
    form.parentId = id
  }
}, { immediate: true })

// 对话框关闭时重置
const handleClosed = () => {
  formRef.value?.resetFields()
  form.code = ''
  form.name = ''
  form.parentId = null
  form.inheritMode = 0
  form.sort = 0
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
      inheritMode: form.inheritMode,
      sort: form.sort
    }

    if (props.isEdit) {
      await roleApi.update(props.roleData.id, data)
      ElMessage.success('更新成功')
    } else {
      await roleApi.create(data)
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
.role-form-dialog {
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