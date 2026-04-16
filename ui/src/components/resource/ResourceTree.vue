<template>
  <div class="resource-tree-container">
    <!-- 操作按钮 -->
    <div class="tree-actions">
      <el-button
        class="create-button"
        size="small"
        @click="handleCreateRoot"
        v-permission="{ resource: 'RESOURCE_MANAGE', action: 'CREATE' }"
      >
        <el-icon><Plus /></el-icon>
        新增根资源
      </el-button>
    </div>

    <!-- 资源树表格 -->
    <el-table
      ref="tableRef"
      :data="filteredResources"
      :tree-props="treeProps"
      row-key="id"
      default-expand-all
      :expand-row-keys="expandedKeys"
      class="resource-table"
      v-loading="loading"
    >
      <el-table-column prop="name" label="资源名称" min-width="180">
        <template #default="{ row }">
          <div class="name-cell">
            <el-icon v-if="row.icon" class="resource-icon">
              <component :is="row.icon" />
            </el-icon>
            <span>{{ row.name }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="code" label="资源编码" min-width="150">
        <template #default="{ row }">
          <span class="code-text">{{ row.code }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="path" label="路径" min-width="200">
        <template #default="{ row }">
          <span class="path-text">{{ row.path || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="80" align="center">
        <template #default="{ row }">
          {{ row.sort || 0 }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="{ row }">
          <div class="action-buttons">
            <el-button
              type="primary"
              size="small"
              link
              @click="handleCreateChild(row)"
              v-permission="{ resource: 'RESOURCE_MANAGE', action: 'CREATE' }"
            >
              新增子资源
            </el-button>
            <el-button
              type="primary"
              size="small"
              link
              @click="handleEdit(row)"
              v-permission="{ resource: 'RESOURCE_MANAGE', action: 'UPDATE' }"
            >
              编辑
            </el-button>
            <el-button
              :type="row.status === 1 ? 'warning' : 'success'"
              size="small"
              link
              @click="handleToggleStatus(row)"
              v-permission="{ resource: 'RESOURCE_MANAGE', action: 'UPDATE' }"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button
              type="danger"
              size="small"
              link
              @click="handleDelete(row)"
              v-permission="{ resource: 'RESOURCE_MANAGE', action: 'DELETE' }"
            >
              删除
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { resourceApi } from '../../api/resource'

const props = defineProps({
  type: {
    type: String,
    default: 'MENU' // MENU, OPERATION, API
  },
  resources: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['create', 'edit', 'refresh'])

// 表格引用
const tableRef = ref()
const loading = ref(false)

// 树属性配置
const treeProps = {
  children: 'children',
  hasChildren: 'hasChildren'
}

// 展开的行
const expandedKeys = ref([])

// 过滤后的资源列表（按类型）
const filteredResources = computed(() => {
  return filterByType(props.resources, props.type)
})

// 递归过滤树（按类型）
const filterByType = (tree, type) => {
  const result = []
  for (const node of tree) {
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
    } else if (node.children && node.children.length > 0) {
      // 如果父节点类型不匹配，但子节点可能匹配，递归查找
      const filteredChildren = filterByType(node.children, type)
      if (filteredChildren.length > 0) {
        result.push(...filteredChildren)
      }
    }
  }
  return result
}

// 新增根资源
const handleCreateRoot = () => {
  emit('create', null, props.type)
}

// 新增子资源
const handleCreateChild = (parent) => {
  emit('create', parent.id, props.type)
}

// 编辑资源
const handleEdit = (row) => {
  emit('edit', row)
}

// 切换状态
const handleToggleStatus = async (row) => {
  const action = row.status === 1 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(
      `确定要${action}资源 "${row.name}" 吗？`,
      `${action}确认`,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    loading.value = true
    if (row.status === 1) {
      await resourceApi.disable(row.id)
    } else {
      await resourceApi.enable(row.id)
    }
    ElMessage.success(`${action}成功`)
    emit('refresh')
  } catch (error) {
    if (error !== 'cancel') {
      // error handled by interceptor
    }
  } finally {
    loading.value = false
  }
}

// 删除资源
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除资源 "${row.name}" 吗？删除后该资源下的权限配置将同步删除。`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    loading.value = true
    await resourceApi.delete(row.id)
    ElMessage.success('删除成功')
    emit('refresh')
  } catch (error) {
    if (error !== 'cancel') {
      // error handled by interceptor
    }
  } finally {
    loading.value = false
  }
}

// 加载资源树
const loadResources = async () => {
  try {
    loading.value = true
    const response = await resourceApi.tree()
    // 触发父组件更新
    emit('refresh')
    return response.data || []
  } catch (error) {
    // error handled by interceptor
    return []
  } finally {
    loading.value = false
  }
}

// 暴露方法
defineExpose({
  loadResources
})
</script>

<style lang="scss" scoped>
.resource-tree-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;

  .tree-actions {
    display: flex;
    justify-content: flex-start;

    .create-button {
      @include gradient-button;
      padding: 6px 12px;
      font-size: 12px;

      .el-icon {
        margin-right: 4px;
      }
    }
  }

  .resource-table {
    flex: 1;
    background: var(--color-bg-glass);
    border-radius: 8px;
    border: 1px solid var(--color-border);

    :deep(.el-table__header-wrapper) {
      .el-table__header {
        th {
          background: var(--color-bg-glass);
          color: var(--color-text-primary);
          font-weight: 600;
        }
      }
    }

    :deep(.el-table__body-wrapper) {
      .el-table__body {
        tr {
          background: transparent;

          &:hover > td {
            background: var(--color-bg-glass-hover);
          }
        }

        td {
          background: transparent;
          color: var(--color-text-primary);
          border-bottom: 1px solid var(--color-border-light);
        }
      }
    }

    .name-cell {
      display: flex;
      align-items: center;
      gap: 8px;

      .resource-icon {
        color: var(--color-primary);
      }
    }

    .code-text {
      color: var(--color-text-muted);
      font-family: monospace;
      font-size: 13px;
    }

    .path-text {
      color: var(--color-text-secondary);
      font-size: 13px;
    }

    .action-buttons {
      display: flex;
      justify-content: center;
      gap: 4px;
      flex-wrap: wrap;

      .el-button {
        font-size: 12px;
      }
    }
  }
}
</style>