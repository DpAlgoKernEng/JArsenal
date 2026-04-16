<template>
  <div class="role-permission-panel">
    <!-- 左侧角色选择 -->
    <div class="left-panel">
      <RoleTree
        @select="handleSelectRole"
      />
    </div>

    <!-- 右侧资源权限配置 -->
    <div class="right-panel">
      <div class="panel-header">
        <h3 class="panel-title">
          {{ selectedRole ? `配置角色 "${selectedRole.name}" 的权限` : '请选择角色' }}
        </h3>
      </div>

      <el-table
        v-if="selectedRole"
        :data="flatResources"
        row-key="id"
        v-loading="resourceLoading"
        class="resource-table"
      >
        <el-table-column prop="name" label="资源名称" min-width="150" />
        <el-table-column prop="code" label="编码" width="150" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag effect="plain" size="small">
              {{ row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button
              v-if="canConfig"
              link
              size="small"
              @click="handleConfig(row)"
            >
              配置权限
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-else description="请从左侧选择一个角色" />
    </div>

    <!-- 权限配置弹窗 -->
    <PermissionConfigDialog
      v-model="configVisible"
      :role-id="selectedRole?.id"
      :resource="configResource"
      @success="handleConfigSuccess"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import RoleTree from '../role/RoleTree.vue'
import PermissionConfigDialog from './PermissionConfigDialog.vue'
import { useRoleStore } from '../../stores/role'
import { usePermission } from '../../composables/usePermission'
import { resourceApi } from '../../api'

const route = useRoute()
const roleStore = useRoleStore()
const { hasPermission } = usePermission()

const canConfig = computed(() => hasPermission('PERMISSION', 'UPDATE').value)

const selectedRole = computed(() => roleStore.currentRole)
const resources = ref([])
const resourceLoading = ref(false)

// 扁平化资源树（用于表格显示）
const flatResources = computed(() => {
  return flattenTree(resources.value)
})

const flattenTree = (tree, result = []) => {
  for (const node of tree) {
    result.push(node)
    if (node.children && node.children.length > 0) {
      flattenTree(node.children, result)
    }
  }
  return result
}

const configVisible = ref(false)
const configResource = ref(null)

// 加载资源树
const loadResources = async () => {
  resourceLoading.value = true
  try {
    resources.value = await resourceApi.treeV2()
  } finally {
    resourceLoading.value = false
  }
}

// 选择角色
const handleSelectRole = (role) => {
  // roleStore 已更新 currentRole
}

// 打开配置弹窗
const handleConfig = (row) => {
  configResource.value = row
  configVisible.value = true
}

// 配置成功
const handleConfigSuccess = () => {
  // PermissionConfigDialog 已刷新 permissionStore
}

// 监听路由参数，自动选择角色
watch(() => route.query.roleId, (roleId) => {
  if (roleId && roleStore.loaded) {
    const role = roleStore.findRoleById(Number(roleId))
    if (role) {
      roleStore.selectRole(role)
    }
  }
}, { immediate: true })

onMounted(() => {
  loadResources()
})
</script>

<style lang="scss" scoped>
.role-permission-panel {
  display: flex;
  height: calc(100vh - 140px);

  .left-panel {
    width: 300px;
    border-right: 1px solid var(--color-border);
    background: var(--color-bg-elevated);
    overflow-y: auto;
  }

  .right-panel {
    flex: 1;
    padding: 24px;
    overflow-y: auto;

    .panel-header {
      margin-bottom: 16px;

      .panel-title {
        @include gradient-text;
        font-size: 18px;
        font-weight: 600;
      }
    }

    .resource-table {
      border-radius: 8px;

      :deep(.el-table__header-wrapper th) {
        background: var(--color-bg-elevated);
      }
    }
  }
}
</style>