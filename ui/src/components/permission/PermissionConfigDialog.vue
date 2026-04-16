<template>
  <el-dialog
    v-model="visible"
    title="配置权限"
    width="500px"
    :close-on-click-modal="false"
  >
    <el-alert type="warning" :closable="false" class="warning-alert">
      配置将覆盖该资源的现有权限设置
    </el-alert>

    <div class="resource-info">
      <p><strong>资源名称：</strong>{{ resource?.name }}</p>
      <p><strong>资源编码：</strong>{{ resource?.code }}</p>
    </div>

    <el-form ref="formRef" :model="form" label-width="100px" class="config-form">
      <el-form-item label="操作权限">
        <el-checkbox-group v-model="form.actions">
          <el-checkbox label="VIEW">查看</el-checkbox>
          <el-checkbox label="CREATE">创建</el-checkbox>
          <el-checkbox label="UPDATE">更新</el-checkbox>
          <el-checkbox label="DELETE">删除</el-checkbox>
          <el-checkbox label="EXECUTE">执行</el-checkbox>
        </el-checkbox-group>
      </el-form-item>

      <el-form-item label="权限效果">
        <el-select v-model="form.effect" placeholder="请选择">
          <el-option label="允许" value="ALLOW" />
          <el-option label="拒绝" value="DENY" />
        </el-select>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button class="save-button" type="primary" @click="handleSave" :loading="loading">
        保存
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { usePermissionStore } from '../../stores/permission'
import { roleApi } from '../../api'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  roleId: { type: Number, default: null },
  resource: { type: Object, default: null }
})

const emit = defineEmits(['update:modelValue', 'success'])

const permissionStore = usePermissionStore()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const formRef = ref()
const loading = ref(false)

const form = reactive({
  actions: ['VIEW'],
  effect: 'ALLOW'
})

// 监听打开，初始化默认值
watch(visible, (val) => {
  if (val) {
    form.actions = ['VIEW']
    form.effect = 'ALLOW'
  }
})

// 保存
const handleSave = async () => {
  if (!props.roleId || !props.resource) return

  try {
    loading.value = true
    await roleApi.assignPermission(props.roleId, {
      resourceId: props.resource.id,
      actions: form.actions,
      effect: form.effect
    })

    ElMessage.success('权限配置成功')

    // 刷新当前用户权限（如果影响到当前用户）
    await permissionStore.loadPermissions()

    emit('success')
    visible.value = false
  } catch (error) {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.warning-alert {
  margin-bottom: 16px;
}

.resource-info {
  padding: 12px;
  background: var(--color-bg-glass);
  border-radius: 8px;
  margin-bottom: 16px;

  p {
    margin: 4px 0;
    color: var(--color-text-primary);
  }
}

.config-form {
  .el-checkbox-group {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
  }
}

.save-button {
  @include gradient-button;
}
</style>